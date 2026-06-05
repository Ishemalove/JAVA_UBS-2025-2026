package rw.utility.billing.dto;

import jakarta.validation.constraints.*;
import rw.utility.billing.enums.Role;

import java.util.Set;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 100) String fullNames,
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "email must be valid and lowercase") String email,
            @NotBlank @Pattern(regexp = ValidationPatterns.PHONE, message = "phone number must contain 10 to 15 digits and may start with +") String phoneNumber,
            @NotBlank @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD, message = "password must be at least 8 characters and contain uppercase, lowercase, digit, and special character") String password,
            @NotEmpty Set<Role> roles
    ) {}

    public record LoginRequest(
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "email must be valid and lowercase") String email,
            @NotBlank String password
    ) {}

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, boolean mustChangePassword, String message) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record VerifyEmailRequest(@NotBlank String token) {}
    public record VerifyOtpRequest(@NotBlank @Pattern(regexp = "^[0-9]{4,6}$") String otp) {}
    public record ResendOtpRequest(
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "email must be valid and lowercase") String email,
            @NotBlank @Pattern(regexp = "^(REGISTRATION|PASSWORD_RESET)$", message = "purpose must be REGISTRATION or PASSWORD_RESET") String purpose
    ) {}
    public record ForgotPasswordRequest(@NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL) String email) {}
    public record ResetPasswordRequest(
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL) String email,
            @NotBlank @Pattern(regexp = "^[0-9]{4,6}$") String otp,
            @NotBlank @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD) String newPassword
    ) {}
    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD, message = "new password must be at least 8 characters and contain uppercase, lowercase, digit, and special character") String newPassword
    ) {}
    public record MessageResponse(String message) {}
}
