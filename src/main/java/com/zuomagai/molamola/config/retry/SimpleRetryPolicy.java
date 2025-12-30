package com.zuomagai.molamola.config.retry;

public final class SimpleRetryPolicy implements RetryPolicy {

    private final int maxRetries;
    private final long delayMillis;

    public SimpleRetryPolicy(int maxRetries, long delayMillis) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
        this.maxRetries = maxRetries;
        this.delayMillis = delayMillis;
    }

    public static SimpleRetryPolicy noRetry() {
        return new SimpleRetryPolicy(0, 0L);
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    @Override
    public long nextDelayMillis(int attempt, Throwable lastError) {
        if (attempt > maxRetries) {
            return -1L;
        }
        return delayMillis;
    }
}
