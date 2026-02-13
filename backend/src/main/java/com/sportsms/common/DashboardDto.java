package com.sportsms.common;

import com.sportsms.fixture.MatchStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DashboardDto {
    public record Summary(long clubs, long teams, long players, long competitions, long fixtures, long subscriptions, long bookings) {}
    public record FixtureCard(UUID id, String homeTeam, String awayTeam, OffsetDateTime matchDate, MatchStatus status, Integer homeScore, Integer awayScore) {}
    public record PaymentCard(UUID id, BigDecimal amount, String currency, String status, String paidAt) {}
    public record BookingSummary(long today, long thisWeek) {}
    public record SimpleSeries(String label, BigDecimal value) {}
    public record StatusWidget(String label, long value) {}
    public record DashboardResponse(Summary summary, List<FixtureCard> upcomingMatches, List<PaymentCard> latestPayments,
                                    BookingSummary bookingSummary, long unreadMessages, List<SimpleSeries> revenuePerMonth,
                                    List<StatusWidget> subscriptionStatuses) {}
}
