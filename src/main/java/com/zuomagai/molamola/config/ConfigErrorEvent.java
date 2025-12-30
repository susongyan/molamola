package com.zuomagai.molamola.config;

public final class ConfigErrorEvent<T> {

    public enum Phase {
        FETCH,
        LISTENER
    }

    private final Phase phase;
    private final Throwable error;
    private final int attempt;
    private final boolean retrying;
    private final ConfigChangeEvent<T> changeEvent;
    private final ConfigChangeListener<T> listener;

    public ConfigErrorEvent(Phase phase,
                            Throwable error,
                            int attempt,
                            boolean retrying,
                            ConfigChangeEvent<T> changeEvent,
                            ConfigChangeListener<T> listener) {
        this.phase = phase;
        this.error = error;
        this.attempt = attempt;
        this.retrying = retrying;
        this.changeEvent = changeEvent;
        this.listener = listener;
    }

    public Phase getPhase() {
        return phase;
    }

    public Throwable getError() {
        return error;
    }

    public int getAttempt() {
        return attempt;
    }

    public boolean isRetrying() {
        return retrying;
    }

    public ConfigChangeEvent<T> getChangeEvent() {
        return changeEvent;
    }

    public ConfigChangeListener<T> getListener() {
        return listener;
    }
}
