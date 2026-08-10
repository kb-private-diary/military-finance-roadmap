package org.scoula.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.job.client.QnetApiClient;
import org.scoula.job.domain.JobQualificationVO;
import org.scoula.job.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class QnetQualificationSyncServiceImpl
        implements QnetQualificationSyncService {

    private static final Pattern YEAR_PATTERN =
            Pattern.compile("(20\\d{2})년");

    private static final Pattern ROUND_PATTERN =
            Pattern.compile("(\\d+)회");

    private final JobMapper jobMapper;
    private final QnetApiClient qnetApiClient;

    @Override
    public void syncQualifications() {

        List<JobQualificationVO> qualifications =
                this.jobMapper.findQnetQualificationList();

        for (JobQualificationVO qualification : qualifications) {

            try {
                this.syncQualification(qualification);

            } catch (Exception e) {
                log.error(
                        "Q-Net 자격증 동기화 실패. qualId={}, qualName={}, externalCode={}",
                        qualification.getQualId(),
                        qualification.getQualName(),
                        qualification.getExternalCode(),
                        e
                );
            }
        }
    }

    private void syncQualification(JobQualificationVO qualification) {

        String jmCd = qualification.getExternalCode();

        if (jmCd == null || jmCd.isBlank()) {
            return;
        }

        log.info(
                "Q-Net 자격증 동기화 시작. qualId={}, qualName={}, jmCd={}",
                qualification.getQualId(),
                qualification.getQualName(),
                jmCd
        );

        // 1. 응시료 동기화
        try {
            this.syncExamFee(
                    qualification.getQualId(),
                    jmCd
            );
        } catch (Exception e) {
            log.warn(
                    "Q-Net 응시료 동기화 실패. qualId={}, qualName={}, jmCd={}",
                    qualification.getQualId(),
                    qualification.getQualName(),
                    jmCd,
                    e
            );
        }

        // 2. 시험일정 동기화
        try {
            this.syncExamSchedules(
                    qualification.getQualId(),
                    jmCd
            );
        } catch (Exception e) {
            log.warn(
                    "Q-Net 시험일정 동기화 실패. qualId={}, qualName={}, jmCd={}",
                    qualification.getQualId(),
                    qualification.getQualName(),
                    jmCd,
                    e
            );
        }


        log.info(
                "Q-Net 자격증 동기화 완료. qualId={}, qualName={}",
                qualification.getQualId(),
                qualification.getQualName()
        );
    }

    private void syncExamFee(
            Long qualId,
            String jmCd) {

        QnetApiClient.ExamFee examFee =
                this.qnetApiClient.findExamFee(jmCd);

        if (examFee.getWrittenFee() == null
                && examFee.getPracticalFee() == null) {

            log.warn(
                    "Q-Net 응시료 정보 없음. qualId={}, jmCd={}",
                    qualId,
                    jmCd
            );

            return;
        }

        this.jobMapper.updateQualificationFee(
                qualId,
                examFee.getWrittenFee(),
                examFee.getPracticalFee()
        );
    }

    private void syncExamSchedules(
            Long qualId,
            String jmCd) {

        List<QnetApiClient.ExamSchedule> schedules =
                this.qnetApiClient.findExamSchedules(jmCd);

        for (QnetApiClient.ExamSchedule schedule : schedules) {

            Integer examYear =
                    this.extractExamYear(schedule);

            String examRound =
                    this.extractExamRound(schedule.getExamRound());

            if (examYear == null || examRound == null) {

                log.warn(
                        "Q-Net 시험 회차 파싱 실패. qualId={}, implPlanNm={}",
                        qualId,
                        schedule.getExamRound()
                );

                continue;
            }

            this.jobMapper.upsertQualificationSchedule(
                    qualId,
                    examYear,
                    examRound,
                    schedule
            );
        }
    }

    private Integer extractExamYear(
            QnetApiClient.ExamSchedule schedule) {

        String examRoundName = schedule.getExamRound();

        if (examRoundName != null) {

            Matcher matcher =
                    YEAR_PATTERN.matcher(examRoundName);

            if (matcher.find()) {
                return Integer.valueOf(matcher.group(1));
            }
        }

        LocalDate date =
                schedule.getWrittenRegStartDate();

        if (date == null) {
            date = schedule.getWrittenExamStartDate();
        }

        if (date == null) {
            date = schedule.getPracticalRegStartDate();
        }

        if (date == null) {
            date = schedule.getPracticalExamStartDate();
        }

        return date == null
                ? null
                : date.getYear();
    }

    private String extractExamRound(String examRoundName) {

        if (examRoundName == null
                || examRoundName.isBlank()) {
            return null;
        }

        Matcher matcher =
                ROUND_PATTERN.matcher(examRoundName);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1) + "회";
    }
}