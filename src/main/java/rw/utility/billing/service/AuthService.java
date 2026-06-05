package rw.utility.billing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.AuthDtos.*;
import rw.utility.billing.entity.*;
import rw.utility.billing.enums.*;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.*;
import rw.utility.billing.security.JwtService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final CustomerRepository customers;
    private final VerificationTokenRepository verificationTokens;
    private final OtpTokenRepository otps;
    private final RefreshTokenRepository refreshTokens;
    private final BlacklistedTokenRepository blacklistedTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final long refreshDays;
    private final long otpMinutes;

    public AuthService(UserRepository users, CustomerRepository customers, VerificationTokenRepository verificationTokens, OtpTokenRepository otps,
                       RefreshTokenRepository refreshTokens, BlacklistedTokenRepository blacklistedTokens,
                       PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwtService,
                       EmailService emailService, @Value("${app.jwt.refresh-days}") long refreshDays,
                       @Value("${app.otp.minutes}") long otpMinutes) {
        this.users = users;
        this.customers = customers;
        this.verificationTokens = verificationTokens;
        this.otps = otps;
        this.refreshTokens = refreshTokens;
        this.blacklistedTokens = blacklistedTokens;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.refreshDays = refreshDays;
        this.otpMinutes = otpMinutes;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) throw new BadRequestException("Email is already registered.");
        if (users.existsByPhoneNumber(request.phoneNumber())) throw new BadRequestException("Phone number is already registered.");
        boolean customerRole = request.roles().contains(Role.ROLE_CUSTOMER);
        if (!customerRole && customers.existsByEmail(request.email())) throw new BadRequestException("This email is already registered as a customer.");
        if (!customerRole && customers.existsByPhoneNumber(request.phoneNumber())) throw new BadRequestException("This phone number is already registered as a customer.");
        if (customerRole && !customers.existsByEmail(request.email())) throw new BadRequestException("A customer account must use the same email as an existing customer profile.");
        User user = new User();
        user.setFullNames(request.fullNames().trim());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(encoder.encode(request.password()));
        user.setRoles(request.roles());
        user.setStatus(AccountStatus.INACTIVE);
        users.save(user);
        String token = createVerificationToken(user);
        String otp = createOtp(user, "REGISTRATION");
        emailService.sendWelcome(user.getEmail(), user.getFullNames());
        emailService.sendVerification(user.getEmail(), token, otp);
        return new MessageResponse("Registration successful. Verify your email link/token and OTP before login.");
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = users.findByEmail(request.email()).orElseThrow(() -> new NotFoundException("User not found."));
        String access = jwtService.generateAccessToken(user);
        String message = user.isMustChangePassword()
                ? "Login successful. You must change your temporary password before continuing with system duties."
                : "Login successful.";
        return new TokenResponse(access, createRefreshToken(user), "Bearer", user.isMustChangePassword(), message);
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken vt = verificationTokens.findByTokenAndUsedFalse(token).orElseThrow(() -> new BadRequestException("Verification token is invalid or already used."));
        if (vt.getExpiresAt().isBefore(LocalDateTime.now())) throw new BadRequestException("Verification token has expired.");
        vt.setUsed(true);
        vt.getUser().setEmailVerified(true);
        users.save(vt.getUser());
        return new MessageResponse("Email verified successfully. Complete OTP verification to activate the account.");
    }

    @Transactional
    public MessageResponse verifyOtp(String email, VerifyOtpRequest request) {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found."));
        OtpToken otp = otps.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No active OTP was found for this email. Request a new OTP and try again."));
        if (!otp.getCode().equals(request.otp())) throw new BadRequestException("OTP code is incorrect.");
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) throw new BadRequestException("OTP has expired.");
        otp.setUsed(true);
        if ("REGISTRATION".equals(otp.getPurpose())) {
            user.setEmailVerified(true);
            user.setStatus(AccountStatus.ACTIVE);
        }
        users.save(user);
        return new MessageResponse("OTP verified successfully.");
    }

    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        User user = users.findByEmail(request.email()).orElseThrow(() -> new NotFoundException("No user account exists for this email."));
        if ("REGISTRATION".equals(request.purpose())) {
            if (user.getStatus() == AccountStatus.ACTIVE && user.isEmailVerified()) {
                throw new BadRequestException("This account is already verified and active. Login instead of requesting another OTP.");
            }
            String otp = createOtp(user, "REGISTRATION");
            emailService.sendVerification(user.getEmail(), user.isEmailVerified() ? "Email already verified" : createVerificationToken(user), otp);
            return new MessageResponse("A new registration OTP has been sent to " + user.getEmail() + ".");
        }
        String otp = createOtp(user, "PASSWORD_RESET");
        emailService.sendPasswordReset(user.getEmail(), otp);
        return new MessageResponse("A new password reset OTP has been sent to " + user.getEmail() + ".");
    }

    @Transactional
    public TokenResponse refresh(String token) {
        RefreshToken rt = refreshTokens.findByTokenAndRevokedFalse(token).orElseThrow(() -> new BadRequestException("Refresh token is invalid."));
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) throw new BadRequestException("Refresh token has expired.");
        return new TokenResponse(jwtService.generateAccessToken(rt.getUser()), token, "Bearer", rt.getUser().isMustChangePassword(),
                rt.getUser().isMustChangePassword() ? "Refresh successful. Temporary password change is still required." : "Refresh successful.");
    }

    @Transactional
    public MessageResponse logout(String accessToken) {
        BlacklistedToken blacklisted = new BlacklistedToken();
        blacklisted.setToken(accessToken);
        blacklisted.setExpiresAt(jwtService.expiresAt(accessToken).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        blacklistedTokens.save(blacklisted);
        return new MessageResponse("Signed out successfully. The current JWT is now blacklisted.");
    }

    @Transactional
    public MessageResponse forgotPassword(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("No user account exists for this email."));
        String otp = createOtp(user, "PASSWORD_RESET");
        emailService.sendPasswordReset(email, otp);
        return new MessageResponse("Password reset OTP has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = users.findByEmail(request.email()).orElseThrow(() -> new NotFoundException("User not found."));
        OtpToken otp = otps.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, "PASSWORD_RESET")
                .orElseThrow(() -> new BadRequestException("Password reset OTP is invalid."));
        if (!otp.getCode().equals(request.otp()) || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset OTP is incorrect or expired.");
        }
        otp.setUsed(true);
        user.setPassword(encoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        users.save(user);
        return new MessageResponse("Password reset successfully.");
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found."));
        if (!encoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (encoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password.");
        }
        user.setPassword(encoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        users.save(user);
        return new MessageResponse("Password changed successfully.");
    }

    private String createVerificationToken(User user) {
        VerificationToken vt = new VerificationToken();
        vt.setUser(user);
        vt.setToken(UUID.randomUUID().toString());
        vt.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationTokens.save(vt);
        return vt.getToken();
    }

    private String createOtp(User user, String purpose) {
        String code = "%06d".formatted(new Random().nextInt(1_000_000));
        OtpToken otp = new OtpToken();
        otp.setUser(user);
        otp.setCode(code);
        otp.setPurpose(purpose);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpMinutes));
        otps.save(otp);
        return code;
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID() + "." + UUID.randomUUID());
        rt.setExpiresAt(LocalDateTime.now().plusDays(refreshDays));
        refreshTokens.save(rt);
        return rt.getToken();
    }
}
