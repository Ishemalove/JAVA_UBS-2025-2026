package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.User;
import rw.utility.billing.enums.AccountStatus;
import rw.utility.billing.enums.Role;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.CustomerRepository;
import rw.utility.billing.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository users;
    private final CustomerRepository customers;
    private final AuditService audit;
    private final PasswordEncoder encoder;
    private final EmailService email;

    public UserService(UserRepository users, CustomerRepository customers, AuditService audit, PasswordEncoder encoder, EmailService email) {
        this.users = users;
        this.customers = customers;
        this.audit = audit;
        this.encoder = encoder;
        this.email = email;
    }

    public Page<UserResponse> list(String search, Pageable pageable) {
        Page<User> page = search == null || search.isBlank()
                ? users.findAll(pageable)
                : users.findByFullNamesContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        return page.map(this::toResponse);
    }

    public UserResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public UserResponse createSystemUser(AdminCreateUserRequest request) {
        validateUniqueContact(request.email(), request.phoneNumber(), null);
        validateSystemRoles(request.roles());
        String temporaryPassword = generateTemporaryPassword();
        User user = new User();
        user.setFullNames(request.fullNames().trim());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(encoder.encode(temporaryPassword));
        user.setRoles(request.roles());
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setMustChangePassword(true);
        users.save(user);
        email.sendTemporaryCredentials(user.getEmail(), user.getFullNames(), temporaryPassword, user.getRoles());
        audit.record("CREATE_SYSTEM_USER", "User", user.getId());
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = find(id);
        validateUniqueContact(request.email(), request.phoneNumber(), user);
        Set<Role> previousRoles = Set.copyOf(user.getRoles());
        user.setFullNames(request.fullNames());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setStatus(request.status());
        user.setRoles(request.roles());
        if (!previousRoles.equals(request.roles())) {
            email.sendRoleChanged(user.getEmail(), user.getFullNames(), request.roles());
        }
        audit.record("UPDATE", "User", id);
        return toResponse(users.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = find(id);
        users.delete(user);
        audit.record("DELETE", "User", id);
    }

    private User find(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User was not found."));
    }

    private void validateUniqueContact(String email, String phoneNumber, User current) {
        boolean sameUserEmail = current != null && current.getEmail().equals(email);
        boolean sameUserPhone = current != null && current.getPhoneNumber().equals(phoneNumber);
        if (!sameUserEmail && users.existsByEmail(email)) throw new BadRequestException("Email is already registered by another system user.");
        if (!sameUserPhone && users.existsByPhoneNumber(phoneNumber)) throw new BadRequestException("Phone number is already registered by another system user.");
        if (customers.existsByEmail(email)) throw new BadRequestException("This email is already used by a customer.");
        if (customers.existsByPhoneNumber(phoneNumber)) throw new BadRequestException("This phone number is already used by a customer.");
    }

    private void validateSystemRoles(Set<Role> roles) {
        if (roles.contains(Role.ROLE_CUSTOMER)) {
            throw new BadRequestException("Administrators create system users only. Customers must use the normal OTP registration flow.");
        }
        if (roles.stream().noneMatch(role -> role == Role.ROLE_OPERATOR || role == Role.ROLE_FINANCE || role == Role.ROLE_ADMIN)) {
            throw new BadRequestException("A system user must have ROLE_OPERATOR, ROLE_FINANCE, or ROLE_ADMIN.");
        }
    }

    private String generateTemporaryPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        String symbols = "@#$%!";
        SecureRandom random = new SecureRandom();
        return "Tmp"
                + symbols.charAt(random.nextInt(symbols.length()))
                + random.nextInt(10)
                + random.ints(10, 0, alphabet.length())
                .mapToObj(alphabet::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullNames(), u.getEmail(), u.getPhoneNumber(), u.getStatus(), u.isEmailVerified(), u.isMustChangePassword(), u.getRoles());
    }
}
