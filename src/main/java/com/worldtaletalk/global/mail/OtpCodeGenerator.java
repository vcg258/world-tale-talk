package com.worldtaletalk.global.mail;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class OtpCodeGenerator {

    /**
     * OTP 랜덤 생성 (6자리)
     *
     * @return OTP
     */
    public static String generate() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
