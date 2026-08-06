package org.scoula.push.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import org.scoula.push.domain.PushSubscriptionVO;

public interface PushSubscriptionMapper {
    // endpoint로 기존 구독 조회 (del_yn 무관 - 재구독/재활성화 판단에 씀)
    PushSubscriptionVO findByEndpoint(String endpoint);

    // 특정 유저의 활성 구독 목록 조회 (발송 대상 조회용)
    List<PushSubscriptionVO> findListByUserId(Long userId);

    // 신규 구독 등록
    void insert(PushSubscriptionVO vo);

    // 기존 endpoint 재활성화 (del_yn 여부와 무관하게 소유자/키 갱신 + del_yn='N')
    void reactivate(PushSubscriptionVO vo);

    // 구독 해지 (소프트 삭제)
    void deleteByEndpoint(
            @Param("endpoint") String endpoint, @Param("modifiedNm") String modifiedNm);
}
