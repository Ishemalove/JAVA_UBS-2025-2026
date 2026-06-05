package rw.utility.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class OtpToken extends BaseEntity {
    @ManyToOne(optional = false)
    private User user;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String purpose;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private boolean used;
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
