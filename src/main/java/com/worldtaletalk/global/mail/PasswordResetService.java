package com.worldtaletalk.global.mail;

import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserService userService;
    private final PasswordResetRedisRepository passwordResetRedisRepository;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 비밀번호 재설정 메일 발송 (비밀번호 찾기 기능)
     *
     * @param loginId 회원 로그인 아이디
     * @param email 이메일
     */
    public void sendResetLink(String loginId, String email) {
        // 로그인 아이디, 이메일 동시 조회
        UserDTO user = userService.getUserByLoginIdAndEmail(loginId, email);

        if (user == null || user.getPassword() == null) {
            log.warn("비밀번호 재설정 요청 - 조건 불일치 (loginId: {})", loginId);
            return;
        }

        String token = UUID.randomUUID().toString();
        passwordResetRedisRepository.saveToken(token, user.getId());

        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token; // TODO 도메인 설정시 변경 필요

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[world-tale-talk] 비밀번호 재설정 안내");
        message.setText("아래 링크를 클릭해 새 비밀번호를 설정해주세요 (30분간 유효합니다)\n" + resetUrl);
        message.setFrom(from);
        javaMailSender.send(message);

        log.info("비밀번호 재설정 링크 발송 userId : {}", user.getId());
    }

    /**
     * 재설정 링크 만료 검증
     *
     * @param token 토큰
     * @return 검증된 회원 PK
     */
    public Long verifyToken(String token) {
        Long userId = passwordResetRedisRepository.getUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 링크입니다.");
        }
        return userId;
    }

    /**
     * 링크 사용후 제거
     *
     * @param token 토큰
     */
    public void consumeToken(String token) {
        passwordResetRedisRepository.deleteToken(token);
    }
}
