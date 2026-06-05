package rw.utility.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RefreshToken extends BaseEntity {
    @ManyToOne(optional = false)
    private User user;
    @Column(nullable = false, unique = true, length = 512)
    private String token;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private boolean revoked;
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
