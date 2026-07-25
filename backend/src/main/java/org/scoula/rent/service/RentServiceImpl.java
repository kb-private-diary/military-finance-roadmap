package org.scoula.rent.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.RentGoalCreateResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentGoalRegionCreateRequestDTO;
import org.scoula.rent.mapper.RentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private static final int MAX_REGION_COUNT = 5;

    private final RentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponseDTO> findRegions(String sido, String sigunguCode) {
        if (sido == null && sigunguCode == null) {
            return mapper.findSidoList().stream()
                    .map(vo -> new RegionResponseDTO(vo.getSidoName(), vo.getSidoName()))
                    .toList();
        } else if (sigunguCode == null) {
            return mapper.findSigunguListBySido(sido).stream()
                    .map(vo -> new RegionResponseDTO(vo.getSigunguCode(), vo.getSigunguName()))
                    .toList();
        } else {
            return mapper.findUmdListBySigunguCode(sigunguCode).stream()
                    .map(vo -> new RegionResponseDTO(vo.getRegionCode(), vo.getUmdName()))
                    .toList();
        }
    }

    @Override
    @Transactional
    public RentGoalCreateResponseDTO createRentGoal(Long userId, RentGoalCreateRequestDTO request) {
        this.validateCreateRequest(userId, request);
        String actor = String.valueOf(userId);

        // 재등록: 기존 DRAFT 가 있으면 소프트 삭제로 대체
        RentGoalVO existingDraft = mapper.findDraftRentGoalByUserId(userId);
        if (existingDraft != null) {
            existingDraft.setModifiedNm(actor);
            mapper.softDeleteRentGoal(existingDraft);
        }

        RentGoalVO goal = new RentGoalVO();
        goal.setUserId(userId);
        goal.setTitle(request.getTitle());
        goal.setTradeType(request.getTradeType());
        goal.setEstateType(request.getEstateType());
        goal.setMaxDeposit(request.getMaxDeposit());
        goal.setMaxMonthly(request.getMaxMonthly());
        goal.setRoomCount(request.getRoomCount());
        goal.setExpectedFee(request.getExpectedFee());
        goal.setResidenceTerm(request.getResidenceTerm());
        goal.setCurrentAsset(request.getCurrentAsset());
        goal.setTargetDate(request.getTargetDate());
        goal.setCreatedNm(actor);

        mapper.insertRentGoal(goal);
        return new RentGoalCreateResponseDTO(goal.getGoalId());
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findCurrentRentGoal(Long userId) {
        RentGoalVO goal = mapper.findCurrentRentGoalByUserId(userId);
        return goal == null ? null : RentGoalDetailResponseDTO.of(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findRentGoal(Long goalId, Long userId) {
        return RentGoalDetailResponseDTO.of(this.findOwnedGoal(goalId, userId));
    }

    @Override
    @Transactional
    public void deleteRentGoal(Long goalId, Long userId) {
        RentGoalVO goal = this.findOwnedGoal(goalId, userId);
        goal.setModifiedNm(String.valueOf(userId));
        mapper.softDeleteRentGoal(goal);
    }

    @Override
    @Transactional
    public void addGoalRegions(Long goalId, Long userId, RentGoalRegionCreateRequestDTO request) {
        this.findOwnedGoal(goalId, userId); // 존재·소유 검증
        List<String> codes = request == null ? null : request.getRegionCodes();
        if (codes == null || codes.isEmpty()) {
            throw BusinessException.badRequest("희망 지역을 1개 이상 선택해주세요.", "RENT_004");
        }
        if (codes.size() > MAX_REGION_COUNT) {
            throw BusinessException.badRequest("희망 지역은 최대 " + MAX_REGION_COUNT + "개까지 등록할 수 있습니다.", "RENT_004");
        }

        String actor = String.valueOf(userId);

        // 재등록: 기존 지역 소프트 삭제 후 새로 등록
        RentGoalRegionVO deleteCond = new RentGoalRegionVO();
        deleteCond.setGoalId(goalId);
        deleteCond.setModifiedNm(actor);
        mapper.softDeleteRegionsByGoalId(deleteCond);

        for (String code : codes) {
            RentGoalRegionVO region = new RentGoalRegionVO();
            region.setGoalId(goalId);
            region.setRegionCode(code);
            region.setCreatedNm(actor);
            mapper.insertGoalRegion(region);
        }
    }

    // ---------- 내부 헬퍼 ----------

    private RentGoalVO findOwnedGoal(Long goalId, Long userId) {
        RentGoalVO goal = mapper.findRentGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("자취 목표를 찾을 수 없습니다.", "RENT_001");
        }
        if (!goal.getUserId().equals(userId)) {
            throw BusinessException.forbidden("본인의 목표만 접근할 수 있습니다.", "RENT_002");
        }
        return goal;
    }

    private void validateCreateRequest(Long userId, RentGoalCreateRequestDTO request) {
        if (userId == null) {
            throw BusinessException.badRequest("userId 는 필수입니다.", "RENT_003");
        }
        if (this.isBlank(request.getTradeType())) {
            throw BusinessException.badRequest("거래유형은 필수입니다.", "RENT_003");
        }
        if (this.isBlank(request.getResidenceTerm())) {
            throw BusinessException.badRequest("거주기간은 필수입니다.", "RENT_003");
        }
        if (request.getMaxDeposit() == null || request.getMaxDeposit() <= 0) {
            throw BusinessException.badRequest("보증금 한도는 0보다 커야 합니다.", "RENT_003");
        }
        if (request.getMaxMonthly() == null || request.getMaxMonthly() <= 0) {
            throw BusinessException.badRequest("월세 한도는 0보다 커야 합니다.", "RENT_003");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
