package rw.utility.billing.entity;

import jakarta.persistence.*;
import rw.utility.billing.enums.AccountStatus;
import rw.utility.billing.enums.MeterType;

import java.time.LocalDate;

@Entity
public class Meter extends BaseEntity {
    @ManyToOne(optional = false)
    private Customer customer;
    @Column(nullable = false, unique = true)
    private String meterNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;
    @Column(nullable = false)
    private LocalDate installationDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }
    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }
    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
}
