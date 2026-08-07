package org.scoula.member.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.scoula.common.util.RankCalculator;
import org.scoula.member.dto.UserRankInfoDTO;
import org.scoula.member.mapper.RankMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class RankPromotionServiceImpl implements RankPromotionService {

    private static final String UPDATED_BY = "rank-scheduler";

    private final RankMapper mapper;

    @Transactional
    @Override
    public void promoteRanks() {
        LocalDate today = LocalDate.now();
        List<UserRankInfoDTO> users = this.mapper.findActiveEnlistedUsers();

        int promotedCount = 0;
        for (UserRankInfoDTO user : users) {
            int monthsSinceEnlist = RankCalculator.monthsSinceEnlist(user.getEnlistDate(), today);
            Integer newRankId = this.mapper.findRankIdByServiceMonths(monthsSinceEnlist);
            if (newRankId != null && !newRankId.equals(user.getRankId())) {
                this.mapper.updateRankId(user.getId(), newRankId, UPDATED_BY);
                promotedCount++;
            }
        }
        log.info("계급 진급 배치 완료 - 대상 {}명, 진급 {}명", users.size(), promotedCount);
    }
}
