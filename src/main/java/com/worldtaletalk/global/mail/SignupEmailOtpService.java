package com.worldtaletalk.global.mail;

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
public class SignupEmailOtpService {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender mailSender;
    private final SignupEmailOtpRedisRepository signupEmailOtpRedisRepository;
    private final UserService userService;

    /**
     * 회원가입 이메일 본인확인 메일 발송
     *
     * @param email 발송할 이메일
     */
    public void signupSendOtp(String email) {
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if (signupEmailOtpRedisRepository.isInCooldown(email)) {
            throw new IllegalStateException("잠시후 다시 시도 해주세요.");
        }

        String otp = OtpCodeGenerator.generate();

        // 캐시값 저장
        signupEmailOtpRedisRepository.saveOtp(email, otp);

        // 메일 발송 완료 캐시값 (연속 발송 방지)
        signupEmailOtpRedisRepository.markCooldown(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[world-tale-talk] 본인확인 이메일 인증코드");
        message.setText("인증코드: " + otp + "(4분간 유효합니다)");
        message.setFrom(from);

        mailSender.send(message);
    }

    /**
     * OTP 유효성 검사
     *
     * @param email 발송한 이메일
     * @param inputOtp 입력받은 OTP
     * @return 검사 여부
     */
    public boolean verifyOtp(String email, String inputOtp) {
        String otp = signupEmailOtpRedisRepository.getOtp(email);
        if (otp == null || !otp.equals(inputOtp)) return false;

        // 기존 OTP 캐시값 제거
        signupEmailOtpRedisRepository.deleteOtp(email);
        // 인증 완료시 완료여부 캐시값 저장
        signupEmailOtpRedisRepository.markVerified(email);

        return true;
    }

    /**
     * OTP인증완료 캐시값 검증
     *
     * @param email 이메일
     * @return 검증 여부
     */
    public boolean isVerified(String email) {
        return signupEmailOtpRedisRepository.isVerified(email);
    }
}
