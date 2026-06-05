package rw.utility.billing.service;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rw.utility.billing.enums.Role;
import rw.utility.billing.exception.BadRequestException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;

    public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String from, @Value("${app.mail.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
    }

    public void sendWelcome(String to, String name) {
        send(to, "Welcome to Utility Billing System", "Dear " + name + ", your account has been created.");
    }

    public void sendVerification(String to, String token, String otp) {
        send(to, "Verify your account", "Verification token: " + token + "\nOTP: " + otp);
    }

    public void sendPasswordReset(String to, String otp) {
        send(to, "Password reset OTP", "Use this OTP to reset your password: " + otp);
    }

    public void sendNotification(String to, String message) {
        send(to, "Utility notification", message);
    }

    public void sendTemporaryCredentials(String to, String name, String temporaryPassword, Set<Role> roles) {
        String roleList = roles.stream().map(Enum::name).collect(Collectors.joining(", "));
        send(to, "Your UBS system login credentials",
                "Dear " + name + ",\n\n"
                        + "An administrator created your Utility Billing System account.\n"
                        + "Username/email: " + to + "\n"
                        + "Temporary password: " + temporaryPassword + "\n"
                        + "Assigned role(s): " + roleList + "\n\n"
                        + "Sign in using Swagger or the system login endpoint, then change this temporary password immediately. "
                        + "You will be required to change it after your first successful login.\n\n"
                        + roleResponsibilities(roles));
    }

    public void sendRoleChanged(String to, String name, Set<Role> roles) {
        send(to, "Your UBS system role has been updated",
                "Dear " + name + ",\n\n"
                        + "Your Utility Billing System access role has been assigned or updated to: "
                        + roles.stream().map(Enum::name).collect(Collectors.joining(", ")) + ".\n\n"
                        + roleResponsibilities(roles));
    }

    private void send(String to, String subject, String body) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Email sent from={} to={} subject={}", from, to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to={} subject={}", to, subject, ex);
            throw new BadRequestException("Email could not be sent to " + to + ": " + ex.getMessage());
        }
    }

    private String roleResponsibilities(Set<Role> roles) {
        return roles.stream().map(role -> switch (role) {
            case ROLE_ADMIN -> "ROLE_ADMIN: configure tariffs, approve bills, and manage users.";
            case ROLE_OPERATOR -> "ROLE_OPERATOR: capture active meter readings.";
            case ROLE_FINANCE -> "ROLE_FINANCE: approve bills and record/approve payments.";
            case ROLE_CUSTOMER -> "ROLE_CUSTOMER: view bills, payment history, and notifications.";
        }).collect(Collectors.joining("\n"));
    }
}
