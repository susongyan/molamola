package com.zuomagai.molamola.config;

public interface ConfigChangeDetector<T> {

    boolean isChanged(ConfigSnapshot<T> previous, ConfigSnapshot<T> current);
}
