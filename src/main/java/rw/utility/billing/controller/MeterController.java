package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.MeterService;

import java.util.UUID;

@RestController
@RequestMapping("/api/meters")
@Tag(name = "04 Meters", description = "Customer utility meters")
public class MeterController {
    private final MeterService meters;
    public MeterController(MeterService meters) { this.meters = meters; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: create meter for customer")
    public MeterResponse create(@Valid @RequestBody MeterRequest request) { return meters.create(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: list/search meters")
    public Page<MeterResponse> list(@RequestParam(required = false) String search, Pageable pageable) { return meters.list(search, pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: get meter")
    public MeterResponse get(@PathVariable UUID id) { return meters.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: update meter")
    public MeterResponse update(@PathVariable UUID id, @Valid @RequestBody MeterRequest request) { return meters.update(id, request); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete meter")
    public void delete(@PathVariable UUID id) { meters.delete(id); }
}
