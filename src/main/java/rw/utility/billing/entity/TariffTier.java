package rw.utility.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class TariffTier extends BaseEntity {
    @ManyToOne(optional = false)
    private Tariff tariff;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal minConsumption;
    @Column(precision = 14, scale = 2)
    private BigDecimal maxConsumption;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal rate;
    public Tariff getTariff() { return tariff; }
    public void setTariff(Tariff tariff) { this.tariff = tariff; }
    public BigDecimal getMinConsumption() { return minConsumption; }
    public void setMinConsumption(BigDecimal minConsumption) { this.minConsumption = minConsumption; }
    public BigDecimal getMaxConsumption() { return maxConsumption; }
    public void setMaxConsumption(BigDecimal maxConsumption) { this.maxConsumption = maxConsumption; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}
