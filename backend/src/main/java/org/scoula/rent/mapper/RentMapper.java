package org.scoula.rent.mapper;

import org.scoula.rent.domain.RegionCodeVO;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.SchoolVO;
import org.scoula.rent.domain.RentProgressVO;
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

    // 진행률 단계 UPSERT (goal_id + step_code 있으면 UPDATE, 없으면 INSERT)
    void upsertProgress(RentProgressVO progress);

    // 목표의 진행 단계 목록 조회 (진행률 계산·표시용)
    List<RentProgressVO> findProgressListByGoalId(Long goalId);

    // 목표 단건 조회 (상세)
    RentGoalVO findGoalById(Long goalId);

    // 재등록 시 기존 DRAFT 목표 soft delete (회원당 DRAFT 1건 유지)
    void deleteDraftGoalByUserId(@Param("userId") Long userId, @Param("modifiedNm") String modifiedNm);

    // 목표 상태 변경 (로드맵 저장: DRAFT → CONFIRMED)
    void updateGoalStatus(@Param("goalId") Long goalId, @Param("status") String status, @Param("modifiedNm") String modifiedNm);

    // 기존 CONFIRMED 목표를 ARCHIVED 로 보관 (회원당 CONFIRMED 1건 유지)
    void archiveConfirmedGoalByUserId(@Param("userId") Long userId, @Param("modifiedNm") String modifiedNm);
}
