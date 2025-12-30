package com.zuomagai.molamola.config.http;

import com.zuomagai.molamola.config.ConfigSnapshot;
import com.zuomagai.molamola.config.ConfigSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpLongPollingConfigSource<T> implements ConfigSource<T> {

    public interface ResponseParser<T> {
        T parse(String body) throws Exception;
    }

    private final URL url;
    private final ResponseParser<T> parser;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final String requestVersionHeader;
    private final String responseVersionHeader;
    private final String defaultCharset;
    private final Map<String, String> headers;
    private ConfigSnapshot<T> lastSnapshot;

    private HttpLongPollingConfigSource(Builder<T> builder) {
        this.url = builder.url;
        this.parser = builder.parser;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.requestVersionHeader = builder.requestVersionHeader;
        this.responseVersionHeader = builder.responseVersionHeader;
        this.defaultCharset = builder.defaultCharset;
        this.headers = new LinkedHashMap<>(builder.headers);
    }

    public static Builder<String> stringBuilder(String url) {
        return new Builder<>(url, body -> body);
    }

    public static <T> Builder<T> builder(String url, ResponseParser<T> parser) {
        return new Builder<>(url, parser);
    }

    @Override
    public synchronized ConfigSnapshot<T> fetch() throws Exception {
        HttpURLConnection connection = openConnection();
        try {
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_NOT_MODIFIED || status == HttpURLConnection.HTTP_NO_CONTENT) {
                ConfigSnapshot<T> snapshot = lastSnapshot;
                if (snapshot == null) {
                    snapshot = new ConfigSnapshot<>(readVersion(connection), null);
                    lastSnapshot = snapshot;
                }
                return snapshot;
            }
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected response " + status + " from " + url + ": " + readErrorBody(connection));
            }
            String body = readBody(connection);
            T value = parser.parse(body);
            ConfigSnapshot<T> snapshot = new ConfigSnapshot<>(readVersion(connection), value);
            lastSnapshot = snapshot;
            return snapshot;
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (requestVersionHeader != null && lastSnapshot != null) {
            String version = lastSnapshot.getVersion();
            if (version != null) {
                connection.setRequestProperty(requestVersionHeader, version);
            }
        }
        return connection;
    }

    private String readVersion(HttpURLConnection connection) {
        if (responseVersionHeader == null) {
            return null;
        }
        String version = connection.getHeaderField(responseVersionHeader);
        return version == null || version.isEmpty() ? null : version;
    }

    private String readBody(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
            return readToString(inputStream, resolveCharset(connection));
        }
    }

    private String readErrorBody(HttpURLConnection connection) {
        InputStream errorStream = connection.getErrorStream();
        if (errorStream == null) {
            return "";
        }
        try (InputStream inputStream = new BufferedInputStream(errorStream)) {
            return readToString(inputStream, resolveCharset(connection));
        } catch (IOException ex) {
            return "";
        }
    }

    private String resolveCharset(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        if (contentType != null) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.toLowerCase().startsWith("charset=")) {
                    String charset = trimmed.substring("charset=".length()).trim();
                    if (!charset.isEmpty()) {
                        return charset;
                    }
                }
            }
        }
        return defaultCharset;
    }

    private String readToString(InputStream inputStream, String charsetName) throws IOException {
        Charset charset = Charset.forName(charsetName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int read;
        while ((read = inputStream.read(data)) >= 0) {
            buffer.write(data, 0, read);
        }
        return new String(buffer.toByteArray(), charset);
    }

    public static final class Builder<T> {

        private final URL url;
        private final ResponseParser<T> parser;
        private int connectTimeoutMillis = 3000;
        private int readTimeoutMillis = 30000;
        private String requestVersionHeader = "If-None-Match";
        private String responseVersionHeader = "ETag";
        private String defaultCharset = "UTF-8";
        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder(String url, ResponseParser<T> parser) {
            if (parser == null) {
                throw new IllegalArgumentException("parser must not be null");
            }
            this.parser = parser;
            this.url = parseUrl(url);
        }

        public Builder<T> connectTimeoutMillis(int connectTimeoutMillis) {
            if (connectTimeoutMillis < 0) {
                throw new IllegalArgumentException("connectTimeoutMillis must be >= 0");
            }
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder<T> readTimeoutMillis(int readTimeoutMillis) {
            if (readTimeoutMillis < 0) {
                throw new IllegalArgumentException("readTimeoutMillis must be >= 0");
            }
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public Builder<T> requestVersionHeader(String requestVersionHeader) {
            this.requestVersionHeader = normalizeHeaderName(requestVersionHeader);
            return this;
        }

        public Builder<T> responseVersionHeader(String responseVersionHeader) {
            this.responseVersionHeader = normalizeHeaderName(responseVersionHeader);
            return this;
        }

        public Builder<T> defaultCharset(String defaultCharset) {
            if (defaultCharset == null || defaultCharset.trim().isEmpty()) {
                throw new IllegalArgumentException("defaultCharset must not be blank");
            }
            this.defaultCharset = defaultCharset;
            return this;
        }

        public Builder<T> header(String name, String value) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("header name must not be blank");
            }
            if (value == null) {
                headers.remove(name);
            } else {
                headers.put(name, value);
            }
            return this;
        }

        public Builder<T> headers(Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    header(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public HttpLongPollingConfigSource<T> build() {
            return new HttpLongPollingConfigSource<>(this);
        }

        private static String normalizeHeaderName(String header) {
            if (header == null) {
                return null;
            }
            String trimmed = header.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }

        private static URL parseUrl(String url) {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("url must not be blank");
            }
            try {
                return new URL(url);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Invalid url: " + url, ex);
            }
        }
    }
}
