package org.scoula.openbanking.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.scoula.openbanking.dto.AccountInfo;
import org.scoula.openbanking.dto.BalanceInfo;
import org.scoula.openbanking.dto.TokenResponse;
import org.scoula.openbanking.dto.TransactionInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 오픈뱅킹 Mock 구현체 (개발용)
 * {@code @Profile("dev")} 라서 dev 환경에서만 활성화되며, 실제 API 대신 하드코딩/동적 시나리오를 반환
 *
 * <p>[회원 판별] Mock에서는 code를 userId로 사용 (실제 오픈뱅킹 code는 불투명 인증코드지만 Mock 편의상)
 * <ul>
 *   <li>code="1" → 회원1 / code="2" → 회원2 : <b>saving_account 시드와 동일한 계좌</b>(누적납입금·은행코드·개설일 일치)</li>
 *   <li>그 외 code(userId) → <b>신규 회원</b>: userId 기반 유니크 계좌 + 결정적 거래내역을 동적 생성</li>
 * </ul>
 * 신규회원 적금 개설일·만기일은 Service가 그 회원 입대일 이후로 보정 (정합성)</p>
 *
 * <p>[제공 데이터] 적금계좌 = 누적납입액(balance) + 개설일·만기일 + 입금(납입) 리스트 / 입출금계좌 = 잔액 + 입출금 리스트
 * 적금 데이터는 석윤 saving_account, 입출금 출금(지출)은 후회소비(spending)가 사용</p>
 */
@Component
@Profile("!prod")
public class OpenBankingClientMock implements OpenBankingClient {

    private static final String TOKEN_PREFIX = "MOCK_ACCESS_";
    private static final String REFRESH_PREFIX = "MOCK_REFRESH_";

    // 회원 1/2 입출금계좌 (거래내역·유형 판별에 사용)
    private static final String M1_CHECKING = "199167428338341000003";
    private static final String M2_CHECKING = "199167428338341000006";

    // 신규 회원 거래내역 가맹점 풀 (userId 기반으로 골라 다양화)
    private static final String[] MERCHANT_POOL = {
            "배달의민족", "넥슨", "쿠팡이츠", "GS25", "스타벅스", "CU", "세븐일레븐", "국군복지단 PX"
    };

    @Override
    public TokenResponse getToken(String code) {
        // Mock: code를 userId로 사용 (없으면 회원1 기본)
        String userSeqNo = (code == null || code.isEmpty()) ? "1" : code;
        return buildToken(userSeqNo);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        String userSeqNo = (refreshToken != null && refreshToken.startsWith(REFRESH_PREFIX))
                ? refreshToken.substring(REFRESH_PREFIX.length())
                : "1";
        return buildToken(userSeqNo);
    }

    @Override
    public List<AccountInfo> getAccounts(String accessToken, String userSeqNo) {
        List<AccountInfo> result = new ArrayList<>();
        if ("1".equals(userSeqNo)) {
            // 회원1 - 입대 2026-01-05, 개설 2026-03-09 → 만기 2027-09-09
            result.add(withRate(createAccount("199167428338341000001", "004", "KB국민은행", "SAVING", "나라사랑 군적금", "004-01-****111", 1500000L, "2026-03-09", "2027-09-09"), "5.0", "100"));
            result.add(withRate(createAccount("199167428338341000002", "003", "IBK기업은행", "SAVING", "IBK 나라사랑 적금", "003-01-****112", 1250000L, "2026-03-09", "2027-09-09"), "5.0", "100"));
            result.add(createAccount(M1_CHECKING, "004", "KB국민은행", "CHECKING", "KB국민 입출금통장", "004-01-****113", 1000000L, null, null));
        } else if ("2".equals(userSeqNo)) {
            // 회원2 - 입대 2025-11-10, 개설 2025-11-15 → 만기 2027-05-15
            result.add(withRate(createAccount("199167428338341000004", "004", "KB국민은행", "SAVING", "나라사랑 군적금", "004-01-****114", 2400000L, "2025-11-15", "2027-05-15"), "5.0", "100"));
            result.add(withRate(createAccount("199167428338341000005", "088", "신한은행", "SAVING", "장병내일준비적금", "088-01-****115", 950000L, "2025-11-15", "2027-05-15"), "5.0", "100"));
            result.add(createAccount(M2_CHECKING, "004", "KB국민은행", "CHECKING", "KB국민 입출금통장", "004-01-****116", 800000L, null, null));
        } else {
            // 신규 회원 - userId 기반 유니크 계좌, 개설일·만기일은 임시값(Service가 입대일 기반 보정)
            result.add(withRate(createAccount(dynamicFintech(userSeqNo, "1"), "004", "KB국민은행", "SAVING", "나라사랑 군적금",
                    "004-01-****" + pad3(userSeqNo) + "1", 3000000L, "2026-01-01", "2027-07-01"), "5.0", "100"));
            result.add(createAccount(dynamicFintech(userSeqNo, "2"), "004", "KB국민은행", "CHECKING", "KB국민 입출금통장",
                    "004-01-****" + pad3(userSeqNo) + "2", 500000L, null, null));
        }
        return result;
    }

