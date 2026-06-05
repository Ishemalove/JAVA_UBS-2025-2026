package rw.utility.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_meter_reading_month", columnNames = {"meter_id", "billingMonth", "billingYear"}))
public class MeterReading extends BaseEntity {
    @ManyToOne(optional = false)
    private Meter meter;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal previousReading;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentReading;
    @Column(nullable = false)
    private LocalDate readingDate;
    @Column(nullable = false)
    private int billingMonth;
    @Column(nullable = false)
    private int billingYear;
    public Meter getMeter() { return meter; }
    public void setMeter(Meter meter) { this.meter = meter; }
    public BigDecimal getPreviousReading() { return previousReading; }
    public void setPreviousReading(BigDecimal previousReading) { this.previousReading = previousReading; }
    public BigDecimal getCurrentReading() { return currentReading; }
    public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }
    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }
    public int getBillingMonth() { return billingMonth; }
    public void setBillingMonth(int billingMonth) { this.billingMonth = billingMonth; }
    public int getBillingYear() { return billingYear; }
    public void setBillingYear(int billingYear) { this.billingYear = billingYear; }
    public BigDecimal consumption() { return currentReading.subtract(previousReading); }
}
