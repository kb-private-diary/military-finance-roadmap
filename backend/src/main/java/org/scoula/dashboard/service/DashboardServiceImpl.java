package org.scoula.dashboard.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.common.exception.BusinessException;
import org.scoula.common.util.MilitarySavingsCalculator;
import org.scoula.common.util.MilitarySavingsCalculator.CalcResult;
import org.scoula.saving.mapper.MilitarySavingProductMapper;
import org.scoula.saving.util.MilitarySavingRateResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.dashboard.domain.VacationVO;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
// openbanking 패키지에서 VO 객체 import (향후 교체)
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationCreateRequestDTO;
import org.scoula.dashboard.dto.DashboardVacationDetailResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationItemDTO;
import org.scoula.dashboard.dto.DashboardVacationListResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationUsageDTO;
import org.scoula.dashboard.mapper.DashboardMapper;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardServiceImpl implements DashboardService {
    // 정기휴가(연가) 카테고리 코드. 마스터(부여) 행 하나 + 사용내역 행 여러 개로 관리된다.
    private static final String CATEGORY_REGULAR = "REGULAR";

    // DashboardSavingAccountDTO account -> SavingAccountVO account 교체
    // VO getter 가 같은지 확인 필요(getCreatedDate(), getMonthlySave() 등)
    private final DashboardMapper mapper;
    private final MilitarySavingProductMapper militarySavingProductMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardBasicResponseDTO findBasicInfo(Long userId) {
        // 1. DB에서 조인된 기본 정보 가져오기
        DashboardBasicResponseDTO dto = this.mapper.findBasicInfoByUserId(userId);
        if (dto == null) {
            throw BusinessException.notFound("유저를 찾을 수 없습니다.", "DASH_001");
        }
        
        LocalDate enlistDate = dto.getEnlistDate();
        LocalDate dischargeDate = dto.getDischargeDate();
        LocalDate today = LocalDate.now();
        
        // 날짜가 없으면 기본값(0) 반환 (방어 로직)
        if (enlistDate == null || dischargeDate == null) {
            dto.setTotalServiceDays(0L);
            dto.setCurrentServiceDays(0L);
            dto.setServiceRate(0.0);
            return dto;
        }
        
        // 2. 총 복무일 (입대일 ~ 전역일 + 1)
        long totalServiceDays = ChronoUnit.DAYS.between(enlistDate, dischargeDate) + 1;
        if (totalServiceDays <= 0) {
            totalServiceDays = 1; // 0으로 나누기 방지
        }
        
        // 3. 현재 복무일 (입대일 ~ 오늘 + 1)
        long currentServiceDays;
        if (today.isBefore(enlistDate)) {
            currentServiceDays = 0L; // 입대 전
        } else if (today.isAfter(dischargeDate)) {
            currentServiceDays = totalServiceDays; // 전역 후
        } else {
            currentServiceDays = ChronoUnit.DAYS.between(enlistDate, today) + 1; // 복무 중
        }
        
        // 4. 복무율 (백분율 계산 후 소수점 첫째 자리까지만 포맷팅)
        double serviceRate = ((double) currentServiceDays / totalServiceDays) * 100.0;
        serviceRate = Math.round(serviceRate * 10.0) / 10.0;
        
        // 5. 계산된 필드 셋팅
        dto.setTotalServiceDays(totalServiceDays);
        dto.setCurrentServiceDays(currentServiceDays);
        dto.setServiceRate(serviceRate);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSavingsResponseDTO findSavingsStatus(Long userId) {
        Long currentTotalSavings = this.mapper.findCurrentTotalSavings(userId);
        List<DashboardSavingAccountDTO> accounts = this.mapper.findSavingAccountListByUserId(userId);
        
        // 기획 변경: 군적금 가입자만 이용 가능하므로, 계좌가 없으면 404 예외 처리 유도
        if (accounts == null || accounts.isEmpty()) {
            throw BusinessException.notFound("군적금 가입 내역을 찾을 수 없습니다.", "DASH_002");
        }
        
        LocalDate dischargeDate = this.mapper.findDischargeDateByUserId(userId);
        if (dischargeDate == null) {
            dischargeDate = LocalDate.now().plusMonths(24); // fallback
        }
        
        // 입대일 가져오기 (복무개월수 한도 계산용)
        org.scoula.dashboard.dto.DashboardBasicResponseDTO basicInfo = this.mapper.findBasicInfoByUserId(userId);
        LocalDate enlistDate = (basicInfo != null && basicInfo.getEnlistDate() != null) 
                ? basicInfo.getEnlistDate() 
                : LocalDate.now();
        int totalServiceMonths = (int) ChronoUnit.MONTHS.between(
                enlistDate.withDayOfMonth(1), 
                dischargeDate.withDayOfMonth(1));
        if (totalServiceMonths <= 0) {
            totalServiceMonths = 1;
        }
        
        Long expectedMaturityTotal = 0L;

        if (accounts != null) {
            for(DashboardSavingAccountDTO account : accounts) {
                // 1. 해당 계좌의 실제 납입 내역(saving_history) 가져오기
                List<DashboardSavingHistoryDTO> histories =
                        this.mapper.findSavingHistoryListByAccountId(account.getAccountId());

                Long monthlySave = account.getMonthlySave() != null ? account.getMonthlySave() : 0L;

                MilitarySavingRateResolver rateResolver = new MilitarySavingRateResolver(
                        this.militarySavingProductMapper, account.getBankCode());
                CalcResult calc = MilitarySavingsCalculator.calculateAccount(
                        account.getCreatedDate(),
                        monthlySave,
                        dischargeDate,
                        histories,
                        rateResolver
                );

                // 4. 총 원금 및 총 이자 합산
                long totalPrincipal = calc.getTotalPrincipal();
                double totalInterest = calc.getTotalInterest();

                // 5. 정부 매칭지원금 계산 (military_saving_product.gov_match_rate 기준)
                double matchingFund = totalPrincipal * rateResolver.getGovMatchRate();

                // 매칭지원금 최대 한도 제한 (복무개월수 * 해당 계좌 월 납입액)
                double maxMatchingFundLimit = (double) totalServiceMonths * monthlySave;
                if (matchingFund > maxMatchingFundLimit) {
                    matchingFund = maxMatchingFundLimit;
                }
                
                // 6. 비과세 처리 적용(세금 0) 및 최종 예상 만기 수령액 합산
                expectedMaturityTotal += (long) (totalPrincipal + totalInterest + matchingFund);
            }
        }
        
        return new DashboardSavingsResponseDTO(currentTotalSavings, expectedMaturityTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVacationListResponseDTO findVacations(Long userId) {
        List<VacationVO> vacations = this.mapper.findVacationListByUserId(userId);
        VacationGroups groups = this.groupVacations(vacations);

        // REGULAR 사용내역은 마스터의 days 안에 이미 포함된 몫이라 totalDays엔 더하지 않는다.
        int regularUsedDays = this.sumDays(groups.regularUsages);
        int totalDays = groups.regularMaster != null ? this.dayCountOf(groups.regularMaster) : 0;
        int usedDays = regularUsedDays;

        List<DashboardVacationItemDTO> items = new ArrayList<>();
        for (VacationVO vacation : groups.others) {
            int days = this.dayCountOf(vacation);
            totalDays += days;
            if (Boolean.TRUE.equals(vacation.getVacationState())) {
                usedDays += days;
            }
            items.add(DashboardVacationItemDTO.of(vacation));
        }

        // 사용완료(isUsed=true) 카드는 뒤로. 안정정렬이라 같은 isUsed 안에서는 위에서 쌓인
        // 획득일 최신순이 그대로 유지된다.
        items.sort(Comparator.comparing(DashboardVacationItemDTO::getIsUsed));

        if (groups.regularMaster != null) {
            int regularRemainingDays = this.dayCountOf(groups.regularMaster) - regularUsedDays;
            items.add(0, DashboardVacationItemDTO.ofRegularMaster(
                    groups.regularMaster, regularRemainingDays));
        }

        return DashboardVacationListResponseDTO.builder()
                .totalDays(totalDays)
                .usedDays(usedDays)
                .remainingDays(totalDays - usedDays)
                .vacations(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVacationDetailResponseDTO findVacationDetail(Long userId, Long vacationId) {
        VacationVO vacation = this.mapper.findVacationById(vacationId, userId);
        if (vacation == null) {
            throw BusinessException.notFound("휴가 정보를 찾을 수 없습니다.", "DASH_003");
        }

        if (!this.isRegularMaster(vacation)) {
            return DashboardVacationDetailResponseDTO.of(vacation, null, null);
        }

        // REGULAR 마스터 상세는 사용내역 목록(1차/2차...)까지 같이 내려줘야 등록·삭제 화면을 그릴 수 있다.
        List<VacationVO> vacations = this.mapper.findVacationListByUserId(userId);
        VacationGroups groups = this.groupVacations(vacations);

        List<DashboardVacationUsageDTO> usages = new ArrayList<>();
        for (VacationVO usage : groups.regularUsages) {
            usages.add(DashboardVacationUsageDTO.of(usage));
        }
        // "1차/2차..." 이름 순서와 맞도록 사용내역은 획득일 오름차순(오래된 것부터)으로 보여준다.
        usages.sort(Comparator.comparing(DashboardVacationUsageDTO::getAcquiredDate));

        int usedDays = this.sumDays(groups.regularUsages);
        int remainingDays = this.dayCountOf(vacation) - usedDays;

        return DashboardVacationDetailResponseDTO.of(vacation, remainingDays, usages);
    }

    @Override
    @Transactional
    public Long createVacation(
            Long userId, String createdNm, DashboardVacationCreateRequestDTO request) {
        boolean isRegular = CATEGORY_REGULAR.equals(request.getCategory());

        String name;
        LocalDate acquiredDate;
        boolean isUsed;

        if (isRegular) {
            // REGULAR로 등록하는 건 전부 사용내역이다 (마스터는 이미 시드로 존재, 여기선 안 만듦).
            // name/acquiredDate/isUsed는 프론트가 안 보내므로 서버가 자동으로 채운다.
            int usageCount = this.validateAndCountRegularUsage(userId, request.getDays());
            name = (usageCount + 1) + "차 정기휴가";
            acquiredDate = LocalDate.now();
            isUsed = true;
        } else {
            // @Valid로 조건부 필수를 표현할 수 없어 여기서 직접 검증한다.
            if (request.getName() == null || request.getName().isBlank()
                    || request.getAcquiredDate() == null
                    || request.getIsUsed() == null) {
                throw BusinessException.badRequest(
                        "이름·획득일·사용여부는 필수입니다.", "DASH_006");
            }
            name = request.getName();
            acquiredDate = request.getAcquiredDate();
            isUsed = Boolean.TRUE.equals(request.getIsUsed());
        }

        VacationVO vacation = VacationVO.builder()
                .userId(userId)
                .vacationCate(request.getCategory())
                .vacationName(name)
                .vacationGet(acquiredDate)
                .vacationDay(request.getDays())
                .vacationState(isUsed)
                .build();
        vacation.setCreatedNm(createdNm);

        this.mapper.insertVacation(vacation);

        return vacation.getVacationId();
    }

    // REGULAR 사용내역 등록 검증(잔여일수 초과 확인) + 기존 사용내역 개수(차수 이름용) 반환
    private int validateAndCountRegularUsage(Long userId, Integer requestedDays) {
        List<VacationVO> vacations = this.mapper.findVacationListByUserId(userId);
        VacationGroups groups = this.groupVacations(vacations);

        if (groups.regularMaster == null) {
            throw BusinessException.notFound("정기휴가 부여 내역을 찾을 수 없습니다.", "DASH_004");
        }

        int regularUsedDays = this.sumDays(groups.regularUsages);
        int remainingDays = this.dayCountOf(groups.regularMaster) - regularUsedDays;
        if (requestedDays > remainingDays) {
            throw BusinessException.badRequest("정기휴가 잔여일수를 초과했습니다.", "DASH_005");
        }

        return groups.regularUsages.size();
    }

    // vacations를 REGULAR 마스터/REGULAR 사용내역/그 외 카테고리로 분류한다.
    // findVacations·findVacationDetail·validateAndCountRegularUsage가 공통으로 사용한다.
    private VacationGroups groupVacations(List<VacationVO> vacations) {
        VacationVO regularMaster = null;
        List<VacationVO> regularUsages = new ArrayList<>();
        List<VacationVO> others = new ArrayList<>();

        for (VacationVO vacation : vacations) {
            if (this.isRegularMaster(vacation)) {
                regularMaster = vacation;
            } else if (this.isRegularUsage(vacation)) {
                regularUsages.add(vacation);
            } else {
                others.add(vacation);
            }
        }

        return new VacationGroups(regularMaster, regularUsages, others);
    }

    private boolean isRegularMaster(VacationVO vacation) {
        return CATEGORY_REGULAR.equals(vacation.getVacationCate())
                && !Boolean.TRUE.equals(vacation.getVacationState());
    }

    private boolean isRegularUsage(VacationVO vacation) {
        return CATEGORY_REGULAR.equals(vacation.getVacationCate())
                && Boolean.TRUE.equals(vacation.getVacationState());
    }

    private int dayCountOf(VacationVO vacation) {
        return vacation.getVacationDay() != null ? vacation.getVacationDay() : 0;
    }

    private int sumDays(List<VacationVO> vacations) {
        int sum = 0;
        for (VacationVO vacation : vacations) {
            sum += this.dayCountOf(vacation);
        }
        return sum;
    }

    // groupVacations()의 분류 결과를 담는 내부 값 객체.
    private static final class VacationGroups {
        private final VacationVO regularMaster;
        private final List<VacationVO> regularUsages;
        private final List<VacationVO> others;

        private VacationGroups(
                VacationVO regularMaster, List<VacationVO> regularUsages, List<VacationVO> others) {
            this.regularMaster = regularMaster;
            this.regularUsages = regularUsages;
            this.others = others;
        }
    }
}
