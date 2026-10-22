package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import vn.edu.group12.qldt.config.nhom.NhomBaoCaoConfiguration;
import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.storage.nhom.*;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

class R2ReportStorageTests {
    @Test
    void sdkS3ThatGuiPutGetDeleteVaXuLy404() throws Exception {
        var objects = new ConcurrentHashMap<String, byte[]>();
        var authorization = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/",
                exchange -> {
                    String key = exchange.getRequestURI().getPath();
                    authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                    int status = 200;
                    byte[] response = new byte[0];
                    switch (exchange.getRequestMethod()) {
                        case "PUT" -> {
                            objects.put(key, exchange.getRequestBody().readAllBytes());
                            exchange.getResponseHeaders().set("ETag", "\"test-etag\"");
                        }
                        case "GET" -> {
                            if (objects.containsKey(key)) {
                                response = objects.get(key);
                                exchange.getResponseHeaders()
                                        .set("Content-Type", "application/pdf");
                            } else {
                                status = 404;
                                response =
                                        "<Error><Code>NoSuchKey</Code><Message>Không tìm thấy</Message></Error>"
                                                .getBytes();
                                exchange.getResponseHeaders()
                                        .set("Content-Type", "application/xml");
                            }
                        }
                        case "DELETE" -> {
                            objects.remove(key);
                            status = 204;
                        }
                        default -> status = 405;
                    }
                    exchange.sendResponseHeaders(status, status == 204 ? -1 : response.length);
                    if (status != 204) exchange.getResponseBody().write(response);
                    exchange.close();
                });
        server.start();
        try (var client =
                NhomBaoCaoConfiguration.taoClient(
                        URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                        "test-key",
                        "test-secret",
                        "auto")) {
            var storage = new R2ReportStorage(client, "private-test");
            byte[] bytes = NhomTestSupport.pdf("SDK");
            storage.put("bao-cao/test.pdf", bytes);
            assertThat(objects.get("/private-test/bao-cao/test.pdf")).isEqualTo(bytes);
            assertThat(authorization.get()).startsWith("AWS4-HMAC-SHA256 ");
            assertThat(storage.get("bao-cao/test.pdf", 10000)).isEqualTo(bytes);
            assertThatThrownBy(() -> storage.get("bao-cao/test.pdf", 5))
                    .isInstanceOfSatisfying(
                            NghiepVuNhomException.class,
                            e -> assertThat(e.getLoai()).isEqualTo(NghiepVuNhomException.Loai.LUU_TRU));
            storage.delete("bao-cao/test.pdf");
            assertThat(objects).isEmpty();
            assertThatThrownBy(() -> storage.get("bao-cao/test.pdf", 10000))
                    .isInstanceOfSatisfying(
                            NghiepVuNhomException.class,
                            e ->
                                    assertThat(e.getLoai())
                                            .isEqualTo(NghiepVuNhomException.Loai.KHONG_TIM_THAY));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void khongChoTraversalVaKhongGuiYeuCauRaNgoai() {
        for (String key :
                new String[] {
                    "../a.pdf",
                    "/a.pdf",
                    "a/../b.pdf",
                    "a\\b.pdf",
                    "https://public/a.pdf",
                    "a//b.pdf"
                })
            assertThatThrownBy(() -> ReportStorageService.kiemTraKhoa(key))
                    .isInstanceOf(NghiepVuNhomException.class);
    }
}
