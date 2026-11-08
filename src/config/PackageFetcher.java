package config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for downloading and decompressing package indices.
 */
public final class PackageFetcher {
    private static final int GZIP_HEADER_MIN_LENGTH = 2;
    private static final int GZIP_MAGIC_BYTE_1 = 0x1f;
    private static final int GZIP_MAGIC_BYTE_2 = 0x8b;
    private static final int BYTE_MASK = 0xff;

    private static final int HTTP_OK = 200;

    private PackageFetcher() {
    }

    public static String fetch(String location) throws ConfigException {
        try {
            byte[] raw;
            if (location.startsWith("http://") || location.startsWith("https://")) {
                raw = download(location);
            } else {
                raw = Files.readAllBytes(Path.of(location));
            }

            if (isGzip(raw)) {
                raw = gunzip(raw);
            }

            return new String(raw, StandardCharsets.UTF_8);
        } catch (IOException | InterruptedException e) {
            throw new ConfigException("Failed to fetch package index from '"
                    + location + "': " + e.getMessage(), e);
        }
    }

    private static byte[] download(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != HTTP_OK) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    private static boolean isGzip(byte[] data) {
        return data.length >= GZIP_HEADER_MIN_LENGTH
                && (data[0] & BYTE_MASK) == GZIP_MAGIC_BYTE_1
                && (data[1] & BYTE_MASK) == GZIP_MAGIC_BYTE_2;
    }

    private static byte[] gunzip(byte[] compressed) throws IOException {
        try (InputStream gz = new GZIPInputStream(new java.io.ByteArrayInputStream(compressed));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            gz.transferTo(out);
            return out.toByteArray();
        }
    }
}