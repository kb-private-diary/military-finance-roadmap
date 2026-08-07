package org.scoula.rent.mapper;

import org.scoula.rent.domain.RegionCodeVO;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.SchoolVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RentMapper {
    // 시/도 목록 (중복 제거)
    List<RegionCodeVO> findSidoList();

    // 특정 시/도의 시/군/구 목록
    List<RegionCodeVO> findSigunguListBySido(String sidoName);

    // 특정 시/군/구의 읍/면/동 목록
    List<RegionCodeVO> findUmdListBySigunguCode(String sigunguCode);

    // 자취 목표 등록 (INSERT 후 생성된 goalId 가 goal 객체에 채워짐)
    void insertGoal(RentGoalVO goal);

    // 목표별 희망 지역 등록 (REGION 모드, 여러 건 한 번에)
    void insertGoalRegions(List<RentGoalRegionVO> regions);

    // 학교 검색 (자동완성, 이름 부분일치)
    List<SchoolVO> findSchoolsByKeyword(String keyword);

    // 목표 단건 조회 (상세)
    RentGoalVO findGoalById(Long goalId);

    // 목표의 희망 지역 법정동코드 목록 (REGION 모드 매물 조회용)
    List<String> findRegionCodesByGoalId(Long goalId);

    // 시군구코드 + 법정동명 → 법정동코드 (국토부 응답엔 코드가 없어 매물 적재 시 매핑)
    String findRegionCodeBySigunguAndUmd(@Param("sigunguCode") String sigunguCode,
                                         @Param("umdName") String umdName);

    // 재등록 시 기존 DRAFT 목표 soft delete (회원당 DRAFT 1건 유지)
    void deleteDraftGoalByUserId(@Param("userId") Long userId, @Param("modifiedNm") String modifiedNm);

    // 회원의 진행중(DRAFT) 목표 단건 조회 (없으면 null)
    RentGoalVO findCurrentGoalByUserId(Long userId);

    // 목표 단건 soft delete (goal_id 기준)
    void deleteGoalById(@Param("goalId") Long goalId, @Param("modifiedNm") String modifiedNm);
}
