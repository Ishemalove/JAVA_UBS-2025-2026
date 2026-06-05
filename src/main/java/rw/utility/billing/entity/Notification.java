package rw.utility.billing.entity;

import jakarta.persistence.*;

@Entity
public class Notification extends BaseEntity {
    @ManyToOne(optional = false)
    private Customer customer;
    @Column(nullable = false, length = 1000)
    private String message;
    @Column(nullable = false)
    private boolean sent;
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
}
