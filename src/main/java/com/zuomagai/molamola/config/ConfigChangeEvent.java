package com.zuomagai.molamola.config;

public final class ConfigChangeEvent<T> {

    private final ConfigSnapshot<T> previous;
    private final ConfigSnapshot<T> current;
    private final long timestampMillis;

    public ConfigChangeEvent(ConfigSnapshot<T> previous, ConfigSnapshot<T> current) {
        this.previous = previous;
        this.current = current;
        this.timestampMillis = System.currentTimeMillis();
    }

    public ConfigSnapshot<T> getPrevious() {
        return previous;
    }

    public ConfigSnapshot<T> getCurrent() {
        return current;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
