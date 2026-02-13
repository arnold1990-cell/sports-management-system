package com.sportsms.subscription;

import java.util.UUID;

public record RiskItem(UUID subscriptionId, int riskScore, long overdueDays) {}
