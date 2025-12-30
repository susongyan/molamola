package com.zuomagai.molamola.config;

import java.util.Objects;

public final class DefaultConfigChangeDetector<T> implements ConfigChangeDetector<T> {

    @Override
    public boolean isChanged(ConfigSnapshot<T> previous, ConfigSnapshot<T> current) {
        if (current == null) {
            return false;
        }
        if (previous == null) {
            return true;
        }
        String currentVersion = current.getVersion();
        String previousVersion = previous.getVersion();
        if (currentVersion != null || previousVersion != null) {
            return !Objects.equals(currentVersion, previousVersion);
        }
        return !Objects.equals(current.getValue(), previous.getValue());
    }
}
