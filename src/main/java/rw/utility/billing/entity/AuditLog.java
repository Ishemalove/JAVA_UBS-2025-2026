package rw.utility.billing.entity;

import jakarta.persistence.*;

@Entity
public class AuditLog extends BaseEntity {
    @Column(nullable = false)
    private String actor;
    @Column(nullable = false)
    private String action;
    @Column(nullable = false)
    private String entityName;
    @Column(nullable = false)
    private String entityId;
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
}
