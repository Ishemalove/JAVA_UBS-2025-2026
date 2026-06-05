package rw.utility.billing.config;

import org.slf4j.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.utility.billing.entity.User;
import rw.utility.billing.enums.*;
import rw.utility.billing.repository.UserRepository;

import java.util.Set;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedDefaultAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.existsByEmail("admin@utility.rw")) return;
            User admin = new User();
            admin.setFullNames("System Administrator");
            admin.setEmail("admin@utility.rw");
            admin.setPhoneNumber("+250780000000");
            admin.setPassword(encoder.encode("Admin@123!"));
            admin.setStatus(AccountStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setMustChangePassword(false);
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            users.save(admin);
        };
    }

    @Bean
    CommandLineRunner configurePostgresRoutines(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
                jdbcTemplate.execute("""
                        CREATE OR REPLACE FUNCTION notify_bill_generated()
                        RETURNS trigger AS $$
                        DECLARE
                            customer_name text;
                        BEGIN
                            SELECT full_names INTO customer_name FROM customer WHERE id = NEW.customer_id;
                            INSERT INTO notification(id, created_at, updated_at, customer_id, message, sent)
                            VALUES (
                                gen_random_uuid(),
                                now(),
                                now(),
                                NEW.customer_id,
                                'Dear ' || COALESCE(customer_name, 'Customer') || E',\\nYour ' || NEW.billing_month || '/' || NEW.billing_year ||
                                ' utility bill of ' || NEW.amount || ' FRW has been successfully processed.',
                                false
                            );
                            RETURN NEW;
                        END;
                        $$ LANGUAGE plpgsql
                        """);
                jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_notification ON bills");
                jdbcTemplate.execute("""
                        CREATE TRIGGER trg_bill_notification
                        AFTER INSERT ON bills
                        FOR EACH ROW
                        EXECUTE FUNCTION notify_bill_generated()
                        """);
            } catch (Exception ex) {
                log.warn("PostgreSQL bill notification trigger could not be configured: {}", ex.getMessage());
            }
        };
    }
}
