package com.worldtaletalk.global.mail;

import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindIdService {
    private final UserService userService;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 아이디 찾기 메일 전송 (아이디 찾기 기능)
     *
     * @param email 이메일
     */
    public void sendLoginId(String email) {
        UserDTO user = userService.getUserByEmail(email);

        if (user == null || user.getLoginId() == null) {
            log.warn("아이디 찾기 요청 - 조건 불일치 (email: {})", email);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[world-tale-talk] 아이디 찾기 안내");
        message.setText("회원님의 아이디는 [" + user.getLoginId() + "] 입니다.");
        message.setFrom(from);
        javaMailSender.send(message);

        log.info("아이디 찾기 메일 발송 userId : {}", user.getId());
    }
}
