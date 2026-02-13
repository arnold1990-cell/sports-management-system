package com.sportsms.analytics;

import com.sportsms.subscription.RiskItem;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final EntityManager entityManager;

    public AnalyticsController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping("/subscriptions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<Map<String, Object>> subscriptions() {
        return subscriptionsRevenue(null, null).stream()
                .map(item -> Map.of("month", item.month(), "revenue", item.revenue()))
                .toList();
    }

    @GetMapping("/subscriptions/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SubscriptionSummaryResponse subscriptionSummary() {
        BigDecimal revenueThisMonth = (BigDecimal) entityManager.createNativeQuery(
                "select coalesce(sum(amount),0) from payments where status='PAID' and date_trunc('month', paid_at)=date_trunc('month', now())")
                .getSingleResult();

        long active = countWhere("subscriptions", "status='ACTIVE'");
        long expired = countWhere("subscriptions", "status='EXPIRED'");
        long pendingPayments = countWhere("payments", "status='PENDING'");

        List<TypeCount> byType = entityManager.createNativeQuery("select lower(subscriber_type), count(*) from subscriptions group by 1")
                .getResultList().stream()
                .map(row -> {
                    Object[] value = (Object[]) row;
                    return new TypeCount(String.valueOf(value[0]), ((Number) value[1]).longValue());
                }).toList();

        List<StatusCount> statusMix = entityManager.createNativeQuery("select status, count(*) from subscriptions group by 1")
                .getResultList().stream().map(row -> {
                    Object[] value = (Object[]) row;
                    return new StatusCount(String.valueOf(value[0]), ((Number) value[1]).longValue());
                }).toList();

        List<TopClubPayment> topPayingClubs = entityManager.createNativeQuery(
                        "select coalesce(c.name, 'Unknown Club') as club, coalesce(sum(p.amount),0) as total " +
                                "from payments p " +
                                "join subscriptions s on s.id = p.subscription_id " +
                                "left join clubs c on c.id = s.subscriber_id " +
                                "where p.status='PAID' " +
                                "group by c.name order by total desc limit 5")
                .getResultList().stream().map(row -> {
                    Object[] value = (Object[]) row;
                    return new TopClubPayment(String.valueOf(value[0]), new BigDecimal(value[1].toString()));
                }).toList();

        List<RiskItem> highRisk = entityManager.createNativeQuery(
                        "select id, greatest(0, date_part('day', current_date - end_date)) as overdue " +
                                "from subscriptions where end_date < current_date order by overdue desc limit 10")
                .getResultList().stream().map(row -> {
                    Object[] value = (Object[]) row;
                    long overdue = ((Number) value[1]).longValue();
                    int score = (int) Math.min(100, overdue * 5);
                    return new RiskItem(UUID.fromString(value[0].toString()), score, overdue);
                }).toList();

        return new SubscriptionSummaryResponse(revenueThisMonth, active, expired, pendingPayments, byType, statusMix, topPayingClubs, highRisk);
    }

    @GetMapping("/subscriptions/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<MonthRevenue> subscriptionsRevenue(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate rangeFrom = from != null ? from : LocalDate.now().minusMonths(11).withDayOfMonth(1);
        LocalDate rangeTo = to != null ? to : LocalDate.now().plusDays(1);

        return entityManager.createNativeQuery(
                        "select to_char(date_trunc('month', paid_at), 'YYYY-MM') as month, coalesce(sum(amount),0) " +
                                "from payments where status='PAID' and paid_at between :from and :to group by 1 order by 1")
                .setParameter("from", rangeFrom.atStartOfDay())
                .setParameter("to", rangeTo.atStartOfDay())
                .getResultList().stream().map(row -> {
                    Object[] value = (Object[]) row;
                    return new MonthRevenue(String.valueOf(value[0]), new BigDecimal(value[1].toString()));
                }).toList();
    }

    @GetMapping("/subscriptions/expiring")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ExpiringSubscription> expiringSubscriptions(@RequestParam(name = "days", defaultValue = "30") long days) {
        return entityManager.createNativeQuery(
                        "select id, status, end_date from subscriptions where end_date between current_date and current_date + cast(:days || ' day' as interval) order by end_date asc")
                .setParameter("days", String.valueOf(days))
                .getResultList().stream().map(row -> {
                    Object[] value = (Object[]) row;
                    return new ExpiringSubscription(UUID.fromString(value[0].toString()), String.valueOf(value[1]), LocalDate.parse(value[2].toString()));
                }).toList();
    }

    private long countWhere(String table, String where) {
        return ((Number) entityManager.createNativeQuery("select count(*) from " + table + " where " + where).getSingleResult()).longValue();
    }

    public record MonthRevenue(String month, BigDecimal revenue) {}
    public record ExpiringSubscription(UUID subscriptionId, String status, LocalDate endDate) {}
    public record TypeCount(String type, long count) {}
    public record StatusCount(String status, long count) {}
    public record TopClubPayment(String club, BigDecimal totalPaid) {}
    public record SubscriptionSummaryResponse(BigDecimal revenueThisMonth,
                                              long activeSubscriptions,
                                              long expiredSubscriptions,
                                              long pendingPayments,
                                              List<TypeCount> byType,
                                              List<StatusCount> statusMix,
                                              List<TopClubPayment> topPayingClubs,
                                              List<RiskItem> highRisk) {}
}
