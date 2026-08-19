package org.scoula.dashboard.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.common.exception.BusinessException;
import org.scoula.common.util.MilitarySavingsCalculator;
import org.scoula.common.util.MilitarySavingsCalculator.CalcResult;
import org.scoula.saving.mapper.MilitarySavingProductMapper;
import org.scoula.saving.util.MilitarySavingRateResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.dashboard.domain.VacationHistoryVO;
import org.scoula.dashboard.domain.VacationVO;
import org.scoula.dashboard.dto.DashboardBasicResponseDTO;
import org.scoula.dashboard.dto.DashboardDDayUserDTO;
// openbanking 패키지에서 VO 객체 import (향후 교체)
import org.scoula.dashboard.dto.DashboardSavingAccountDTO;
import org.scoula.dashboard.dto.DashboardSavingHistoryDTO;
import org.scoula.dashboard.dto.DashboardSavingsResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationCreateRequestDTO;
import org.scoula.dashboard.dto.DashboardVacationDetailResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationItemDTO;
import org.scoula.dashboard.dto.DashboardVacationListResponseDTO;
import org.scoula.dashboard.dto.DashboardVacationUsageCreateRequestDTO;
import org.scoula.dashboard.dto.DashboardVacationUsageDTO;
import org.scoula.dashboard.mapper.DashboardMapper;
import org.scoula.push.service.PushNotificationService;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardServiceImpl implements DashboardService {
    // 정기휴가(연가) 카테고리 코드. 가입 시 서버가 자동 부여하며, 이 카테고리는 직접 등록·수정·삭제할 수 없다.
    private static final String CATEGORY_REGULAR = "REGULAR";

    // 허용되는 휴가 카테고리 전체 목록.
    private static final Set<String> VALID_CATEGORIES =
            Set.of(CATEGORY_REGULAR, "REWARD", "CONSOLATION", "PETITION", "ETC");

    // 전역일이 없을 때(연동 전 등) 쓰는 폴백 복무기간(개월). SimulatorServiceImpl과 동일 값으로 통일.
    private static final int DEFAULT_SERVICE_MONTHS = 24;

    // D-Day 웹푸시 발송 기준(전역까지 남은 일수)
    private static final long DDAY_30 = 30L;
    private static final long DDAY_1 = 1L;
    private static final String CATEGORY_DDAY = "DDAY";

    // DashboardSavingAccountDTO account -> SavingAccountVO account 교체
    // VO getter 가 같은지 확인 필요(getOpenDate(), getMonthlySave() 등)
    private final DashboardMapper mapper;
    private final MilitarySavingProductMapper militarySavingProductMapper;
    private final PushNotificationService pushNotificationService;

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
            dischargeDate = LocalDate.now().plusMonths(DEFAULT_SERVICE_MONTHS); // fallback
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
                        account.getOpenDate(),
                        monthlySave,
                        dischargeDate,
                        histories,
                        rateResolver
                );

                // 4. 총 원금·이자·매칭지원금 합산 (비과세 처리라 세금은 반영하지 않음)
                expectedMaturityTotal += calc.getTotalPrincipal()
                        + (long) calc.getTotalInterest() + calc.matchingFund;
            }
        }
        
        return new DashboardSavingsResponseDTO(currentTotalSavings, expectedMaturityTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVacationListResponseDTO findVacations(Long userId) {
        List<VacationVO> vacations = this.mapper.findVacationListByUserId(userId);

        int totalDays = 0;
        int usedDays = 0;
        List<DashboardVacationItemDTO> items = new ArrayList<>();
        for (VacationVO vacation : vacations) {
            int days = this.dayCountOf(vacation);
            int used = this.sumUsedDays(vacation.getVacationId());
            totalDays += days;
            usedDays += used;
            items.add(DashboardVacationItemDTO.of(vacation, days - used));
        }

        // REGULAR는 항상 맨 앞에 고정. 그 다음은 사용완료(isUsed=true) 카드가 뒤로 가고,
        // 안정정렬이라 같은 그룹 안에서는 위에서 쌓인 획득일 최신순이 그대로 유지된다.
        items.sort(Comparator.comparing(this::isNonRegular)
                .thenComparing(DashboardVacationItemDTO::getIsUsed));

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

        List<VacationHistoryVO> histories =
                this.mapper.findVacationHistoryListByVacationId(vacationId);
        List<DashboardVacationUsageDTO> usages =
                histories.stream().map(DashboardVacationUsageDTO::of).toList();

        int remainingDays = this.dayCountOf(vacation) - this.sumHistoryDays(histories);

        return DashboardVacationDetailResponseDTO.of(vacation, remainingDays, usages);
    }

    @Override
    @Transactional
    public Long createVacation(
            Long userId, String createdNm, DashboardVacationCreateRequestDTO request) {
        String category = request.getCategory();
        if (!VALID_CATEGORIES.contains(category)) {
            throw BusinessException.badRequest("유효하지 않은 휴가 카테고리입니다.", "DASH_005");
        }
        if (CATEGORY_REGULAR.equals(category)) {
            throw BusinessException.badRequest("정기휴가는 직접 등록할 수 없습니다.", "DASH_008");
        }

        VacationVO vacation = VacationVO.builder()
                .userId(userId)
                .vacationCate(category)
                .vacationName(request.getName())
                .vacationGet(request.getAcquiredDate())
                .vacationDay(request.getDays())
                .build();
        vacation.setCreatedNm(createdNm);

        this.mapper.insertVacation(vacation);

        return vacation.getVacationId();
    }

    @Override
    @Transactional
    public void updateVacation(
            Long userId, Long vacationId, String modifiedNm,
            DashboardVacationCreateRequestDTO request) {
        VacationVO vacation = this.mapper.findVacationById(vacationId, userId);
        if (vacation == null) {
            throw BusinessException.notFound("휴가 정보를 찾을 수 없습니다.", "DASH_003");
        }
        // REGULAR는 가입 시 자동 부여되며 이 API로 수정할 수 없다.
        if (CATEGORY_REGULAR.equals(vacation.getVacationCate())) {
            throw BusinessException.badRequest("정기휴가는 이 API로 수정할 수 없습니다.", "DASH_006");
        }

        String category = request.getCategory();
        if (!VALID_CATEGORIES.contains(category) || CATEGORY_REGULAR.equals(category)) {
            throw BusinessException.badRequest("유효하지 않은 휴가 카테고리입니다.", "DASH_005");
        }
        int usedDays = this.sumUsedDays(vacationId);
        if (request.getDays() < usedDays) {
            throw BusinessException.badRequest(
                    "이미 사용한 일수(" + usedDays + "일)보다 적게 설정할 수 없습니다.", "DASH_010");
        }

        vacation.setVacationCate(category);
        vacation.setVacationName(request.getName());
        vacation.setVacationGet(request.getAcquiredDate());
        vacation.setVacationDay(request.getDays());
        vacation.setModifiedNm(modifiedNm);

        this.mapper.updateVacation(vacation);
    }

    @Override
    @Transactional
    public void deleteVacation(Long userId, Long vacationId, String modifiedNm) {
        VacationVO vacation = this.mapper.findVacationById(vacationId, userId);
        if (vacation == null) {
            throw BusinessException.notFound("휴가 정보를 찾을 수 없습니다.", "DASH_003");
        }
        // REGULAR는 입대 시 고정 부여된 총량이라 삭제 대상이 아니다.
        if (CATEGORY_REGULAR.equals(vacation.getVacationCate())) {
            throw BusinessException.badRequest("정기휴가는 삭제할 수 없습니다.", "DASH_007");
        }

        this.mapper.deleteVacation(vacationId, modifiedNm);
    }

    @Override
    @Transactional
    public Long createVacationUsage(
            Long userId, String createdNm, Long vacationId,
            DashboardVacationUsageCreateRequestDTO request) {
        VacationVO vacation = this.mapper.findVacationById(vacationId, userId);
        if (vacation == null) {
            throw BusinessException.notFound("휴가 정보를 찾을 수 없습니다.", "DASH_003");
        }

        if (request.getUsedDate().isBefore(vacation.getVacationGet())) {
            throw BusinessException.badRequest("사용일은 휴가 획득일보다 이전일 수 없습니다.", "DASH_011");
        }

        int remainingDays = this.dayCountOf(vacation) - this.sumUsedDays(vacationId);
        if (request.getDays() > remainingDays) {
            throw BusinessException.badRequest("휴가 잔여일수를 초과했습니다.", "DASH_004");
        }

        VacationHistoryVO history = VacationHistoryVO.builder()
                .vacationId(vacationId)
                .usedDate(request.getUsedDate())
                .usedDay(request.getDays())
                .build();
        history.setCreatedNm(createdNm);

        this.mapper.insertVacationHistory(history);

        return history.getHistoryId();
    }

    @Override
    @Transactional
    public void deleteVacationUsage(Long userId, Long historyId, String modifiedNm) {
        VacationHistoryVO history = this.mapper.findVacationHistoryById(historyId, userId);
        if (history == null) {
            throw BusinessException.notFound("사용내역을 찾을 수 없습니다.", "DASH_009");
        }

        this.mapper.deleteVacationHistory(historyId, modifiedNm);
    }

    // 조회 자체는 읽기전용이지만, 결과에 따라 send()가 커밋 후 push_history에 쓰기 때문에
    // readOnly로 두면 커넥션이 읽기전용으로 묶여 그 쓰기가 실패한다.
    @Transactional
    @Override
    public void sendDDayNotifications() {
        LocalDate today = LocalDate.now();
        List<DashboardDDayUserDTO> users = this.mapper.findActiveUsersWithDischargeDate();

        int sentCount = 0;
        for (DashboardDDayUserDTO user : users) {
            long daysUntilDischarge = ChronoUnit.DAYS.between(today, user.getDischargeDate());
            if (daysUntilDischarge == DDAY_30) {
                this.pushNotificationService.send(
                        user.getUserId(), "전역 D-30", "전역까지 30일 남았어요!", CATEGORY_DDAY);
                sentCount++;
            } else if (daysUntilDischarge == DDAY_1) {
                this.pushNotificationService.send(
                        user.getUserId(), "전역 D-1", "내일이면 전역입니다!", CATEGORY_DDAY);
                sentCount++;
            }
        }
        log.info("D-Day 알림 배치 완료 - 대상 {}명, 발송 {}건", users.size(), sentCount);
    }

    private int sumUsedDays(Long vacationId) {
        return this.sumHistoryDays(this.mapper.findVacationHistoryListByVacationId(vacationId));
    }

    private int sumHistoryDays(List<VacationHistoryVO> histories) {
        int sum = 0;
        for (VacationHistoryVO history : histories) {
            sum += history.getUsedDay() != null ? history.getUsedDay() : 0;
        }
        return sum;
    }

    private int dayCountOf(VacationVO vacation) {
        return vacation.getVacationDay() != null ? vacation.getVacationDay() : 0;
    }

    private boolean isNonRegular(DashboardVacationItemDTO item) {
        return !CATEGORY_REGULAR.equals(item.getCategory());
    }
}
