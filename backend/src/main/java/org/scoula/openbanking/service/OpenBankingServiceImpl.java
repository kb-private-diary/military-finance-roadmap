package org.scoula.openbanking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.scoula.common.exception.BusinessException;
import org.scoula.openbanking.client.OpenBankingClient;
import org.scoula.openbanking.domain.OpenBankingLinkVO;
import org.scoula.openbanking.domain.SavingAccountVO;
import org.scoula.openbanking.domain.SavingHistoryVO;
import org.scoula.openbanking.domain.SpendingVO;
import org.scoula.openbanking.dto.AccountInfo;
import org.scoula.openbanking.dto.AuthUrlResponse;
import org.scoula.openbanking.dto.LinkRequest;
import org.scoula.openbanking.dto.TokenResponse;
import org.scoula.openbanking.dto.TransactionInfo;
import org.scoula.openbanking.mapper.OpenBankingLinkMapper;
import org.scoula.openbanking.mapper.SavingWriteMapper;
import org.scoula.openbanking.mapper.SpendingMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 오픈뱅킹 연동 서비스 구현
 * Client(Mock/Real)와 Mapper를 조합해 온보딩 연동 흐름을 처리
 *
 * <p>계좌 연동은 여러 계좌를 한 트랜잭션으로 저장하므로, 한 건이라도 실패하면
 * 전부 롤백되도록 {@code @Transactional} 을 검</p>
 */
@Service
@RequiredArgsConstructor
public class OpenBankingServiceImpl implements OpenBankingService {

    private final OpenBankingClient client;
    private final OpenBankingLinkMapper mapper;
    private final SpendingMapper spendingMapper;
    private final SavingWriteMapper savingWriteMapper;

    // 분류 규칙(merchant_category)에 걸리지 않는 가맹점의 기본 카테고리
    private static final String DEFAULT_CATEGORY = "ETC";

    // 오픈뱅킹 공통 설정 (application.properties), 없어도 뜨도록 기본값 지정
    @Value("${openbanking.base-url:https://testapi.openbanking.or.kr/v2.0}")
    private String baseUrl;
    @Value("${openbanking.client-id:70a905a4-46d1-426e-89dd-b3c1e8652e24}")
    private String clientId;
    @Value("${openbanking.callback-url:http://localhost:8080/api/openbanking/callback}")
    private String callbackUrl;
    @Value("${openbanking.scope:login inquiry}")
    private String scope;

    @Override
    public boolean hasLink(Long userId) {
        return mapper.countByUserId(userId) > 0;
    }

    @Override
    public AuthUrlResponse getAuthUrl(Long userId) {
        // state: CSRF 방지용, 회원 식별값 + 랜덤값 조합
        // TODO: state를 세션/DB에 저장해 콜백에서 검증 (지금은 발급만)
        String state = userId + "-" + UUID.randomUUID().toString().replace("-", "");

        // 발급받은 client-id·callback-url·scope를 실제 인증 URL 형태로 조합 (여기서 키를 실사용)
        String authUrl = baseUrl + "/oauth/2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + callbackUrl
                + "&scope=" + scope.replace(" ", "+") // URL 규칙: 공백은 +
                + "&state=" + state;

        AuthUrlResponse res = new AuthUrlResponse();
        res.setAuthUrl(authUrl);
        return res;
    }

    @Override
    @Transactional
    public List<AccountInfo> linkAccounts(Long userId, LinkRequest request) {
        List<String> selected = request.getSelectedFintechNums();
        if (selected == null || selected.isEmpty()) {
            throw BusinessException.badRequest("연동할 계좌를 1개 이상 선택해주세요.", "OPBANK_007");
        }

        // 1. 인증코드 → 토큰 발급
        TokenResponse token = client.getToken(request.getCode());
        if (token == null || token.getAccessToken() == null) {
            throw BusinessException.badRequest("오픈뱅킹 인증에 실패했습니다.", "OPBANK_001");
        }

        // 2. 계좌 목록 조회 (적금·입출금 전부)
        List<AccountInfo> allAccounts = client.getAccounts(token.getAccessToken(), token.getUserSeqNo());

        // 토큰 만료일시 = 현재 + expiresIn(초)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(token.getExpiresIn());
        String actor = String.valueOf(userId);

        // 3. 선택된 계좌만 연동 저장
        List<AccountInfo> linked = new ArrayList<>();
        for (AccountInfo acc : allAccounts) {
            if (!selected.contains(acc.getFintechUseNum())) {
                continue; // 사용자가 선택하지 않은 계좌는 건너뜀
            }
            // 중복 연동 방지 (같은 계좌 재연동 차단)
            if (mapper.countByFintechUseNum(acc.getFintechUseNum()) > 0) {
                throw BusinessException.conflict(
                        "이미 연동된 계좌입니다: " + acc.getAccountNumMasked(), "OPBANK_006");
            }

            OpenBankingLinkVO link = new OpenBankingLinkVO();
            link.setUserId(userId);
            link.setAccessToken(token.getAccessToken());
            link.setRefreshToken(token.getRefreshToken());
            link.setFintechUseNum(acc.getFintechUseNum());
            link.setBankCode(acc.getBankCodeStd());
            link.setAccountNumMasked(acc.getAccountNumMasked());
            link.setExpiresAt(expiresAt);
            link.setCreatedNm(actor);
            // 신규회원(동적 계좌 "8"로 시작) 적금이면 개설일·만기일을 그 회원 입대일 이후로 보정
            // (회원1·2는 시드라 이미 입대일 이후로 정합성 맞음 → 보정 안 함)
            if ("SAVING".equals(acc.getAccountType()) && acc.getFintechUseNum().startsWith("8")) {
                LocalDate enlist = mapper.findEnlistDateByUserId(userId);
                if (enlist != null) {
                    LocalDate open = enlist.plusMonths(1);                 // 입대 1개월 후 적금 개설
                    acc.setOpenDate(open.toString());
                    acc.setMaturityDate(open.plusMonths(18).toString());  // 18개월 만기
                }
            }

            // 적금계좌면 saving_account + saving_history 저장 후 account_id 연결
            // (저장은 오픈뱅킹 담당, 조회·만기금 계산은 석윤 saving 파트가 이 데이터를 사용)
            if ("SAVING".equals(acc.getAccountType())) {
                Long accountId = saveSaving(acc, userId, token, actor);
                link.setAccountId(accountId);
            }

            mapper.insertLink(link);
            linked.add(acc);
        }
        return linked;
    }

