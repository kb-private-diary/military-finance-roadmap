package org.scoula.job.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.job.client.QnetApiClient;
import org.scoula.job.domain.JobCategoryVO;
import org.scoula.job.domain.JobCourseVO;
import org.scoula.job.domain.JobGoalVO;
import org.scoula.job.domain.JobQualificationVO;
import org.scoula.job.domain.JobRecommendServiceVO;
import org.scoula.job.domain.JobTrainingVO;
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

    // 선택한 훈련과정 소프트 삭제
    void deleteGoalTrainingByGoalId(
            @Param("goalId") Long goalId,
            @Param("modifiedNm") String modifiedNm
    );

    // 선택한 자격증 저장
    void insertGoalQualificationList(
            @Param("goalId") Long goalId,
            @Param("qualifications") List<JobQualificationVO> qualifications,
            @Param("createdNm") String createdNm
    );

    // 선택한 인강 저장
    void insertGoalCourseList(
            @Param("goalId") Long goalId,
            @Param("courseIds") List<Long> courseIds,
            @Param("createdNm") String createdNm
    );

    void  insertGoalTrainingList(
            @Param("goalId") Long goalId,
            @Param("trainings") List<JobTrainingVO> trainings,
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

    // 목표에 저장된 훈련과정 조회
    List<JobTrainingVO> findSelectedTrainingListByGoalId(Long goalId);

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

    // Q-Net 동기화 대상 국가자격증 조회
    List<JobQualificationVO> findQnetQualificationList();

    // Q-Net 응시료 갱신
    int updateQualificationFee(
            @Param("qualId") Long qualId,
            @Param("writtenFee") Long writtenFee,
            @Param("practicalFee") Long practicalFee
    );

    // Q-Net 시험일정 저장 또는 갱신
    int upsertQualificationSchedule(
            @Param("qualId") Long qualId,
            @Param("examYear") Integer examYear,
            @Param("examRound") String examRound,
            @Param("schedule") QnetApiClient.ExamSchedule schedule
    );

    // 목표 ID로 선택한 직무 카테고리 조회
    // 고용24 훈련과정 추천 시 NCS 코드와 상위 카테고리 확인에 사용
    JobCategoryVO findJobCategoryByGoalId(Long goalId);

    // =========================================================
    // 인강 크롤링 데이터 동기화
    // =========================================================

    // 인강 매핑 코드에 해당하는 하위 카테고리 ID 목록 조회
    List<Long> findChildCategoryIdsByCourseMappingCode(
            String courseMappingCode
    );

    // 인강 매핑 코드에 해당하는 카테고리 ID 단건 조회
    Long findCategoryIdByCourseMappingCode(
            String courseMappingCode
    );

    // 편입 인강 매핑 코드별 학과계열 코드 목록 조회
    List<String> findTransferMajorCodesByCourseMappingCode(
            String courseMappingCode
    );

    // 전체 편입 학과계열 코드 목록 조회
    List<String> findAllTransferMajorCodes();

    // 자격증·어학 유형별 목록 조회
    List<JobQualificationVO> findQualificationListByQualType(
            @Param("qualType") String qualType
    );

    // 업체명 + 강의명으로 기존 인강 ID 조회
    Long findCourseIdByProviderAndName(
            @Param("providerName") String providerName,
            @Param("courseName") String courseName
    );

    // 인강 신규 저장
    void insertJobCourse(JobCourseVO jobCourseVO);

    // 기존 인강 정보 갱신
    int updateJobCourse(JobCourseVO jobCourseVO);

    // 자격증·어학 ↔ 인강 매핑 저장
    void upsertQualificationCourse(
            @Param("qualId") Long qualId,
            @Param("courseId") Long courseId,
            @Param("createdNm") String createdNm
    );

    // 공무원 직렬 ↔ 인강 매핑 저장
    void upsertCategoryCourse(
            @Param("categoryId") Long categoryId,
            @Param("courseId") Long courseId,
            @Param("createdNm") String createdNm
    );

    // 편입 학과계열 ↔ 인강 매핑 저장
    void upsertTransferMajorCourse(
            @Param("majorCode") String majorCode,
            @Param("courseId") Long courseId,
            @Param("createdNm") String createdNm
    );
}
