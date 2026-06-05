package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.BillingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "07 Bills", description = "Bill generation, approval, status, and balances")
public class BillController {
    private final BillingService bills;
    public BillController(BillingService bills) { this.bills = bills; }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: generate bill and database trigger inserts notification")
    public BillResponse generate(@Valid @RequestBody BillRequest request) { return bills.generate(request); }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: approve bill")
    public BillResponse approve(@PathVariable UUID id) { return bills.approve(id); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: list/search bills")
    public Page<BillResponse> list(@RequestParam(required = false) String search, Pageable pageable) { return bills.list(search, pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: get bill")
    public BillResponse get(@PathVariable UUID id) { return bills.get(id); }
}
