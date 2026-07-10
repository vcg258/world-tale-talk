package com.worldtaletalk.user.scheduler;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantAccountConversionScheduler {
    private static final int DORMANT_INACTIVITY_DAYS = 365; // TODO 휴면상태 기간 필요시 수정
    private final UserRepository userRepository;


    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(DORMANT_INACTIVITY_DAYS);
            List<UserEntity> targets = userRepository.findAllByStatusAndLastLoginAtBefore(UserStatus.ACTIVE, cutoff);

            for (UserEntity target : targets) {
                target.changeStatus(UserStatus.DORMANT);
            }
            userRepository.saveAll(targets);
            log.info("휴면 계정 자동 전환 완료 - {}건", targets.size());
        } catch (Exception e) {
            log.error("휴면 계정 자동 전환 실패", e);
        }
    }
}
