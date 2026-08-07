package org.scoula.member.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import org.scoula.member.dto.UserRankInfoDTO;

public interface RankMapper {
    // 진급 재계산 대상(입대일이 있고 아직 전역 전인 활성 유저) 목록
    List<UserRankInfoDTO> findActiveEnlistedUsers();

    // monthsSinceEnlist 이하인 service_months 중 가장 큰 값을 가진 계급(구간 매칭). 매칭 없으면 null
    Integer findRankIdByServiceMonths(@Param("monthsSinceEnlist") int monthsSinceEnlist);

    int updateRankId(
            @Param("userId") Long userId,
            @Param("rankId") Integer rankId,
            @Param("modifiedNm") String modifiedNm);
}
