package com.zuomagai.molamola.config;

public final class ConfigSnapshot<T> {

    private final String version;
    private final T value;

    public ConfigSnapshot(String version, T value) {
        this.version = version;
        this.value = value;
    }

    public String getVersion() {
        return version;
    }

    public T getValue() {
        return value;
    }
}
