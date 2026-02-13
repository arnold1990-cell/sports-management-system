package com.sportsms.subscription;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

enum BillingPeriod { MONTHLY, ANNUAL, ONE_TIME }
enum SubscriberType { CLUB, PLAYER, LEAGUE }
enum SubscriptionStatus { ACTIVE, EXPIRED, SUSPENDED }
enum PaymentProviderType { STRIPE, PAYPAL, PAYFAST, MOBILE_MONEY, MANUAL }
enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }
enum InvoiceStatus { OPEN, PAID, OVERDUE }
enum ReminderChannel { EMAIL, SMS, IN_APP }

@Entity @Table(name = "subscription_plans")
class SubscriptionPlan {
    @Id private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private BigDecimal amount;
    @Column(nullable = false) private String currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BillingPeriod billingPeriod;
    @Column(nullable = false) private int graceDays;
    @Column(nullable = false) private boolean active;
    @PrePersist void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; } public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; } public void setCurrency(String currency) { this.currency = currency; }
    public BillingPeriod getBillingPeriod() { return billingPeriod; } public void setBillingPeriod(BillingPeriod billingPeriod) { this.billingPeriod = billingPeriod; }
    public int getGraceDays() { return graceDays; } public void setGraceDays(int graceDays) { this.graceDays = graceDays; }
    public boolean isActive() { return active; } public void setActive(boolean active) { this.active = active; }
}

@Entity @Table(name = "subscriptions")
class Subscription {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) private SubscriberType subscriberType;
    private UUID subscriberId;
    @ManyToOne @JoinColumn(name = "plan_id") private SubscriptionPlan plan;
    @Enumerated(EnumType.STRING) private SubscriptionStatus status;
    private LocalDate startDate; private LocalDate endDate; private LocalDate graceEndDate; private boolean autoRenew;
    @PrePersist void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; } public SubscriberType getSubscriberType() { return subscriberType; } public void setSubscriberType(SubscriberType s) { this.subscriberType = s; }
    public UUID getSubscriberId() { return subscriberId; } public void setSubscriberId(UUID subscriberId) { this.subscriberId = subscriberId; }
    public SubscriptionPlan getPlan() { return plan; } public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public SubscriptionStatus getStatus() { return status; } public void setStatus(SubscriptionStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDate getGraceEndDate() { return graceEndDate; } public void setGraceEndDate(LocalDate graceEndDate) { this.graceEndDate = graceEndDate; }
    public boolean isAutoRenew() { return autoRenew; } public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
}

@Entity @Table(name = "payments")
class Payment {
    @Id private UUID id;
    @ManyToOne @JoinColumn(name = "subscription_id") private Subscription subscription;
    @Enumerated(EnumType.STRING) private PaymentProviderType provider;
    private BigDecimal amount; private String currency; private String reference;
    @Enumerated(EnumType.STRING) private PaymentStatus status;
    private OffsetDateTime paidAt;
    @PrePersist void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; } public Subscription getSubscription() { return subscription; } public void setSubscription(Subscription subscription) { this.subscription = subscription; }
    public PaymentProviderType getProvider() { return provider; } public void setProvider(PaymentProviderType provider) { this.provider = provider; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; } public void setCurrency(String currency) { this.currency = currency; }
    public String getReference() { return reference; } public void setReference(String reference) { this.reference = reference; }
    public PaymentStatus getStatus() { return status; } public void setStatus(PaymentStatus status) { this.status = status; }
    public OffsetDateTime getPaidAt() { return paidAt; } public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
}

@Entity @Table(name = "invoices")
class Invoice { @Id private UUID id; @ManyToOne @JoinColumn(name = "subscription_id") private Subscription subscription; private String invoiceNumber;
    private LocalDate issueDate; private LocalDate dueDate; private BigDecimal totalAmount; @Enumerated(EnumType.STRING) private InvoiceStatus status;
    private String pdfUrl; @Column(columnDefinition = "TEXT") private String htmlContent; @PrePersist void init() { if (id == null) id = UUID.randomUUID(); }
    public UUID getId() { return id; } public Subscription getSubscription() { return subscription; } public void setSubscription(Subscription subscription) { this.subscription = subscription; }
    public String getInvoiceNumber() { return invoiceNumber; } public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getIssueDate() { return issueDate; } public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public InvoiceStatus getStatus() { return status; } public void setStatus(InvoiceStatus status) { this.status = status; }
    public String getPdfUrl() { return pdfUrl; } public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getHtmlContent() { return htmlContent; } public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
}
