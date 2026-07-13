package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.marketplace.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://marketplace-frontend-theta-seven.vercel.app"
    })
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ── REGISTER ─────────────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String name     = request.get("name");
        String email    = request.get("email");
        String phone    = request.get("phone");
        String password = request.get("password");
        String roleStr  = request.get("role");
        User.Role role  = User.Role.valueOf(roleStr.toUpperCase());
        String result   = authService.register(name, email, phone, password, role);
        return ResponseEntity.ok(result);
    }

    // ── VERIFY OTP ───────────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/verify-otp
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String email   = request.get("email");
        String otpCode = request.get("otpCode");
        String result  = authService.verifyOtp(email, otpCode);
        return ResponseEntity.ok(result);
    }

    // ── RESEND OTP ───────────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/resend-otp
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.sendOtp(email);
        return ResponseEntity.ok("OTP resent successfully");
    }

    // ── LOGIN ────────────────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String email    = request.get("email");
        String password = request.get("password");
        String result   = authService.login(email, password);
        return ResponseEntity.ok(result);
    }

    // ── GOOGLE LOGIN ─────────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/google-login
    @PostMapping("/google-login")
    public ResponseEntity<String> googleLogin(@RequestBody Map<String, String> request) {
        String googleId = request.get("googleId");
        String email    = request.get("email");
        String name     = request.get("name");
        String roleStr  = request.get("role");
        User.Role role  = User.Role.valueOf(roleStr.toUpperCase());
        String result   = authService.googleLogin(googleId, email, name, role);
        return ResponseEntity.ok(result);
    }

    // ── UPLOAD ID PROOF ──────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/upload-id-proof
    @PostMapping("/upload-id-proof")
    public ResponseEntity<String> uploadIdProof(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email,
            @RequestParam("idProofType") String idProofType) {
        try {
            String url = cloudinaryService.uploadFile(file, "id-proofs");
            authService.saveIdProof(email, url, idProofType);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // ── SAVE BANK DETAILS ────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/save-bank-details
    @PostMapping("/save-bank-details")
    public ResponseEntity<String> saveBankDetails(@RequestBody Map<String, String> request) {
        String email              = request.get("email");
        String accountHolderName  = request.get("accountHolderName");
        String bankName           = request.get("bankName");
        String accountNumber      = request.get("accountNumber");
        String ifscCode           = request.get("ifscCode");
        String upiId              = request.get("upiId");
        String result = authService.saveBankDetails(
            email, accountHolderName, bankName, accountNumber, ifscCode, upiId
        );
        return ResponseEntity.ok(result);
    }

    // ── CHECK OTP (without consuming) ───────────────────────
    // URL : POST http://localhost:8080/api/auth/check-otp
    @PostMapping("/check-otp")
    public ResponseEntity<String> checkOtp(@RequestBody Map<String, String> request) {
        String email   = request.get("email");
        String otpCode = request.get("otpCode");
        String result  = authService.checkOtp(email, otpCode);
        return ResponseEntity.ok(result);
    }

    // ── RESET PASSWORD ───────────────────────────────────────
    // URL : POST http://localhost:8080/api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email       = request.get("email");
        String newPassword = request.get("newPassword");
        String result      = authService.resetPassword(email, newPassword);
        return ResponseEntity.ok(result);
    }

    // ── TEMPORARY — remove after use ─────────────────────────
    @GetMapping("/hash/{password}")
    public ResponseEntity<String> hashPassword(@PathVariable String password) {
        return ResponseEntity.ok(passwordEncoder.encode(password));
    }
}