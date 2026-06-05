package rw.utility.billing.entity;

import jakarta.persistence.*;
import rw.utility.billing.enums.MeterType;
import rw.utility.billing.enums.TariffMode;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Tariff extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffMode mode;
    @Column(nullable = false)
    private int version;
    @Column(nullable = false)
    private YearMonth effectiveFrom;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal flatRate = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal fixedCharge = BigDecimal.ZERO;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal latePenaltyRate = BigDecimal.ZERO;
    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffTier> tiers = new ArrayList<>();
    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }
    public TariffMode getMode() { return mode; }
    public void setMode(TariffMode mode) { this.mode = mode; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public YearMonth getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(YearMonth effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public BigDecimal getFlatRate() { return flatRate; }
    public void setFlatRate(BigDecimal flatRate) { this.flatRate = flatRate; }
    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getLatePenaltyRate() { return latePenaltyRate; }
    public void setLatePenaltyRate(BigDecimal latePenaltyRate) { this.latePenaltyRate = latePenaltyRate; }
    public List<TariffTier> getTiers() { return tiers; }
    public void setTiers(List<TariffTier> tiers) { this.tiers = tiers; }
}
