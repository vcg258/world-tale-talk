package com.worldtaletalk.user.scheduler;

import com.worldtaletalk.global.mail.DormantWarningMailService;
import com.worldtaletalk.user.domain.DormantNotificationsEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantMailScheduler {
    private final UserRepository userRepository;
    private final DormantWarningMailService dormantWarningMailService;
    private final DormantNotificationsRepository dormantNotificationsRepository;

    @Value("${app.dormant.inactivity-days}")
    private int inactivityDays;
    @Value("${app.dormant.warning-before-days}")
    private int warningBeforeDays;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(inactivityDays - warningBeforeDays);
            List<UserEntity> targets = userRepository.findAllByLastLoginAtAndNotification(UserStatus.ACTIVE, cutoff);

            for (UserEntity target : targets) {
                String email = target.getEmail();
                try {
                    dormantWarningMailService.sendWarning(email);
                    dormantNotificationsRepository.save(
                            DormantNotificationsEntity.builder().user(target).build()
                    );
                } catch (Exception e) {
                    log.warn("이메일 발송 실패", e);
                }
            }
            log.info("휴면 계정 메일 발송 완료 - {}건", targets.size());
        } catch (Exception e) {
            log.error("휴면 계정 메일 발송 실패", e);
        }
    }
}