    @Override
    public BalanceInfo getBalance(String accessToken, String fintechUseNum) {
        BalanceInfo info = new BalanceInfo();
        info.setFintechUseNum(fintechUseNum);
        info.setBalanceAmt(balanceOf(fintechUseNum));
        return info;
    }

    @Override
    public List<TransactionInfo> getTransactions(String accessToken, String fintechUseNum,
                                                 String fromDate, String toDate) {
        // 입출금계좌 = 지출(출금) 리스트 / 적금계좌 = 입금(납입) 리스트
        return isCheckingAccount(fintechUseNum)
                ? checkingTransactions(fintechUseNum)
                : savingTransactions(fintechUseNum, fromDate); // 적금 = 개설일(fromDate)부터 매월 납입 동적 생성
    }

    @Override
    public void revokeToken(String accessToken) {
        // Mock: 실제 폐기 호출 없음 (no-op), Real 구현에서 오픈뱅킹 폐기 API 호출
    }

    // ------------------------------------------------------------------
    //  헬퍼
    // ------------------------------------------------------------------

    private TokenResponse buildToken(String userSeqNo) {
        TokenResponse res = new TokenResponse();
        res.setAccessToken(TOKEN_PREFIX + userSeqNo);   // 토큰에 회원 식별자(userId) 심기
        res.setRefreshToken(REFRESH_PREFIX + userSeqNo);
        res.setUserSeqNo(userSeqNo);
        res.setExpiresIn(7776000L); // 90일
        res.setScope("login inquiry");
        res.setTokenType("Bearer");
        return res;
    }

    /** 신규 회원 계좌의 유니크 핀테크이용번호 ("8"로 시작=동적, 끝 1자리로 유형: 1=적금/2=입출금) */
    private String dynamicFintech(String uid, String suffix) {
        return "8" + String.format("%015d", Long.parseLong(uid)) + suffix;
    }

    /** 동적 핀테크번호에서 userId 추출 ("8" + userId 15자 + suffix 구조) */
    private long extractUid(String fintechUseNum) {
        try {
            return Long.parseLong(fintechUseNum.substring(1, 16));
        } catch (Exception e) {
            return 0L;
        }
    }

    /** 마스킹 계좌번호 꼬리 (userId 기반) */
    private String pad3(String uid) {
        return String.format("%03d", Long.parseLong(uid) % 1000);
    }

    /** 입출금계좌 여부 (거래내역·유형 판별) */
    private boolean isCheckingAccount(String fintechUseNum) {
        if (M1_CHECKING.equals(fintechUseNum) || M2_CHECKING.equals(fintechUseNum)) {
            return true;
        }
        return fintechUseNum.startsWith("8") && fintechUseNum.endsWith("2"); // 동적 입출금
    }

    private AccountInfo createAccount(String fintechUseNum, String bankCodeStd, String bankName,
                                      String accountType, String productName, String accountNumMasked,
                                      Long balance, String openDate, String maturityDate) {
        AccountInfo a = new AccountInfo();
        a.setFintechUseNum(fintechUseNum);
        a.setBankCodeStd(bankCodeStd);
        a.setBankName(bankName);
        a.setAccountType(accountType);
        a.setProductName(productName);
        a.setAccountNumMasked(accountNumMasked);
        a.setBalance(balance);
        a.setOpenDate(openDate);       // 적금만 값, 입출금은 null
        a.setMaturityDate(maturityDate);
        return a;
    }

    /** 적금계좌에 금리·정부매칭 세팅 (군적금 목데이터 - 사용자 화면 노출용). 입출금엔 미적용 */
    private AccountInfo withRate(AccountInfo a, String interestRate, String govMatchRate) {
        a.setInterestRate(new BigDecimal(interestRate));
        if (govMatchRate != null) {
            a.setGovMatchRate(new BigDecimal(govMatchRate));
        }
        return a;
    }

    /** 계좌별 잔액 (적금=누적납입금(시드 curr_amount와 일치) / 입출금=현재잔액) */
    private Long balanceOf(String fintechUseNum) {
        switch (fintechUseNum) {
            case "199167428338341000001": return 1500000L; // 회원1 적금 KB
            case "199167428338341000002": return 1250000L; // 회원1 적금 IBK
            case M1_CHECKING:             return 1000000L; // 회원1 입출금
            case "199167428338341000004": return 2400000L; // 회원2 적금 KB
            case "199167428338341000005": return 950000L;  // 회원2 적금 신한
            case M2_CHECKING:             return 800000L;  // 회원2 입출금
            default:
                // 동적 계좌: 적금(끝1)=300만, 입출금(끝2)=50만
                if (fintechUseNum.startsWith("8")) {
                    return fintechUseNum.endsWith("2") ? 500000L : 3000000L;
                }
                return 0L;
        }
    }

