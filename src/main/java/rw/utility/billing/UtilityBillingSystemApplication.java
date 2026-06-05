package rw.utility.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UtilityBillingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(UtilityBillingSystemApplication.class, args);
    }
}
