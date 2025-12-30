package com.zuomagai.molamola.config;

public interface ConfigSource<T> {

    ConfigSnapshot<T> fetch() throws Exception;
}
