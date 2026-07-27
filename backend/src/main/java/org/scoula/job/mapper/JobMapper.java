package org.scoula.job.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.job.domain.*;

import java.util.List;

public interface JobMapper {
    // goalType(J01/J02/J03)에 해당하는 직무·직렬·학과 코드 목록 조회
    List<JobCodeVO> findJobCodesByGoalType(String goalType);

    // 진로 목표 신규 등록
    void createJobGoal(JobGoalVO jobGoalVO);

    // 진로 목표에 선택된 준비 항목 등록 (goal_id, item_type 조합 UNIQUE)
    void createJobInterestedType(JobInterestedTypeVO jobInterestedTypeVO);

    // goalId로 진로 목표 단건 조회 (goal_type, job_code_id 확인용)
    JobGoalVO findJobGoal(Long goalId);

    // goalId로 선택된 준비 항목 유형(item_type) 목록 조회
    List<String> findInterestedItemTypes(Long goalId);

    // goalType·jobCodeId·itemTypes 조건으로 준비 항목 기준 데이터 조회
    List<PrepItemCriteriaVO> findPrepItemCriteriaList(
            @Param("goalType") String goalType,
            @Param("jobCodeId") Long jobCodeId,
            @Param("itemTypes") List<String> itemTypes
    );

    // 선택한 prepCritId 목록으로 준비항목 기준 데이터 재조회 (스냅샷 값 복사용)
    List<PrepItemCriteriaVO> findPrepItemCriteriaByIds(@Param("prepCritIds") List<Long> prepCritIds);

    // 선택한 준비항목들을 job_plan에 일괄 저장
    void createJobPlans(@Param("plans") List<JobPlanVO> plans, @Param("createdNm") String createdNm);

    // 재선택 시 기존 스냅샷 소프트 삭제 (del_yn='Y' 후 재저장)
    void deleteJobPlansByGoalId(@Param("goalId") Long goalId, @Param("modifiedNm") String modifiedNm);
}
