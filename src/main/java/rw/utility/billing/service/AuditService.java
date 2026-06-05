package rw.utility.billing.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rw.utility.billing.entity.AuditLog;
import rw.utility.billing.repository.AuditLogRepository;

@Service
public class AuditService {
    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    public void record(String action, String entityName, Object entityId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = new AuditLog();
        log.setActor(auth == null ? "system" : auth.getName());
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(String.valueOf(entityId));
        auditLogs.save(log);
    }
}
