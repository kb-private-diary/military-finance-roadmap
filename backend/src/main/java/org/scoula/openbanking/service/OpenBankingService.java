package org.scoula.openbanking.service;

import java.util.List;

import org.scoula.openbanking.dto.AccountInfo;
import org.scoula.openbanking.dto.AuthUrlResponse;
import org.scoula.openbanking.dto.LinkRequest;

/**
 * 오픈뱅킹 연동 서비스
 * 온보딩(최초 1회 연동)과 연동 해제를 담당
 */
public interface OpenBankingService {

    /** 온보딩 체크: 회원이 이미 오픈뱅킹을 연동했는지 (false면 연동 화면으로) */
    boolean hasLink(Long userId);

    /** 오픈뱅킹 인증 URL 발급 (프론트가 이 URL로 사용자를 리다이렉트) */
    AuthUrlResponse getAuthUrl(Long userId);

    /** 연동 가능한 계좌 목록 조회 (사용자가 이 중에서 선택해 link). 실제 오픈뱅킹은 인증 콜백 code로 조회, Mock은 userId 기반 */
    List<AccountInfo> getLinkableAccounts(Long userId);

    /** 사용자가 선택한 계좌들을 연동 (openbanking_link 저장). 연동된 계좌 목록 반환 */
    List<AccountInfo> linkAccounts(Long userId, LinkRequest request);

    /** 회원의 오픈뱅킹 연동 전체 해제 */
    void unlink(Long userId);

    /**
     * 연동된 계좌들의 거래내역을 조회해 지출(spending)로 적재
     * 입출금계좌의 출금 거래만 카테고리 분류 후 저장
     * @return 적재한 지출 건수
     */
    int syncTransactions(Long userId);

    /**
     * [월급 배치] 오픈뱅킹 연동 회원 전체에 이번 달까지의 급여(income)를 멱등 적재
     * 스케줄러(매일)와 데모용 수동 트리거가 공용으로 호출
     * @return 이번 배치에서 새로 적재된 급여 건수
     */
    int runMonthlySalaryBatch();
}
