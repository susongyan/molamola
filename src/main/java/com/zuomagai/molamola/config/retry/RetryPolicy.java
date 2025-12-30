package com.zuomagai.molamola.config.retry;

public interface RetryPolicy {

    long nextDelayMillis(int attempt, Throwable lastError);
}