    /** 입출금계좌 거래내역 - 지출(출금 OUT) 리스트, 후회소비(spending)가 사용 */
    private List<TransactionInfo> checkingTransactions(String fintechUseNum) {
        List<TransactionInfo> list = new ArrayList<>();
        if (M1_CHECKING.equals(fintechUseNum)) {
            list.add(createTx(2026, 7, 14, 20, 15, "국군복지단 PX", 15200L, "OUT"));
            list.add(createTx(2026, 7, 14, 22, 40, "배달의민족", 23500L, "OUT"));
            list.add(createTx(2026, 7, 15, 23, 55, "넥슨", 30000L, "OUT"));
            list.add(createTx(2026, 7, 16, 21, 10, "쿠팡이츠", 18900L, "OUT"));
            list.add(createTx(2026, 7, 17, 14, 20, "KTX 서울-부산", 59800L, "OUT"));
            list.add(createTx(2026, 7, 17, 23, 30, "넥슨", 50000L, "OUT"));
        } else if (M2_CHECKING.equals(fintechUseNum)) {
            list.add(createTx(2026, 7, 14, 19, 30, "국군복지단 PX", 8500L, "OUT"));
            list.add(createTx(2026, 7, 15, 19, 20, "GS25 부대앞점", 4300L, "OUT"));
            list.add(createTx(2026, 7, 16, 20, 10, "세븐일레븐", 6800L, "OUT"));
            list.add(createTx(2026, 7, 17, 8, 15, "CU 위병소점", 5200L, "OUT"));
            list.add(createTx(2026, 7, 17, 15, 45, "스타벅스", 6500L, "OUT"));
            list.add(createTx(2026, 7, 18, 20, 45, "국군복지단 PX", 12500L, "OUT"));
        } else {
            return dynamicCheckingTransactions(fintechUseNum); // 신규회원 = userId 기반 다양화
        }
        return list;
    }

    /** 신규 회원 지출 리스트 - userId를 seed로 결정적 생성 (같은 회원은 항상 같은 내역) */
    private List<TransactionInfo> dynamicCheckingTransactions(String fintechUseNum) {
        long uid = extractUid(fintechUseNum);
        Random r = new Random(uid); // 시드 고정 → 재조회해도 동일한 내역
        int count = 3 + r.nextInt(3); // 3~5건

        List<TransactionInfo> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String merchant = MERCHANT_POOL[r.nextInt(MERCHANT_POOL.length)];
            long amount = (5 + r.nextInt(46)) * 1000L; // 5,000 ~ 50,000원
            int day = 10 + r.nextInt(15);              // 7월 10~24일
            list.add(createTx(2026, 7, day, 20, 0, merchant, amount, "OUT"));
        }
        return list;
    }

    /**
     * 적금계좌 납입내역 - 입금(IN) 리스트, 석윤 saving_history가 사용.
     * 개설일(fromDate)부터 현재까지 매월 25일 자동납입으로 동적 생성 → 회원마다 개설일이 달라도 정합성 유지.
     * 납입액 합 = 누적납입금(balance)에 정확히 맞춤(방식B): 천원 단위 균등 + 마지막 회차로 잔액 보정.
     * (멘토 조언: 고정값 대신 개설일 기준 매월 납입 / balance 유지로 시드·만기금 계산에 영향 없음)
     */
    private List<TransactionInfo> savingTransactions(String fintechUseNum, String fromDate) {
        List<TransactionInfo> list = new ArrayList<>();
        long balance = balanceOf(fintechUseNum);           // 누적납입금 (이 합에 맞춰 분배)
        if (balance <= 0 || fromDate == null) {
            return list;
        }
        // 개설일 이후 첫 25일부터 오늘 이전까지 매월 납입일 수집
        LocalDate open = LocalDate.parse(fromDate);
        LocalDate today = LocalDate.now();
        LocalDate payDay = open.withDayOfMonth(25);
        if (payDay.isBefore(open)) {
            payDay = payDay.plusMonths(1);                 // 개설일이 25일 이후면 다음 달부터
        }
        List<LocalDate> payDates = new ArrayList<>();
        while (!payDay.isAfter(today)) {
            payDates.add(payDay);
            payDay = payDay.plusMonths(1);
        }
        int n = payDates.size();
        if (n == 0) {
            return list;
        }
        long perPay = (balance / n / 1000) * 1000;         // 천원 단위 균등 월납입액
        long accumulated = 0;
        for (int i = 0; i < n; i++) {
            // 마지막 회차에 잔액을 몰아 합계를 balance와 정확히 일치시킴
            long amount = (i == n - 1) ? (balance - accumulated) : perPay;
            accumulated += amount;
            LocalDate d = payDates.get(i);
            list.add(createTx(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), 9, 0, "군적금 자동납입", amount, "IN"));
        }
        return list;
    }

    private TransactionInfo createTx(int year, int month, int day, int hour, int min,
                                     String merchantName, Long amount, String inoutType) {
        TransactionInfo t = new TransactionInfo();
        t.setTxDateTime(LocalDateTime.of(year, month, day, hour, min)); // month 1-12 그대로 (Calendar 불필요)
        t.setMerchantName(merchantName);
        t.setAmount(amount);
        t.setInoutType(inoutType); // "OUT"(지출) / "IN"(입금·납입)
        return t;
    }
}
