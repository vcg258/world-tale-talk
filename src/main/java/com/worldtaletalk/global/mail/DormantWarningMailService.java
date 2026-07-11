package com.worldtaletalk.global.mail;

import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DormantWarningMailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 휴면 계정 통보 메일 안내
     *
     * @param email 보낼 메일
     */
    public void sendWarning(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("해당 이메일은 존재하지 않습니다.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[world-tale-talk] 휴면 계정 전환 예정 안내");
        message.setText("장기간 로그인 기록이 없어 휴면 계정으로 전환될 예정입니다. 계속 이용하시려면 로그인해주세요.");
        message.setFrom(from);
        mailSender.send(message);
    }
}
