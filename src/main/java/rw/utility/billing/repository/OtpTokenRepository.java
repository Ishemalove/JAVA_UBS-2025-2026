package rw.utility.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.utility.billing.entity.OtpToken;
import rw.utility.billing.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(User user, String purpose);
    Optional<OtpToken> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}
