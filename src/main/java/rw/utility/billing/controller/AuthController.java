package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.AuthDtos.*;
import rw.utility.billing.exception.BadRequestException;
import rw.utility.billing.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "01 Authentication and Verification", description = "Public registration/login, email verification, OTP, refresh, logout, and password recovery")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @Operation(summary = "Public: register user and send email verification token plus OTP")
    @SecurityRequirements
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Public: login verified active user and receive JWT plus refresh token")
    @SecurityRequirements
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Public: verify account through email token")
    @SecurityRequirements
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return auth.verifyEmail(request.token());
    }

    @PostMapping("/verify-otp/{email}")
    @Operation(summary = "Public: verify latest active OTP for this email")
    @SecurityRequirements
    public MessageResponse verifyOtp(@PathVariable String email, @Valid @RequestBody VerifyOtpRequest request) {
        return auth.verifyOtp(email, request);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Public: resend OTP for REGISTRATION or PASSWORD_RESET using a valid lowercase email")
    @SecurityRequirements
    public MessageResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return auth.resendOtp(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Public: exchange refresh token for a new JWT")
    @SecurityRequirements
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return auth.refresh(request.refreshToken());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Public: send password reset OTP")
    @SecurityRequirements
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return auth.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Public: reset password using OTP")
    @SecurityRequirements
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return auth.resetPassword(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Authenticated users: sign out by blacklisting current JWT")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new BadRequestException("Authorization Bearer token is required for logout.");
        return ResponseEntity.ok(auth.logout(header.substring(7)));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Authenticated users: change password; required for admin-created temporary passwords")
    public MessageResponse changePassword(HttpServletRequest servletRequest, @Valid @RequestBody ChangePasswordRequest request) {
        return auth.changePassword(servletRequest.getUserPrincipal().getName(), request);
    }
}
