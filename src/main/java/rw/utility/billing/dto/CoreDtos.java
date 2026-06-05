package rw.utility.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import rw.utility.billing.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CoreDtos {
    public record UserRequest(
            @NotBlank @Size(min = 2, max = 100) String fullNames,
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL) String email,
            @NotBlank @Pattern(regexp = ValidationPatterns.PHONE) String phoneNumber,
            @NotNull AccountStatus status,
            @NotEmpty Set<Role> roles
    ) {}
    public record AdminCreateUserRequest(
            @NotBlank @Size(min = 2, max = 100) String fullNames,
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL) String email,
            @NotBlank @Pattern(regexp = ValidationPatterns.PHONE) String phoneNumber,
            @NotEmpty Set<Role> roles
    ) {}
    public record UserResponse(UUID id, String fullNames, String email, String phoneNumber, AccountStatus status, boolean emailVerified, boolean mustChangePassword, Set<Role> roles) {}

    public record CustomerRequest(
            @NotBlank @Size(min = 2, max = 100) String fullNames,
            @NotBlank @Size(min = 8, max = 30) String nationalId,
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL) String email,
            @NotBlank @Pattern(regexp = ValidationPatterns.PHONE) String phoneNumber,
            @NotBlank @Size(max = 250) String address,
            @NotNull AccountStatus status
    ) {}
    public record CustomerResponse(UUID id, String fullNames, String nationalId, String email, String phoneNumber, String address, AccountStatus status) {}

    public record MeterRequest(@NotNull UUID customerId, @NotBlank String meterNumber, @NotNull MeterType meterType, @NotNull @PastOrPresent(message = "installation date cannot be in the future") LocalDate installationDate, @NotNull AccountStatus status) {}
    public record MeterResponse(UUID id, UUID customerId, String customerName, String meterNumber, MeterType meterType, LocalDate installationDate, AccountStatus status) {}

    public record ReadingRequest(@NotNull UUID meterId, @NotNull @PositiveOrZero BigDecimal previousReading, @NotNull @Positive BigDecimal currentReading, @NotNull @FutureOrPresent(message = "reading date cannot be in the past") LocalDate readingDate) {}
    public record ReadingResponse(UUID id, UUID meterId, String meterNumber, BigDecimal previousReading, BigDecimal currentReading, LocalDate readingDate, int billingMonth, int billingYear) {}

    public record TariffTierRequest(@NotNull @PositiveOrZero BigDecimal minConsumption, @PositiveOrZero BigDecimal maxConsumption, @NotNull @Positive BigDecimal rate) {}
    public record TariffRequest(@NotNull MeterType meterType, @NotNull TariffMode mode, @NotBlank @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "effectiveFrom must use YYYY-MM format, for example 2026-07") String effectiveFrom, @NotNull @PositiveOrZero BigDecimal flatRate, @NotNull @PositiveOrZero BigDecimal fixedCharge, @NotNull @PositiveOrZero @DecimalMax(value = "100.00", message = "tax rate cannot exceed 100") BigDecimal taxRate, @NotNull @PositiveOrZero BigDecimal latePenaltyRate, @Valid List<TariffTierRequest> tiers) {}
    public record TariffResponse(UUID id, MeterType meterType, TariffMode mode, int version, String effectiveFrom, BigDecimal flatRate, BigDecimal fixedCharge, BigDecimal taxRate, BigDecimal latePenaltyRate) {}

    public record BillRequest(@NotNull UUID readingId, @NotNull @Future(message = "bill due date must be in the future") LocalDate dueDate) {}
    public record BillResponse(UUID id, String reference, UUID customerId, String customerName, int billingMonth, int billingYear, BigDecimal amount, BigDecimal outstandingBalance, BillStatus status, LocalDate dueDate) {}

    public record PaymentRequest(@NotBlank String billReference, @NotNull @Positive BigDecimal amountPaid, @NotNull PaymentMethod paymentMethod, @NotNull @PastOrPresent(message = "payment date cannot be in the future") LocalDate paymentDate) {}
    public record PaymentResponse(UUID id, String billReference, BigDecimal amountPaid, PaymentMethod paymentMethod, LocalDate paymentDate, BigDecimal remainingBalance) {}

    public record NotificationResponse(UUID id, String customerName, String message, boolean sent) {}
}
