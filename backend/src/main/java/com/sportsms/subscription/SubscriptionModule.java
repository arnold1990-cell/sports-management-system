package com.sportsms.subscription;

import com.sportsms.common.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {}
interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByStatus(SubscriptionStatus status);
}
interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findTop10ByOrderByPaidAtDesc();
}
interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    long countByStatus(InvoiceStatus status);
}

record PlanRequest(String name, String type, BigDecimal amount, String currency, BillingPeriod billingPeriod, int graceDays, boolean active) {}
record SubscriptionRequest(SubscriberType subscriberType, UUID subscriberId, UUID planId, boolean autoRenew) {}
record PaymentRequest(@NotNull UUID subscriptionId, PaymentProviderType provider, BigDecimal amount, String currency) {}
record VerifyResponse(UUID paymentId, PaymentStatus status, String message) {}
record RiskItem(UUID subscriptionId, int riskScore, long overdueDays) {}

interface PaymentProvider { VerifyResponse verify(Payment payment); }

@Service class MockPaymentProvider implements PaymentProvider {
    public VerifyResponse verify(Payment payment) { return new VerifyResponse(payment.getId(), PaymentStatus.PAID, "Mock payment verified"); }
}

@Service
class SubscriptionService {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProvider paymentProvider;

    SubscriptionService(SubscriptionPlanRepository planRepository, SubscriptionRepository subscriptionRepository,
                        PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, PaymentProvider paymentProvider) {
        this.planRepository = planRepository; this.subscriptionRepository = subscriptionRepository; this.paymentRepository = paymentRepository; this.invoiceRepository = invoiceRepository; this.paymentProvider = paymentProvider;
    }

    List<SubscriptionPlan> plans() { return planRepository.findAll(); }
    SubscriptionPlan createPlan(PlanRequest request) {
        SubscriptionPlan p = new SubscriptionPlan(); p.setName(request.name()); p.setType(request.type()); p.setAmount(request.amount()); p.setCurrency(request.currency()); p.setBillingPeriod(request.billingPeriod()); p.setGraceDays(request.graceDays()); p.setActive(request.active());
        return planRepository.save(p);
    }

    Subscription createSubscription(SubscriptionRequest request) {
        SubscriptionPlan plan = planRepository.findById(request.planId()).orElseThrow(() -> new NotFoundException("Plan not found"));
        Subscription s = new Subscription(); s.setSubscriberType(request.subscriberType()); s.setSubscriberId(request.subscriberId()); s.setPlan(plan); s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartDate(LocalDate.now());
        LocalDate endDate = switch (plan.getBillingPeriod()) { case MONTHLY -> LocalDate.now().plusMonths(1); case ANNUAL -> LocalDate.now().plusYears(1); case ONE_TIME -> LocalDate.now().plusDays(1); };
        s.setEndDate(endDate); s.setGraceEndDate(endDate.plusDays(plan.getGraceDays())); s.setAutoRenew(request.autoRenew());
        s = subscriptionRepository.save(s);
        generateInvoice(s);
        return s;
    }

    List<Subscription> subscriptions() { return subscriptionRepository.findAll(); }

    Payment createPayment(PaymentRequest request) {
        Subscription s = subscriptionRepository.findById(request.subscriptionId()).orElseThrow(() -> new NotFoundException("Subscription not found"));
        Payment payment = new Payment(); payment.setSubscription(s); payment.setProvider(request.provider()); payment.setAmount(request.amount()); payment.setCurrency(request.currency()); payment.setStatus(PaymentStatus.PENDING); payment.setReference("PAY-" + System.currentTimeMillis());
        return paymentRepository.save(payment);
    }

    @Transactional
    VerifyResponse verifyPayment(UUID paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        VerifyResponse response = paymentProvider.verify(p);
        p.setStatus(response.status()); p.setPaidAt(OffsetDateTime.now()); paymentRepository.save(p);
        if (response.status() == PaymentStatus.PAID) {
            Subscription sub = p.getSubscription(); sub.setStatus(SubscriptionStatus.ACTIVE); subscriptionRepository.save(sub);
        }
        return response;
    }

    List<Payment> latestPayments() { return paymentRepository.findTop10ByOrderByPaidAtDesc(); }

    List<RiskItem> riskScores() {
        return subscriptionRepository.findAll().stream().map(s -> {
            long overdue = s.getEndDate().isBefore(LocalDate.now()) ? ChronoUnit.DAYS.between(s.getEndDate(), LocalDate.now()) : 0;
            int unpaid = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE) > 0 ? 20 : 0;
            int score = (int) Math.min(100, overdue * 5 + unpaid);
            return new RiskItem(s.getId(), score, overdue);
        }).toList();
    }

    private void generateInvoice(Subscription s) {
        Invoice i = new Invoice(); i.setSubscription(s); i.setInvoiceNumber("INV-" + System.currentTimeMillis()); i.setIssueDate(LocalDate.now()); i.setDueDate(LocalDate.now().plusDays(7)); i.setTotalAmount(s.getPlan().getAmount()); i.setStatus(InvoiceStatus.OPEN);
        i.setHtmlContent("<h1>Invoice</h1><p>Subscription " + s.getId() + "</p>");
        invoiceRepository.save(i);
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void refreshStatuses() {
        for (Subscription s : subscriptionRepository.findAll()) {
            if (s.getEndDate().isBefore(LocalDate.now()) && s.getGraceEndDate().isBefore(LocalDate.now())) {
                s.setStatus(SubscriptionStatus.SUSPENDED);
            } else if (s.getEndDate().isBefore(LocalDate.now())) {
                s.setStatus(SubscriptionStatus.EXPIRED);
            }
        }
        subscriptionRepository.saveAll(subscriptionRepository.findAll());
    }
}

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController {
    private final SubscriptionService service;
    SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping("/plans") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SubscriptionPlan> plans() { return service.plans(); }
    @PostMapping("/plans") @PreAuthorize("hasRole('ADMIN')")
    public SubscriptionPlan createPlan(@Valid @RequestBody PlanRequest request) { return service.createPlan(request); }
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<Subscription> subscriptions() { return service.subscriptions(); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Subscription createSubscription(@Valid @RequestBody SubscriptionRequest request) { return service.createSubscription(request); }
    @PostMapping("/payments") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Payment createPayment(@Valid @RequestBody PaymentRequest request) { return service.createPayment(request); }
    @PostMapping("/payments/{id}/verify") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public VerifyResponse verify(@PathVariable UUID id) { return service.verifyPayment(id); }
    @GetMapping("/payments/latest") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<Payment> latest() { return service.latestPayments(); }
    @GetMapping("/analytics/risk") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<RiskItem> risk() { return service.riskScores(); }

    @PostMapping("/payments/webhook/{provider}")
    public Map<String, String> webhook(@PathVariable String provider) { return Map.of("status", "accepted", "provider", provider); }
}
