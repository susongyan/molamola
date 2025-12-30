package com.zuomagai.molamola.config;

public interface ConfigErrorListener<T> {

    void onError(ConfigErrorEvent<T> event);
}
