package rw.utility.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.utility.billing.dto.CoreDtos.NotificationResponse;
import rw.utility.billing.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "09 Notifications", description = "Billing, payment, and role notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: list notifications without sort query; customers receive unsent notifications by email")
    public Page<NotificationResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return notifications.list(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
