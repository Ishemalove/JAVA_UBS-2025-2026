package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "08 Payments", description = "Partial and full payment processing")
public class PaymentController {
    private final PaymentService payments;
    public PaymentController(PaymentService payments) { this.payments = payments; }

    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "ROLE_FINANCE: record partial/full payment, update balance/status, notify full payment")
    public PaymentResponse record(@Valid @RequestBody PaymentRequest request) { return payments.record(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: list/search payments")
    public Page<PaymentResponse> list(@RequestParam(required = false) String search, Pageable pageable) { return payments.list(search, pageable); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "ROLE_FINANCE: delete payment record")
    public void delete(@PathVariable UUID id) { payments.delete(id); }
}
