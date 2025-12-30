# molamola
common utils

string
- [x] StringUtils

collection
- [x] CollectionUtils

thread
- [x] NamedThreadFactory

component
- [x] config notify, long polling

## 配置变更通知使用文档

### 核心概念
- `ConfigSource<T>`: 获取配置的来源，返回 `ConfigSnapshot<T>`（包含 version + value）。
- `ConfigNotifier<T>`: 负责轮询、变更探测与通知监听器。
- `ConfigChangeListener<T>`: 处理配置变更事件。
- `ConfigErrorListener<T>`: 接收拉取或监听处理失败的错误事件。
- `RetryPolicy`: 重试策略（内置 `SimpleRetryPolicy` / `ExponentialBackoffRetryPolicy`）。

默认变更判定策略：
- `DefaultConfigChangeDetector` 优先比较 `version`，没有 version 时比较 `value`。

### HTTP 长轮询示例
```java
import com.zuomagai.molamola.config.ConfigNotifier;
import com.zuomagai.molamola.config.ConfigSnapshot;
import com.zuomagai.molamola.config.ExponentialBackoffRetryPolicy;
import com.zuomagai.molamola.config.SimpleRetryPolicy;
import com.zuomagai.molamola.config.http.HttpLongPollingConfigSource;

public class Demo {
    public static void main(String[] args) {
        HttpLongPollingConfigSource<String> source = HttpLongPollingConfigSource
                .stringBuilder("http://localhost:8080/config")
                .readTimeoutMillis(30000) // 长轮询读超时
                .build();

        ConfigNotifier<String> notifier = ConfigNotifier.<String>builder()
                .source(source)
                .pollIntervalMillis(0L) // 长轮询通常无需额外间隔
                .fetchRetryPolicy(new ExponentialBackoffRetryPolicy(5, 200, 5000, 2.0d))
                .listenerRetryPolicy(new SimpleRetryPolicy(3, 100))
                .addListener(event -> {
                    ConfigSnapshot<String> current = event.getCurrent();
                    System.out.println("config changed, version=" + current.getVersion());
                })
                .addErrorListener(event -> {
                    System.err.println("config error phase=" + event.getPhase()
                            + ", attempt=" + event.getAttempt()
                            + ", retrying=" + event.isRetrying()
                            + ", error=" + event.getError().getMessage());
                })
                .build();

        notifier.start();
    }
}
```

### HTTP 长轮询服务端实现注意事项
- 协议约定：建议使用 `ETag` / `If-None-Match` 或自定义版本号头，未变更时返回 `304` 或 `204`。
- 超时策略：服务端长轮询的超时需略大于客户端 `readTimeoutMillis`，避免双方同时断开。
- 连接保持：不要在请求线程里 `sleep`，应使用异步/事件驱动方式 hold 连接。
- 资源控制：限制单客户端并发长连接数，避免大量阻塞占用线程。
- 返回时机：配置变更立即返回新配置；无变更则在超时点返回空响应。

示例（Servlet 3.x 异步方式 hold 连接，伪代码）：
```java
AsyncContext async = request.startAsync();
async.setTimeout(35000L);

// 注册超时回调：未变更时返回 204
async.addListener(new AsyncListener() {
    public void onTimeout(AsyncEvent event) throws IOException {
        HttpServletResponse resp = (HttpServletResponse) event.getAsyncContext().getResponse();
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        event.getAsyncContext().complete();
    }
    public void onComplete(AsyncEvent event) {}
    public void onError(AsyncEvent event) {}
    public void onStartAsync(AsyncEvent event) {}
});

// 监听配置变更，变更时返回并结束连接
configBus.onChange(snapshot -> {
    HttpServletResponse resp = (HttpServletResponse) async.getResponse();
    resp.setHeader("ETag", snapshot.getVersion());
    resp.getWriter().write(snapshot.getValue());
    async.complete();
});
```

### 自定义 ConfigSource 示例
```java
import com.zuomagai.molamola.config.ConfigNotifier;
import com.zuomagai.molamola.config.ConfigSnapshot;
import com.zuomagai.molamola.config.ConfigSource;

ConfigSource<String> source = () -> new ConfigSnapshot<>("v1", "local-value");

ConfigNotifier<String> notifier = ConfigNotifier.<String>builder()
        .source(source)
        .pollIntervalMillis(1000L)
        .addListener(event -> System.out.println("changed: " + event.getCurrent().getValue()))
        .build();

// 单次拉取（无需启动后台线程）
notifier.pollOnce();
```
