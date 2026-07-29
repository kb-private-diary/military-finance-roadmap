package org.scoula.rent.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.mapper.RentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private static final String STATUS_DRAFT = "DRAFT";

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
        goal.setCreatedNm("user:" + userId); // TODO: JWT 연동 후 로그인 사용자명으로 교체

        // 4) INSERT — 실행 후 goal.goalId 에 생성된 번호가 채워진다 (useGeneratedKeys)
        mapper.insertGoal(goal);

        // 5) 생성된 goalId 반환
        return goal.getGoalId();
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
