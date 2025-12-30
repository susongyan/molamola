package com.zuomagai.molamola.test.config;

import com.sun.net.httpserver.HttpServer;
import com.zuomagai.molamola.config.ConfigSnapshot;
import com.zuomagai.molamola.config.http.HttpLongPollingConfigSource;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class HttpLongPollingConfigSourceTest {

    @Test
    public void testFetchWithEtag() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        AtomicReference<String> lastIfNoneMatch = new AtomicReference<>();
        server.createContext("/config", exchange -> {
            String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
            if (ifNoneMatch != null) {
                lastIfNoneMatch.set(ifNoneMatch);
            }
            if ("v1".equals(ifNoneMatch)) {
                exchange.sendResponseHeaders(304, -1);
                exchange.close();
                return;
            }
            byte[] payload = "alpha".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("ETag", "v1");
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(payload);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/config";
            HttpLongPollingConfigSource<String> source = HttpLongPollingConfigSource.stringBuilder(url)
                    .readTimeoutMillis(2000)
                    .build();

            ConfigSnapshot<String> first = source.fetch();
            Assert.assertEquals("v1", first.getVersion());
            Assert.assertEquals("alpha", first.getValue());

            ConfigSnapshot<String> second = source.fetch();
            Assert.assertEquals("v1", second.getVersion());
            Assert.assertEquals("alpha", second.getValue());
            Assert.assertEquals("v1", lastIfNoneMatch.get());
        } finally {
            server.stop(0);
        }
    }
}
