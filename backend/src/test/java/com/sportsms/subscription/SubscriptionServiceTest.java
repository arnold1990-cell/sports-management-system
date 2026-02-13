package com.sportsms.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubscriptionServiceTest {
    @Test
    void createsSubscriptionWithGraceDates() {
        SubscriptionPlanRepository plans = mock(SubscriptionPlanRepository.class);
        SubscriptionRepository subs = mock(SubscriptionRepository.class);
        PaymentRepository payments = mock(PaymentRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        MockPaymentProvider provider = new MockPaymentProvider();
        SubscriptionService service = new SubscriptionService(plans, subs, payments, invoices, provider);

        SubscriptionPlan p = new SubscriptionPlan();
        p.setName("Plan"); p.setType("Club"); p.setAmount(BigDecimal.TEN); p.setCurrency("USD"); p.setBillingPeriod(BillingPeriod.MONTHLY); p.setGraceDays(5); p.setActive(true);
        UUID planId = UUID.randomUUID();
        when(plans.findById(planId)).thenReturn(Optional.of(p));
        when(subs.save(any())).thenAnswer(i -> i.getArgument(0));
        when(invoices.save(any())).thenAnswer(i -> i.getArgument(0));

        Subscription s = service.createSubscription(new SubscriptionRequest(SubscriberType.CLUB, UUID.randomUUID(), planId, true));
        assertEquals(LocalDate.now().plusMonths(1), s.getEndDate());
        assertEquals(LocalDate.now().plusMonths(1).plusDays(5), s.getGraceEndDate());
    }

    @Test
    void calculatesRiskScores() {
        SubscriptionPlanRepository plans = mock(SubscriptionPlanRepository.class);
        SubscriptionRepository subs = mock(SubscriptionRepository.class);
        PaymentRepository payments = mock(PaymentRepository.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        SubscriptionService service = new SubscriptionService(plans, subs, payments, invoices, new MockPaymentProvider());

        Subscription s = new Subscription();
        s.setEndDate(LocalDate.now().minusDays(10));
        when(subs.findAll()).thenReturn(List.of(s));
        when(invoices.countByStatus(InvoiceStatus.OVERDUE)).thenReturn(1L);

        assertEquals(1, service.riskScores().size());
    }
}
