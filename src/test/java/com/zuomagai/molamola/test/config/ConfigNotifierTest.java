package com.zuomagai.molamola.test.config;

import com.zuomagai.molamola.config.ConfigChangeEvent;
import com.zuomagai.molamola.config.ConfigChangeListener;
import com.zuomagai.molamola.config.ConfigNotifier;
import com.zuomagai.molamola.config.ConfigSnapshot;
import com.zuomagai.molamola.config.ConfigSource;
import com.zuomagai.molamola.config.retry.SimpleRetryPolicy;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigNotifierTest {

    @Test
    public void testChangeNotification() throws Exception {
        AtomicInteger fetchCount = new AtomicInteger();
        ConfigSource<String> source = () -> {
            int index = fetchCount.getAndIncrement();
            if (index == 0) {
                return new ConfigSnapshot<>("v1", "a");
            }
            return new ConfigSnapshot<>("v2", "b");
        };

        AtomicReference<ConfigChangeEvent<String>> lastEvent = new AtomicReference<>();
        ConfigChangeListener<String> listener = lastEvent::set;

        ConfigNotifier<String> notifier = ConfigNotifier.<String>builder()
                .source(source)
                .addListener(listener)
                .pollIntervalMillis(0L)
                .build();

        Assert.assertTrue(notifier.pollOnce());
        ConfigChangeEvent<String> first = lastEvent.get();
        Assert.assertNotNull(first);
        Assert.assertNull(first.getPrevious());
        Assert.assertEquals("v1", first.getCurrent().getVersion());

        Assert.assertTrue(notifier.pollOnce());
        ConfigChangeEvent<String> second = lastEvent.get();
        Assert.assertNotNull(second);
        Assert.assertEquals("v1", second.getPrevious().getVersion());
        Assert.assertEquals("v2", second.getCurrent().getVersion());
    }

    @Test
    public void testListenerRetrySuccess() throws Exception {
        ConfigSource<String> source = () -> new ConfigSnapshot<>("v1", "a");
        AtomicInteger calls = new AtomicInteger();
        ConfigChangeListener<String> listener = event -> {
            if (calls.getAndIncrement() < 2) {
                throw new RuntimeException("fail");
            }
        };

        ConfigNotifier<String> notifier = ConfigNotifier.<String>builder()
                .source(source)
                .addListener(listener)
                .listenerRetryPolicy(new SimpleRetryPolicy(2, 0L))
                .pollIntervalMillis(0L)
                .build();

        Assert.assertTrue(notifier.pollOnce());
        Assert.assertEquals(3, calls.get());
    }

    @Test
    public void testFetchRetrySuccess() throws Exception {
        AtomicInteger fetchAttempts = new AtomicInteger();
        ConfigSource<String> source = () -> {
            if (fetchAttempts.getAndIncrement() < 2) {
                throw new RuntimeException("fetch");
            }
            return new ConfigSnapshot<>("v1", "a");
        };

        AtomicInteger changes = new AtomicInteger();
        ConfigChangeListener<String> listener = event -> changes.incrementAndGet();

        ConfigNotifier<String> notifier = ConfigNotifier.<String>builder()
                .source(source)
                .addListener(listener)
                .fetchRetryPolicy(new SimpleRetryPolicy(2, 0L))
                .pollIntervalMillis(0L)
                .build();

        Assert.assertTrue(notifier.pollOnce());
        Assert.assertEquals(3, fetchAttempts.get());
        Assert.assertEquals(1, changes.get());
    }
}
