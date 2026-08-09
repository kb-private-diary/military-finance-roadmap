package org.scoula.job.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.job.domain.JobCategoryVO;
import org.scoula.job.domain.JobCourseVO;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobQualificationVO;
import org.scoula.job.domain.JobRecommendServiceVO;
import org.scoula.job.domain.JobTransferMajorVO;
import org.scoula.job.domain.JobTransferUniversityVO;
import org.scoula.job.dto.JobGoalDetailResponseDTO;

import java.util.List;

public interface JobMapper {
    // 분류 조회

    // 취업·공무원 분류 목록 조회
    List<JobCategoryVO> findCategoryListByGoalType(String goalType);
    // 편입 대학 목록 조회
    List<JobTransferUniversityVO> findTransferUniversityList();
    // 선택한 대학의 편입 모집 학과계열 목록 조회
    List<JobTransferMajorVO> findTransferMajorListByUnivId(Long univId);

    // 목표 등록

    // 진로 목표 신규 등록
    void insertJobGoal(JobGoalVO jobGoalVO);

    // 작성 중인 진로 목표 수정
    int updateJobGoal(JobGoalVO jobGoalVO);

    // 선택한 자격증 소프트 삭제
    void deleteGoalQualificationByGoalId(
            @Param("goalId") Long goalId,
            @Param("modifiedNm") String modifiedNm
    );

    // 선택한 인강 소프트 삭제
    void deleteGoalCourseByGoalId(
            @Param("goalId") Long goalId,
            @Param("modifiedNm") String modifiedNm
    );

    // 선택한 자격증 저장
    void insertGoalQualificationList(
            @Param("goalId") Long goalId,
            @Param("qualIds") List<Long> qualIds,
            @Param("createdNm") String createdNm
    );

    // 선택한 인강 저장
    void insertGoalCourseList(
            @Param("goalId") Long goalId,
            @Param("courseIds") List<Long> courseIds,
            @Param("createdNm") String createdNm
    );

    //진로 목표 단건 조회
    JobGoalVO findJobGoal(Long goalId);

    // 취업·공무원 분류별 자격증·어학 추천 목록 조회
    List<JobQualificationVO> findQualificationListByCategoryId(Long categoryId);

    // 편입 학과계열별 자격증·어학 추천 목록 조회
    List<JobQualificationVO> findQualificationListByMajorId(Long majorId);

    // 선택한 자격증·어학에 연결된 개별 인강 목록 조회
    List<JobCourseVO> findCourseListByQualificationIds(
            @Param("qualIds") List<Long> qualIds
    );

    // 공무원 직렬별 인강 목록 조회
    List<JobCourseVO> findCourseListByCategoryId(Long categoryId);

    // 편입 학과계열별 인강 목록 조회
    List<JobCourseVO> findCourseListByMajorId(Long majorId);

    // 목표유형별 정책과 공통 KB 서비스 목록 조회
    List<JobRecommendServiceVO> findRecommendServiceListByGoalType(String goalType);

    // 선택한 자격증 목록 조회
    List<JobQualificationVO> findQualificationListByIds(
            @Param("qualIds") List<Long> qualIds
    );

    // 선택한 인강 목록 조회
    List<JobCourseVO> findCourseListByIds(
            @Param("courseIds") List<Long> courseIds
    );

    // 목표에 저장된 자격증·어학 조회
    List<JobQualificationVO> findSelectedQualificationListByGoalId(Long goalId);

    // 목표에 저장된 인강 조회
    List<JobCourseVO> findSelectedCourseListByGoalId(Long goalId);

    // 목표 상세 조회
    JobGoalDetailResponseDTO findJobGoalDetail(Long goalId);

    // 진로 로드맵 저장 확정
    void updateJobGoalStatus(
            @Param("goalId") Long goalId,
            @Param("status") String status,
            @Param("modifiedNm") String modifiedNm
    );

    // 사용자의 가장 최근 작성 중(DRAFT) 진로 목표 ID 조회
    Long findCurrentJobGoalIdByUserId(Long userId);

    // 진로 목표 soft delete
    // del_yn을 'Y'로 변경하고 수정일시·수정자를 함께 기록
    void deleteJobGoalById(
            @Param("goalId") Long goalId,
            @Param("modifiedNm") String modifiedNm
    );
}
