package rw.utility.billing.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.utility.billing.dto.CoreDtos.*;
import rw.utility.billing.entity.Customer;
import rw.utility.billing.exception.*;
import rw.utility.billing.repository.CustomerRepository;
import rw.utility.billing.repository.UserRepository;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final UserRepository users;
    private final AuditService audit;

    public CustomerService(CustomerRepository customers, UserRepository users, AuditService audit) {
        this.customers = customers;
        this.users = users;
        this.audit = audit;
    }

    public Page<CustomerResponse> list(String search, Pageable pageable) {
        Page<Customer> page = search == null || search.isBlank() ? customers.findAll(pageable)
                : customers.findByFullNamesContainingIgnoreCaseOrNationalIdContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, search, pageable);
        return page.map(this::toResponse);
    }

    public CustomerResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customers.existsByNationalId(request.nationalId())) throw new BadRequestException("Customer national ID is already registered.");
        validateUniqueContact(request.email(), request.phoneNumber(), null);
        Customer c = new Customer();
        apply(c, request);
        customers.save(c);
        audit.record("CREATE", "Customer", c.getId());
        return toResponse(c);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer c = find(id);
        if (!c.getNationalId().equals(request.nationalId()) && customers.existsByNationalId(request.nationalId())) throw new BadRequestException("Customer national ID is already registered.");
        validateUniqueContact(request.email(), request.phoneNumber(), c);
        apply(c, request);
        audit.record("UPDATE", "Customer", id);
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID id) {
        customers.delete(find(id));
        audit.record("DELETE", "Customer", id);
    }

    Customer find(UUID id) {
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Customer was not found."));
    }

    private void apply(Customer c, CustomerRequest r) {
        c.setFullNames(r.fullNames());
        c.setNationalId(r.nationalId());
        c.setEmail(r.email());
        c.setPhoneNumber(r.phoneNumber());
        c.setAddress(r.address());
        c.setStatus(r.status());
    }

    private void validateUniqueContact(String email, String phoneNumber, Customer current) {
        boolean sameCustomerEmail = current != null && current.getEmail().equals(email);
        boolean sameCustomerPhone = current != null && current.getPhoneNumber().equals(phoneNumber);
        if (!sameCustomerEmail && customers.existsByEmail(email)) throw new BadRequestException("Customer email is already registered.");
        if (!sameCustomerPhone && customers.existsByPhoneNumber(phoneNumber)) throw new BadRequestException("Customer phone number is already registered.");
        if (users.existsByEmail(email)) throw new BadRequestException("This email is already used by a system user.");
        if (users.existsByPhoneNumber(phoneNumber)) throw new BadRequestException("This phone number is already used by a system user.");
    }

    CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getFullNames(), c.getNationalId(), c.getEmail(), c.getPhoneNumber(), c.getAddress(), c.getStatus());
    }
}