    /** 적금계좌를 saving_account에 저장 + 납입내역을 saving_history에 회차별 저장 → account_id 반환 */
    private Long saveSaving(AccountInfo acc, Long userId, TokenResponse token, String actor) {
        // 납입내역(적금 입금) 조회 - 회차·금액 산출
        List<TransactionInfo> pays = client.getTransactions(
                token.getAccessToken(), acc.getFintechUseNum(), null, null);

        long monthlySave = pays.isEmpty() ? 0L : pays.get(0).getAmount(); // 한달 납입금 (회차 금액)
        int monthlyCount = pays.size();                                   // 납입 회차수

        // 1) saving_account 저장 (INSERT 후 accountId 채워짐)
        SavingAccountVO account = new SavingAccountVO();
        account.setUserId(userId);
        account.setBankCode(acc.getBankCodeStd());
        account.setMonthlySave(monthlySave);
        account.setMonthlyCount(monthlyCount);
        account.setCurrAmount(acc.getBalance() == null ? 0L : acc.getBalance()); // 누적납입 = 잔액
        account.setAccountStatus("ACTIVE");
        account.setCreatedNm(actor);
        savingWriteMapper.insertSavingAccount(account);

        // 2) saving_history 회차별 저장
        int round = 1;
        for (TransactionInfo pay : pays) {
            SavingHistoryVO history = new SavingHistoryVO();
            history.setAccountId(account.getAccountId());
            history.setPayRound(round++);
            history.setPayAmount(pay.getAmount());
            history.setCreatedNm(actor);
            savingWriteMapper.insertSavingHistory(history);
        }
        return account.getAccountId();
    }

    @Override
    @Transactional
    public void unlink(Long userId) {
        // 저장된 각 토큰을 오픈뱅킹에 폐기 요청 후, 로컬은 소프트 삭제
        List<OpenBankingLinkVO> links = mapper.findByUserId(userId);
        for (OpenBankingLinkVO link : links) {
            client.revokeToken(link.getAccessToken());
        }
        mapper.softDeleteByUserId(userId, String.valueOf(userId));
    }

    @Override
    @Transactional
    public int syncTransactions(Long userId) {
        // 1. 회원의 연동 계좌들 조회 (사원증 userId → 장부에서 은행 토큰 꺼내기)
        List<OpenBankingLinkVO> links = mapper.findByUserId(userId);
        String actor = String.valueOf(userId);

        // 조회 기간 (Mock은 무시하고 고정 시나리오 반환, Real은 최근 N일로 좁힘)
        String fromDate = "20260101";
        String toDate = "20261231";

        List<SpendingVO> toInsert = new ArrayList<>();
        for (OpenBankingLinkVO link : links) {
            // 적금계좌는 Mock이 빈 리스트를 주므로, 자연스럽게 입출금계좌만 거래가 잡힘
            List<TransactionInfo> txs = client.getTransactions(
                    link.getAccessToken(), link.getFintechUseNum(), fromDate, toDate);

            for (TransactionInfo tx : txs) {
                if (!"OUT".equals(tx.getInoutType())) {
                    continue; // 출금(지출)만 적재, 입금은 제외
                }
                // 가맹점명 → 카테고리 분류 (규칙 없으면 ETC)
                String category = spendingMapper.findCategoryByMerchant(tx.getMerchantName());

                SpendingVO spending = new SpendingVO();
                spending.setUserId(userId);
                spending.setMerchantName(tx.getMerchantName());
                spending.setCategory(category != null ? category : DEFAULT_CATEGORY);
                spending.setAmount(tx.getAmount());
                spending.setSpentAt(tx.getTxDateTime());
                spending.setCreatedNm(actor);
                toInsert.add(spending);
            }
        }

        // TODO: 재동기화 시 중복 적재 방지 (마지막 적재 시점 이후만 조회) - 배치 단계에서 보강
        if (!toInsert.isEmpty()) {
            spendingMapper.insertSpendings(toInsert);
        }
        return toInsert.size();
    }
}
