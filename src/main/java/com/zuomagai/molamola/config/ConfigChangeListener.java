package com.zuomagai.molamola.config;

public interface ConfigChangeListener<T> {

    void onChange(ConfigChangeEvent<T> event) throws Exception;
}
