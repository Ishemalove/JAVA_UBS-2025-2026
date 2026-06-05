package rw.utility.billing.entity;

import jakarta.persistence.*;
import rw.utility.billing.enums.AccountStatus;

@Entity
@Table(indexes = {
        @Index(name = "idx_customer_national_id", columnList = "nationalId", unique = true),
        @Index(name = "idx_customer_email", columnList = "email")
})
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String fullNames;
    @Column(nullable = false, unique = true)
    private String nationalId;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String address;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
    public String getFullNames() { return fullNames; }
    public void setFullNames(String fullNames) { this.fullNames = fullNames; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
}
