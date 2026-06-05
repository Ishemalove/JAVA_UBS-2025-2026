package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.TariffService;

import java.util.UUID;

@RestController
@RequestMapping("/api/tariffs")
@Tag(name = "06 Tariffs Taxes and Penalties", description = "Versioned tariff configuration for future cycles")
public class TariffController {
    private final TariffService tariffs;
    public TariffController(TariffService tariffs) { this.tariffs = tariffs; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: configure future versioned tariff, fixed charges, tax, and penalties")
    public TariffResponse create(@Valid @RequestBody TariffRequest request) { return tariffs.create(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: list tariffs")
    public Page<TariffResponse> list(Pageable pageable) { return tariffs.list(pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: get tariff")
    public TariffResponse get(@PathVariable UUID id) { return tariffs.get(id); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete tariff")
    public void delete(@PathVariable UUID id) { tariffs.delete(id); }
}
