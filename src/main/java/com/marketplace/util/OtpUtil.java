package com.marketplace.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {

    // SecureRandom is safer than Random for generating OTPs
    private static final SecureRandom random = new SecureRandom();

    // Generates a 6 digit OTP like 482916
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}