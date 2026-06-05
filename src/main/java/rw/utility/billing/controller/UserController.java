package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "02 Users and Roles", description = "ROLE_ADMIN manages users, generated credentials, and role updates")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: list/search users with pagination and sorting")
    public Page<UserResponse> list(@RequestParam(required = false) String search, Pageable pageable) { return users.list(search, pageable); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: get one user")
    public UserResponse get(@PathVariable UUID id) { return users.get(id); }

    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: create Operator/Finance/Admin system user, generate temporary password, email credentials, force password change")
    public UserResponse createSystemUser(@Valid @RequestBody AdminCreateUserRequest request) { return users.createSystemUser(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: update user details, status, and roles")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) { return users.update(id, request); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete user")
    public void delete(@PathVariable UUID id) { users.delete(id); }
}
