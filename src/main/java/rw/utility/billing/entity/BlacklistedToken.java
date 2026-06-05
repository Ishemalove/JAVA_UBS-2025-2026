package rw.utility.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BlacklistedToken extends BaseEntity {
    @Column(nullable = false, unique = true, length = 700)
    private String token;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
