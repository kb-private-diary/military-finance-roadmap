package org.scoula.rent.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentProgressVO;
import org.scoula.rent.domain.ProgressStep;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.ProgressUpdateRequestDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.ProgressStepDTO;
import org.scoula.rent.mapper.RentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final int TOTAL_PROGRESS_STEPS = 5;

    private final RentMapper mapper;

    @Override
    public List<RegionResponseDTO> findRegions(String sido, String sigunguCode) {
        if (sido == null && sigunguCode == null) {
            // 아무것도 없음 → 시/도 목록
            return mapper.findSidoList().stream()
                    .map(vo -> new RegionResponseDTO(vo.getSidoName(), vo.getSidoName()))
                    .toList();
        } else if (sigunguCode == null) {
            // 시/도만 있음 → 시/군/구 목록
            return mapper.findSigunguListBySido(sido).stream()
                    .map(vo -> new RegionResponseDTO(vo.getSigunguCode(), vo.getSigunguName()))
                    .toList();
        } else {
            // 시/군/구코드 있음 → 읍/면/동 목록
            return mapper.findUmdListBySigunguCode(sigunguCode).stream()
                    .map(vo -> new RegionResponseDTO(vo.getRegionCode(), vo.getUmdName()))
                    .toList();
        }
    }

    @Override
    @Transactional
    public Long createGoal(RentGoalCreateRequestDTO request, Long userId) {
        // 1) 모드별 필수값 검증 (SCHOOL ↔ REGION XOR)
        validateSelectionMode(request);

        // 2) 거주기간 프리셋 → 개월수 변환
        int months = toResidenceMonths(request.getResidencePreset());

        // 3) VO 조립 — 서버가 정하는 값(status·개월수·생성자)을 여기서 채운다
        RentGoalVO goal = new RentGoalVO();
        goal.setUserId(userId);
        goal.setSelectionMode(request.getSelectionMode());
        goal.setSchoolId(request.getSchoolId());
        goal.setCommuteRadiusKm(request.getCommuteRadiusKm());
        goal.setMonthlyBudget(request.getMonthlyBudget());
        goal.setResidencePreset(request.getResidencePreset());
        goal.setResidenceMonths(months);
        goal.setStatus(STATUS_DRAFT);

        String creator = "user:" + userId; // TODO: JWT 연동 후 로그인 사용자명으로 교체
        goal.setCreatedNm(creator);

        // 4) rent_goal INSERT — 실행 후 goal.goalId 에 생성된 번호가 채워진다 (useGeneratedKeys)
        mapper.insertGoal(goal);

        // 5) REGION 모드면 희망 지역들을 rent_goal_region 에 저장 (1:N)
        if ("REGION".equals(request.getSelectionMode())) {
            List<RentGoalRegionVO> regions = request.getRegionCodes().stream()
                    .map(code -> {
                        RentGoalRegionVO region = new RentGoalRegionVO();
                        region.setGoalId(goal.getGoalId());
                        region.setRegionCode(code);
                        region.setCreatedNm(creator);
                        return region;
                    })
                    .toList();
            mapper.insertGoalRegions(regions);
        }

        // 6) 생성된 goalId 반환
        return goal.getGoalId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolSearchResponseDTO> findSchools(String keyword) {
        return mapper.findSchoolsByKeyword(keyword).stream()
                .map(SchoolSearchResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional
    public int updateProgress(Long goalId, ProgressUpdateRequestDTO request, Long userId) {
        boolean done = Boolean.TRUE.equals(request.getIsCompleted());

        // 1) UPSERT — 있으면 UPDATE, 없으면 INSERT
        RentProgressVO progress = new RentProgressVO();
        progress.setGoalId(goalId);
        progress.setStepCode(request.getStepCode());
        progress.setIsCompleted(done ? "Y" : "N");
        progress.setCompletedDate(done ? LocalDateTime.now() : null);
        progress.setCreatedNm("user:" + userId); // TODO: JWT 연동 후 교체
        mapper.upsertProgress(progress);

        // 2) 진행률(%) 재계산해서 반환
        return calculatePercentage(goalId);
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findGoal(Long goalId) {
        RentGoalVO goal = mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }

        // 완료된 단계 코드만 모으기
        Set<String> completedCodes = mapper.findProgressListByGoalId(goalId).stream()
                .filter(p -> "Y".equals(p.getIsCompleted()))
                .map(RentProgressVO::getStepCode)
                .collect(Collectors.toSet());

        // 5단계 전체 상태 조립 (enum 순회 → 저장된 것과 매핑)
        List<ProgressStepDTO> steps = Arrays.stream(ProgressStep.values())
                .map(step -> ProgressStepDTO.builder()
                        .stepCode(step.name())
                        .label(step.getLabel())
                        .completed(completedCodes.contains(step.name()))
                        .build())
                .toList();

        int percentage = (int) (completedCodes.size() * 100L / TOTAL_PROGRESS_STEPS);
        return RentGoalDetailResponseDTO.of(goal, percentage, steps);
    }

    /** 완료 단계 수 / 전체 단계 수(5) * 100 */
    private int calculatePercentage(Long goalId) {
        long completed = mapper.findProgressListByGoalId(goalId).stream()
                .filter(p -> "Y".equals(p.getIsCompleted()))
                .count();
        return (int) (completed * 100 / TOTAL_PROGRESS_STEPS);
    }

    /** SCHOOL / REGION 모드별 필수값 검증 (모드에 따라 달라지는 조건이라 @Valid 대신 여기서) */
    private void validateSelectionMode(RentGoalCreateRequestDTO request) {
        String mode = request.getSelectionMode();
        if ("SCHOOL".equals(mode)) {
            if (request.getSchoolId() == null) {
                throw BusinessException.badRequest("학교를 선택해주세요.", "RENT_002");
            }
        } else if ("REGION".equals(mode)) {
            if (request.getRegionCodes() == null || request.getRegionCodes().isEmpty()) {
                throw BusinessException.badRequest("희망 지역을 선택해주세요.", "RENT_003");
            }
        } else {
            throw BusinessException.badRequest("위치 모드가 올바르지 않습니다.", "RENT_001");
        }
    }

    /** 거주기간 프리셋 → 개월수 매핑 (SEMESTER=6, YEAR=12, GRADUATE=24) */
    private int toResidenceMonths(String preset) {
        switch (preset) {
            case "SEMESTER": return 6;
            case "YEAR":     return 12;
            case "GRADUATE": return 24;
            default:
                throw BusinessException.badRequest("거주기간 선택이 올바르지 않습니다.", "RENT_004");
        }
    }
}
