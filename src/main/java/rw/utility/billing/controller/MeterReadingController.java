package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.MeterReadingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/readings")
@Tag(name = "05 Meter Readings", description = "Operator reading capture with monthly uniqueness rules")
public class MeterReadingController {
    private final MeterReadingService readings;
    public MeterReadingController(MeterReadingService readings) { this.readings = readings; }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "ROLE_OPERATOR: capture active meter reading; one per meter per month/year")
    public ReadingResponse create(@Valid @RequestBody ReadingRequest request) { return readings.create(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: list readings")
    public Page<ReadingResponse> list(Pageable pageable) { return readings.list(pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: get reading")
    public ReadingResponse get(@PathVariable UUID id) { return readings.get(id); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete reading")
    public void delete(@PathVariable UUID id) { readings.delete(id); }
}
