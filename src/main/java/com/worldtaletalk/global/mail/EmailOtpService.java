package com.worldtaletalk.global.mail;

import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpService {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender mailSender;
    private final EmailOtpRedisRepository emailOtpRedisRepository;
    private final UserService userService;

    /**
     * OTP 메일 발송
     *
     * @param userId 회원 PK
     * @param newEmail 변경할 이메일 (OTP 발송할 이메일)
     */
    public void sendOtp(Long userId, String newEmail) {
        if (userService.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if (emailOtpRedisRepository.isInCoolDown(userId)) {
            throw new IllegalStateException("잠시후 다시시도 해주세요");
        }

        String code = generateCode();

        // 레디스 캐시 저장
        emailOtpRedisRepository.saveOtp(userId, newEmail, code);

        // 메일 여러번 발송 방지 캐시 저장
        emailOtpRedisRepository.markCooldown(userId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newEmail);
        message.setSubject("[world-tale-talk] 이메일 변경 인증코드");
        message.setText("인증코드: " + code + "(4분간 유효합니다)");
        message.setFrom(from);

        // 메일 발송
        mailSender.send(message);
        log.info("이메일 변경 OTP 발송 userId : {}, newEmail : {}, OTP : {}", userId, newEmail, code);
    }

    /**
     * OTP 검증
     *
     * @param userId 회원 PK
     * @param newEmail 변경할 이메일
     * @param inputOtp 입력받은 OTP
     * @return 검증 완료 TRUE 검증 실패 FALSE
     */
    public boolean verifyOtp(Long userId, String newEmail, String inputOtp) {
        String storedOtp = emailOtpRedisRepository.getOtpCode(userId, newEmail);
        if (storedOtp == null || !storedOtp.equals(inputOtp)) return false;
        emailOtpRedisRepository.deleteOtp(userId);
        return true;
    }


    /* Helper Methode */

    /**
     * OTP 랜덤 생성 (6자리)
     *
     * @return OTP
     */
    private String generateCode() {
        SecureRandom secureRandom = new SecureRandom();
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
