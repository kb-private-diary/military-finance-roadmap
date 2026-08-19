package org.scoula.travel.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.push.service.PushNotificationService;
import org.scoula.travel.dto.TravelNotificationTargetDTO;
import org.scoula.travel.mapper.TravelMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class TravelNotificationServiceImpl implements TravelNotificationService {
    private static final int NOTIFICATION_START_DAYS = 7;
    private static final int NOTIFICATION_END_DAYS = 1;
    private static final String CATEGORY_DOMESTIC = "TRAVEL_DOMESTIC";
    private static final String CATEGORY_OVERSEAS = "TRAVEL_OVERSEAS";

    private final TravelMapper mapper;
    private final PushNotificationService pushNotificationService;

    @Override
    public void sendUpcomingTravelNotifications() {
        final LocalDate today = LocalDate.now();
        final List<TravelNotificationTargetDTO> targets =
                this.mapper.findUpcomingConfirmedGoalList(
                        today.plusDays(NOTIFICATION_END_DAYS),
                        today.plusDays(NOTIFICATION_START_DAYS));

        int sentCount = 0;
        for (TravelNotificationTargetDTO target : targets) {
            final long remainingDays = ChronoUnit.DAYS.between(
                    today, target.getStartDate());
            final boolean domestic = Boolean.TRUE.equals(target.getDomestic());
            final String category = domestic
                    ? CATEGORY_DOMESTIC : CATEGORY_OVERSEAS;
            final String title = this.createTitle(domestic, remainingDays);
            final String body = this.createBody(domestic, target.getDestination());

            if (this.mapper.countSuccessfulTravelNotification(
                    target.getUserId(), title, body, category, today) > 0) {
                continue;
            }

            this.pushNotificationService.send(
                    target.getUserId(), title, body, category,
                    "/travel/goals/" + target.getGoalId());
            sentCount++;
        }

        log.info("여행 출발 알림 배치 완료 - 대상 {}건, 발송 요청 {}건",
                targets.size(), sentCount);
    }

    private String createTitle(final boolean domestic, final long remainingDays) {
        final String travelType = domestic ? "국내 여행" : "해외여행";
        return travelType + "이 " + remainingDays + "일 남았어요!";
    }

    private String createBody(final boolean domestic, final String destination) {
        if (domestic) {
            return destination + " 여행의 교통편과 숙소 예약을 확인해 주세요.";
        }
        return destination
                + " 여행의 여행자 보험, 여권, 여행 전용 카드를 확인해 주세요.";
    }
}
