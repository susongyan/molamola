package com.zuomagai.molamola.config;

import com.zuomagai.molamola.config.retry.RetryPolicy;
import com.zuomagai.molamola.config.retry.SimpleRetryPolicy;
import com.zuomagai.molamola.thread.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ConfigNotifier<T> implements AutoCloseable {

    private final ConfigSource<T> source;
    private final ConfigChangeDetector<T> changeDetector;
    private final RetryPolicy fetchRetryPolicy;
    private final RetryPolicy listenerRetryPolicy;
    private final long pollIntervalMillis;
    private final ThreadFactory threadFactory;
    private final CopyOnWriteArrayList<ConfigChangeListener<T>> listeners;
    private final CopyOnWriteArrayList<ConfigErrorListener<T>> errorListeners;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object pollLock = new Object();
    private volatile ConfigSnapshot<T> lastSnapshot;
    private volatile Thread worker;

    private ConfigNotifier(Builder<T> builder) {
        this.source = builder.source;
        this.changeDetector = builder.changeDetector;
        this.fetchRetryPolicy = builder.fetchRetryPolicy;
        this.listenerRetryPolicy = builder.listenerRetryPolicy;
        this.pollIntervalMillis = builder.pollIntervalMillis;
        this.threadFactory = builder.threadFactory;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
        this.errorListeners = new CopyOnWriteArrayList<>(builder.errorListeners);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public void addListener(ConfigChangeListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void addErrorListener(ConfigErrorListener<T> listener) {
        if (listener != null) {
            errorListeners.add(listener);
        }
    }

    public void removeListener(ConfigChangeListener<T> listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void removeErrorListener(ConfigErrorListener<T> listener) {
        if (listener != null) {
            errorListeners.remove(listener);
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public ConfigSnapshot<T> getLastSnapshot() {
        return lastSnapshot;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            Thread thread = threadFactory.newThread(this::runLoop);
            worker = thread;
            thread.start();
        }
    }

    public void stop() {
        running.set(false);
        Thread thread = worker;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void close() {
        stop();
    }

    public boolean pollOnce() {
        synchronized (pollLock) {
            return doPoll(false);
        }
    }

    private void runLoop() {
        while (running.get()) {
            synchronized (pollLock) {
                doPoll(true);
            }
            if (!running.get()) {
                break;
            }
            if (pollIntervalMillis > 0) {
                if (!sleep(pollIntervalMillis, true)) {
                    break;
                }
            }
        }
    }

    private boolean doPoll(boolean allowStop) {
        ConfigSnapshot<T> snapshot = fetchWithRetry(allowStop);
        if (snapshot == null) {
            return false;
        }
        boolean changed = changeDetector.isChanged(lastSnapshot, snapshot);
        if (changed && !listeners.isEmpty()) {
            ConfigChangeEvent<T> event = new ConfigChangeEvent<>(lastSnapshot, snapshot);
            notifyListeners(event, allowStop);
        }
        lastSnapshot = snapshot;
        return changed;
    }

    private ConfigSnapshot<T> fetchWithRetry(boolean allowStop) {
        int attempt = 0;
        while (true) {
            try {
                return source.fetch();
            } catch (Exception ex) {
                attempt++;
                long delay = fetchRetryPolicy.nextDelayMillis(attempt, ex);
                boolean retrying = delay >= 0;
                notifyError(new ConfigErrorEvent<>(ConfigErrorEvent.Phase.FETCH, ex, attempt, retrying, null, null));
                if (!retrying) {
                    return null;
                }
                if (allowStop && !running.get()) {
                    return null;
                }
                if (!sleep(delay, allowStop)) {
                    return null;
                }
            }
        }
    }

    private void notifyListeners(ConfigChangeEvent<T> event, boolean allowStop) {
        for (ConfigChangeListener<T> listener : listeners) {
            int attempt = 0;
            while (true) {
                try {
                    listener.onChange(event);
                    break;
                } catch (Exception ex) {
                    attempt++;
                    long delay = listenerRetryPolicy.nextDelayMillis(attempt, ex);
                    boolean retrying = delay >= 0;
                    notifyError(new ConfigErrorEvent<>(ConfigErrorEvent.Phase.LISTENER, ex, attempt, retrying, event, listener));
                    if (!retrying) {
                        break;
                    }
                    if (allowStop && !running.get()) {
                        return;
                    }
                    if (!sleep(delay, allowStop)) {
                        return;
                    }
                }
            }
        }
    }

    private void notifyError(ConfigErrorEvent<T> event) {
        if (errorListeners.isEmpty()) {
            return;
        }
        for (ConfigErrorListener<T> listener : errorListeners) {
            try {
                listener.onError(event);
            } catch (Exception ignored) {
                // Keep notifier alive even if error handlers fail.
            }
        }
    }

    private boolean sleep(long delayMillis, boolean allowStop) {
        if (delayMillis <= 0) {
            return !(allowStop && !running.get());
        }
        try {
            Thread.sleep(delayMillis);
            return !(allowStop && !running.get());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static final class Builder<T> {

        private ConfigSource<T> source;
        private ConfigChangeDetector<T> changeDetector = new DefaultConfigChangeDetector<T>();
        private RetryPolicy fetchRetryPolicy = SimpleRetryPolicy.noRetry();
        private RetryPolicy listenerRetryPolicy = SimpleRetryPolicy.noRetry();
        private long pollIntervalMillis = 1000L;
        private ThreadFactory threadFactory = new NamedThreadFactory("config-notify-", true);
        private final List<ConfigChangeListener<T>> listeners = new ArrayList<>();
        private final List<ConfigErrorListener<T>> errorListeners = new ArrayList<>();

        public Builder<T> source(ConfigSource<T> source) {
            if (source == null) {
                throw new IllegalArgumentException("source must not be null");
            }
            this.source = source;
            return this;
        }

        public Builder<T> pollIntervalMillis(long pollIntervalMillis) {
            if (pollIntervalMillis < 0) {
                throw new IllegalArgumentException("pollIntervalMillis must be >= 0");
            }
            this.pollIntervalMillis = pollIntervalMillis;
            return this;
        }

        public Builder<T> changeDetector(ConfigChangeDetector<T> changeDetector) {
            if (changeDetector == null) {
                throw new IllegalArgumentException("changeDetector must not be null");
            }
            this.changeDetector = changeDetector;
            return this;
        }

        public Builder<T> fetchRetryPolicy(RetryPolicy fetchRetryPolicy) {
            if (fetchRetryPolicy == null) {
                throw new IllegalArgumentException("fetchRetryPolicy must not be null");
            }
            this.fetchRetryPolicy = fetchRetryPolicy;
            return this;
        }

        public Builder<T> listenerRetryPolicy(RetryPolicy listenerRetryPolicy) {
            if (listenerRetryPolicy == null) {
                throw new IllegalArgumentException("listenerRetryPolicy must not be null");
            }
            this.listenerRetryPolicy = listenerRetryPolicy;
            return this;
        }

        public Builder<T> threadFactory(ThreadFactory threadFactory) {
            if (threadFactory == null) {
                throw new IllegalArgumentException("threadFactory must not be null");
            }
            this.threadFactory = threadFactory;
            return this;
        }

        public Builder<T> addListener(ConfigChangeListener<T> listener) {
            if (listener != null) {
                listeners.add(listener);
            }
            return this;
        }

        public Builder<T> addErrorListener(ConfigErrorListener<T> listener) {
            if (listener != null) {
                errorListeners.add(listener);
            }
            return this;
        }

        public ConfigNotifier<T> build() {
            if (source == null) {
                throw new IllegalStateException("ConfigSource is required");
            }
            return new ConfigNotifier<>(this);
        }
    }
}
