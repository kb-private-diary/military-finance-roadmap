package org.scoula.rent.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.exception.BusinessException;
import org.scoula.rent.domain.RentGoalVO;
import org.scoula.rent.domain.RentGoalRegionVO;
import org.scoula.rent.domain.RentListingVO;
import org.scoula.rent.domain.ResidencePreset;
import org.scoula.rent.dto.RegionResponseDTO;
import org.scoula.rent.dto.RentGoalCreateRequestDTO;
import org.scoula.rent.dto.SchoolSearchResponseDTO;
import org.scoula.rent.dto.RentGoalDetailResponseDTO;
import org.scoula.rent.dto.RentListingResponseDTO;
import org.scoula.rent.dto.RentListingDetailResponseDTO;
import org.scoula.rent.mapper.RentMapper;
import org.scoula.rent.mapper.RentListingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentServiceImpl implements RentService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String MODE_SCHOOL = "SCHOOL";
    private static final String MODE_REGION = "REGION";

    private final RentMapper mapper;
    private final RentListingMapper listingMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponseDTO> findRegions(String sido, String sigunguCode) {
        if (sido == null && sigunguCode == null) {
            // 아무것도 없음 → 시/도 목록
            return this.mapper.findSidoList().stream()
                    .map(vo -> new RegionResponseDTO(vo.getSidoName(), vo.getSidoName()))
                    .toList();
        } else if (sigunguCode == null) {
            // 시/도만 있음 → 시/군/구 목록
            return this.mapper.findSigunguListBySido(sido).stream()
                    .map(vo -> new RegionResponseDTO(vo.getSigunguCode(), vo.getSigunguName()))
                    .toList();
        } else {
            // 시/군/구코드 있음 → 읍/면/동 목록
            return this.mapper.findUmdListBySigunguCode(sigunguCode).stream()
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

        String creator = "user:" + userId; // TODO: JWT 연동 후 로그인 사용자명으로 교체

        // 3) 재등록 방식: 기존 DRAFT 를 soft delete(del_yn='Y') 후 새로 INSERT
        //    자취는 "목표 수정"이 없음 - 등록값을 바꾸면 step1부터 다시 추천해야 해서 새 등록과 동일하기 때문
        //    그래서 conflict(막기)가 아니라 기존 DRAFT 대체로 처리 (결과적으로 회원당 DRAFT 1건 유지)
        this.mapper.deleteDraftGoalByUserId(userId, creator);

        // 4) VO 조립 - 서버가 정하는 값(status·개월수·생성자)을 여기서 채움
        RentGoalVO goal = new RentGoalVO();
        goal.setUserId(userId);
        goal.setSelectionMode(request.getSelectionMode());
        goal.setSchoolId(request.getSchoolId());
        goal.setCommuteRadiusKm(request.getCommuteRadiusKm());
        goal.setMonthlyBudget(request.getMonthlyBudget());
        goal.setResidencePreset(request.getResidencePreset());
        goal.setResidenceMonths(months);
        goal.setStatus(STATUS_DRAFT);
        goal.setCreatedNm(creator);

        // 5) rent_goal INSERT - 실행 후 goal.goalId 에 생성된 번호가 채워짐 (useGeneratedKeys)
        this.mapper.insertGoal(goal);

        // 6) REGION 모드면 희망 지역들을 rent_goal_region 에 저장 (1:N)
        if (MODE_REGION.equals(request.getSelectionMode())) {
            List<RentGoalRegionVO> regions = request.getRegionCodes().stream()
                    .map(code -> {
                        RentGoalRegionVO region = new RentGoalRegionVO();
                        region.setGoalId(goal.getGoalId());
                        region.setRegionCode(code);
                        region.setCreatedNm(creator);
                        return region;
                    })
                    .toList();
            this.mapper.insertGoalRegions(regions);
        }

        // 7) 생성된 goalId 반환
        return goal.getGoalId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolSearchResponseDTO> findSchools(String keyword) {
        return this.mapper.findSchoolsByKeyword(keyword).stream()
                .map(SchoolSearchResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RentGoalDetailResponseDTO findGoal(Long goalId) {
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }
        return RentGoalDetailResponseDTO.of(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentListingResponseDTO> findListings(Long goalId) {
        RentGoalVO goal = this.mapper.findGoalById(goalId);
        if (goal == null) {
            throw BusinessException.notFound("목표를 찾을 수 없습니다.", "RENT_005");
        }

        List<RentListingVO> listings;
        if (MODE_SCHOOL.equals(goal.getSelectionMode())) {
            // 학교 좌표 기준 반경 내 매물
            listings = this.listingMapper.findListingsBySchool(
                    goal.getSchoolId(), goal.getCommuteRadiusKm(), goal.getMonthlyBudget());
        } else {
            // 희망 지역(법정동코드)에 속한 매물
            List<String> regionCodes = this.mapper.findRegionCodesByGoalId(goalId);
            listings = regionCodes.isEmpty()
                    ? List.of()
                    : this.listingMapper.findListingsByRegions(regionCodes, goal.getMonthlyBudget());
        }
        return listings.stream().map(RentListingResponseDTO::of).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RentListingDetailResponseDTO findListingDetail(Long listingId) {
        RentListingVO vo = this.listingMapper.findById(listingId);
        if (vo == null) {
            throw BusinessException.notFound("매물을 찾을 수 없습니다.", "RENT_006");
        }
        return RentListingDetailResponseDTO.of(vo);
    }

    /** SCHOOL / REGION 모드별 필수값 검증 (모드에 따라 달라지는 조건이라 @Valid 대신 여기서) */
    private void validateSelectionMode(RentGoalCreateRequestDTO request) {
        String mode = request.getSelectionMode();
        if (MODE_SCHOOL.equals(mode)) {
            if (request.getSchoolId() == null) {
                throw BusinessException.badRequest("학교를 선택해주세요.", "RENT_002");
            }
        } else if (MODE_REGION.equals(mode)) {
            if (request.getRegionCodes() == null || request.getRegionCodes().isEmpty()) {
                throw BusinessException.badRequest("희망 지역을 선택해주세요.", "RENT_003");
            }
        } else {
            throw BusinessException.badRequest("위치 모드가 올바르지 않습니다.", "RENT_001");
        }
    }

    /** 거주기간 프리셋 → 개월수 (ResidencePreset enum 사용, 잘못된 값이면 RENT_004) */
    private int toResidenceMonths(String preset) {
        try {
            return ResidencePreset.valueOf(preset).getMonths();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw BusinessException.badRequest("거주기간 선택이 올바르지 않습니다.", "RENT_004");
        }
    }
}
