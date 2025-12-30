package com.zuomagai.molamola.config.retry;

public final class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxRetries;
    private final long baseDelayMillis;
    private final long maxDelayMillis;
    private final double multiplier;

    public ExponentialBackoffRetryPolicy(int maxRetries,
                                         long baseDelayMillis,
                                         long maxDelayMillis,
                                         double multiplier) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (baseDelayMillis < 0) {
            throw new IllegalArgumentException("baseDelayMillis must be >= 0");
        }
        if (maxDelayMillis < 0) {
            throw new IllegalArgumentException("maxDelayMillis must be >= 0");
        }
        if (multiplier < 1.0d) {
            throw new IllegalArgumentException("multiplier must be >= 1.0");
        }
        this.maxRetries = maxRetries;
        this.baseDelayMillis = baseDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
        this.multiplier = multiplier;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getBaseDelayMillis() {
        return baseDelayMillis;
    }

    public long getMaxDelayMillis() {
        return maxDelayMillis;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public long nextDelayMillis(int attempt, Throwable lastError) {
        if (attempt > maxRetries) {
            return -1L;
        }
        if (attempt <= 1) {
            return Math.min(baseDelayMillis, maxDelayMillis);
        }
        double factor = Math.pow(multiplier, attempt - 1);
        double rawDelay = baseDelayMillis * factor;
        long delay;
        if (rawDelay >= Long.MAX_VALUE) {
            delay = Long.MAX_VALUE;
        } else {
            delay = (long) rawDelay;
        }
        return Math.min(delay, maxDelayMillis);
    }
}
