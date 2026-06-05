package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.BlacklistedToken;

import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    boolean existsByToken(String token);
}
