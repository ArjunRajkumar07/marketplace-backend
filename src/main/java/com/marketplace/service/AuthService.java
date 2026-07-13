package com.marketplace.service;

import com.marketplace.dao.OtpRepository;
import com.marketplace.dao.UserRepository;
import com.marketplace.model.OtpVerification;
import com.marketplace.model.User;
import com.marketplace.util.JwtUtil;
import com.marketplace.util.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public String register(String name, String email,
                           String phone, String password,
                           User.Role role) {

        if (userRepository.existsByEmail(email)) {
            return "Email already registered";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setIsVerified(false);

        if (role == User.Role.SELLER) {
            user.setIsApproved(false);
        } else {
            user.setIsApproved(true);
        }

        userRepository.save(user);
        sendOtp(email);

        return "Registration successful. OTP sent to your email.";
    }

    public void sendOtp(String email) {

        String otp = otpUtil.generateOtp();

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpVerification.setIsUsed(false);
        otpRepository.save(otpVerification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your OTP - Marketplace");
        message.setText("Your OTP is: " + otp +
                        "\n\nThis OTP is valid for 10 minutes." +
                        "\n\nDo not share this OTP with anyone.");
        mailSender.send(message);
    }

    public String verifyOtp(String email, String otpCode) {

        Optional<OtpVerification> otpOpt =
                otpRepository.findTopByEmailOrderByOtpIdDesc(email);

        if (otpOpt.isEmpty()) {
            return "OTP not found. Please request a new OTP.";
        }

        OtpVerification otp = otpOpt.get();

        if (otp.getIsUsed()) {
            return "OTP already used. Please request a new OTP.";
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "OTP expired. Please request a new OTP.";
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            return "Invalid OTP. Please try again.";
        }

        otp.setIsUsed(true);
        otpRepository.save(otp);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsVerified(true);
            userRepository.save(user);
        }

        return "Email verified successfully. You can now login.";
    }

    public String login(String email, String password) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found. Please register first.";
        }

        User user = userOpt.get();

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            return "Email not verified. Please verify your OTP first.";
        }

        if (user.getRole() == User.Role.SELLER && !Boolean.TRUE.equals(user.getIsApproved())) {
            return "Your seller account is pending admin approval.";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "Incorrect password. Please try again.";
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
    }

    public String googleLogin(String googleId, String email,
                               String name, User.Role role) {

        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            return jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setRole(role);
        user.setIsVerified(true);
        user.setIsApproved(role == User.Role.BUYER);

        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
    }

    public String resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "User not found";
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password reset successfully";
    }

    public String saveIdProof(String email, String idProofUrl, String idProofType) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "User not found";
        }
        User user = userOpt.get();
        user.setIdProofUrl(idProofUrl);
        user.setIdProofType(idProofType);
        userRepository.save(user);
        return "ID proof saved successfully";
    }

    public String checkOtp(String email, String otpCode) {
        Optional<OtpVerification> otpOpt =
            otpRepository.findTopByEmailOrderByOtpIdDesc(email);

        if (otpOpt.isEmpty()) return "OTP not found. Please request a new OTP.";

        OtpVerification otp = otpOpt.get();

        if (otp.getIsUsed()) return "OTP already used. Please request a new OTP.";
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) return "OTP expired. Please request a new OTP.";
        if (!otp.getOtpCode().equals(otpCode)) return "Invalid OTP. Please try again.";

        return "OTP verified successfully.";
    }

    // ── SAVE BANK DETAILS ─────────────────────────────────────
    public String saveBankDetails(String email, String accountHolderName,
                                   String bankName, String accountNumber,
                                   String ifscCode, String upiId) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "User not found";
        }
        User user = userOpt.get();
        user.setAccountHolderName(accountHolderName);
        user.setBankName(bankName);
        user.setAccountNumber(accountNumber);
        user.setIfscCode(ifscCode);
        user.setUpiId(upiId);
        userRepository.save(user);
        return "Bank details saved successfully";
    }
}