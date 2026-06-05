package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.CustomerService;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "03 Customers", description = "Customer records and duplicate prevention")
public class CustomerController {
    private final CustomerService customers;
    public CustomerController(CustomerService customers) { this.customers = customers; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: create customer with duplicate prevention")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) { return customers.create(request); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR: list/search customers")
    public Page<CustomerResponse> list(@RequestParam(required = false) String search, Pageable pageable) { return customers.list(search, pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR: get customer")
    public CustomerResponse get(@PathVariable UUID id) { return customers.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: update customer")
    public CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) { return customers.update(id, request); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete customer")
    public void delete(@PathVariable UUID id) { customers.delete(id); }
}
