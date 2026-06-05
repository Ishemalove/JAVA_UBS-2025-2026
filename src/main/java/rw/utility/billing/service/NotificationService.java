package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.entity.Notification;
import rw.utility.billing.dto.CoreDtos.NotificationResponse;
import rw.utility.billing.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notifications, EmailService emailService) {
        this.notifications = notifications;
        this.emailService = emailService;
    }

    @Transactional
    public Page<NotificationResponse> list(Pageable pageable) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean customerOnly = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        var page = customerOnly
                ? notifications.findByCustomerEmail(auth.getName(), pageable)
                : notifications.findAll(pageable);
        if (customerOnly) {
            page.getContent().stream()
                    .filter(n -> !n.isSent())
                    .forEach(this::sendAndMark);
        }
        return page.map(n -> new NotificationResponse(n.getId(), n.getCustomer().getFullNames(), n.getMessage(), n.isSent()));
    }

    private void sendAndMark(Notification notification) {
        emailService.sendNotification(notification.getCustomer().getEmail(), notification.getMessage());
        notification.setSent(true);
        notifications.save(notification);
    }
}
