package com.sportsms.common;

import com.sportsms.fixture.Fixture;
import com.sportsms.fixture.MatchStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final EntityManager entityManager;

    public DashboardService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public DashboardDto.DashboardResponse getDashboard() {
        DashboardDto.Summary summary = new DashboardDto.Summary(
                count("clubs"), count("teams"), count("players"), count("competitions"), count("fixtures"), count("subscription_plans"), count("facility_bookings"));

        List<DashboardDto.FixtureCard> upcoming = entityManager.createQuery(
                        "select f from Fixture f where f.matchDate between :now and :week order by f.matchDate asc", Fixture.class)
                .setParameter("now", OffsetDateTime.now())
                .setParameter("week", OffsetDateTime.now().plusDays(7))
                .setMaxResults(8)
                .getResultList()
                .stream()
                .map(f -> new DashboardDto.FixtureCard(f.getId(), f.getHomeTeam().getName(), f.getAwayTeam().getName(), f.getMatchDate(), f.getStatus(), f.getHomeScore(), f.getAwayScore()))
                .toList();

        List<DashboardDto.PaymentCard> payments = entityManager.createNativeQuery("select id, amount, currency, status, paid_at from payments order by paid_at desc nulls last limit 5")
                .getResultList().stream().map(row -> {
                    Object[] r = (Object[]) row;
                    return new DashboardDto.PaymentCard(UUID.fromString(r[0].toString()), new BigDecimal(r[1].toString()), String.valueOf(r[2]), String.valueOf(r[3]), r[4] == null ? null : r[4].toString());
                }).toList();

        long unread = count("notifications") + count("chat_messages") - count("message_read_receipts");
        long bookingsToday = countWhere("facility_bookings", "cast(start_date_time as date) = current_date");
        long bookingsWeek = countWhere("facility_bookings", "cast(start_date_time as date) between current_date and current_date + interval '7 day'");
        List<DashboardDto.SimpleSeries> revenueSeries = entityManager.createNativeQuery("select to_char(date_trunc('month', coalesce(paid_at, now())), 'YYYY-MM') as month, coalesce(sum(amount),0) from payments group by 1 order by 1 desc limit 6")
                .getResultList().stream().map(r -> new DashboardDto.SimpleSeries(String.valueOf(((Object[]) r)[0]), new BigDecimal(((Object[]) r)[1].toString()))).toList();

        return new DashboardDto.DashboardResponse(summary, upcoming, payments,
                new DashboardDto.BookingSummary(bookingsToday, bookingsWeek), unread, revenueSeries,
                List.of(new DashboardDto.StatusWidget("ACTIVE", countWhere("subscriptions", "status='ACTIVE'")), new DashboardDto.StatusWidget("EXPIRED", countWhere("subscriptions", "status='EXPIRED'")), new DashboardDto.StatusWidget("SUSPENDED", countWhere("subscriptions", "status='SUSPENDED'"))));
    }

    private long count(String table) { return ((Number) entityManager.createNativeQuery("select count(*) from " + table).getSingleResult()).longValue(); }
    private long countWhere(String table, String where) { return ((Number) entityManager.createNativeQuery("select count(*) from " + table + " where " + where).getSingleResult()).longValue(); }
}
