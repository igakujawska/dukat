package com.example.demoapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:dukat_openapi_test;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.security.user.name=integration-test",
                "spring.security.user.password=integration-test-password"
        }
)
class DemoappApplicationTests {

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void openApiDocumentIncludesTransactions() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"openapi\""), response.body());
        assertTrue(response.body().contains("\"/api/transactions\""), response.body());
    }

    @Test
    void swaggerUiIsAvailableWithoutAuthentication() throws Exception {
        HttpResponse<String> response = get("/swagger-ui/index.html");

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("Swagger UI"), response.body());
    }

    @Test
    void accountsRequireAuthentication() throws Exception {
        assertEquals(401, get("/api/accounts").statusCode());
    }

    @Test
    void accountCreationValidatesItsInputAndRejectsDuplicateNames() throws Exception {
        HttpResponse<String> invalid = authenticatedRequest("POST", "/api/accounts",
                "{\"name\":\" \",\"accountType\":\"PLN\",\"balance\":-1}");
        assertEquals(400, invalid.statusCode(), invalid.body());
        assertTrue(json.readTree(invalid.body()).has("name"), invalid.body());
        assertTrue(json.readTree(invalid.body()).has("balance"), invalid.body());
        String body = "{\"name\":\"Unique-%s\",\"accountType\":\"PLN\",\"balance\":0}".formatted(UUID.randomUUID());
        assertEquals(201, authenticatedRequest("POST", "/api/accounts", body).statusCode());
        assertEquals(409, authenticatedRequest("POST", "/api/accounts", body).statusCode());
    }

    @Test
    void incomeAndExpenseUpdatePersistedBalanceAndRejectInvalidRequests() throws Exception {
        long accountId = createAccount("1000.00");
        assertEquals(201, createTransaction(accountId, "500.00", "INCOME").statusCode());
        assertEquals(201, createTransaction(accountId, "200.00", "EXPENSE").statusCode());
        assertBalance(accountId, "1300.00");

        assertEquals(400, createTransaction(accountId, "-10.00", "EXPENSE").statusCode());
        assertEquals(400, createTransaction(accountId, "10.00", "UNKNOWN").statusCode());
        assertBalance(accountId, "1300.00");

        HttpResponse<String> missingFields = authenticatedRequest("POST", "/api/transactions", "{}");
        assertEquals(400, missingFields.statusCode(), missingFields.body());
        assertTrue(json.readTree(missingFields.body()).has("amount"), missingFields.body());
        assertBalance(accountId, "1300.00");

        HttpResponse<String> history = authenticatedRequest("GET", "/api/transactions", null);
        assertEquals(200, history.statusCode(), history.body());
        assertTrue(history.body().contains("INCOME"), history.body());
        assertTrue(history.body().contains("EXPENSE"), history.body());
    }

    @Test
    void invalidJsonReturnsBadRequest() throws Exception {
        assertEquals(400, authenticatedRequest("POST", "/api/transactions", "{broken").statusCode());
    }

    @Test
    void transferRejectsNegativeAmountAndInsufficientFundsWithoutChangingBalances() throws Exception {
        long sourceId = createAccount("100.00");
        long targetId = createAccount("50.00");
        String transfer = "{\"sourceAccountId\":%d,\"targetAccountId\":%d,\"amount\":%s}";

        assertEquals(400, authenticatedRequest("POST", "/api/transfers",
                transfer.formatted(sourceId, targetId, "-10.00")).statusCode());
        assertEquals(400, authenticatedRequest("POST", "/api/transfers",
                transfer.formatted(sourceId, targetId, "1000.00")).statusCode());
        assertBalance(sourceId, "100.00");
        assertBalance(targetId, "50.00");

        assertEquals(200, authenticatedRequest("POST", "/api/transfers",
                transfer.formatted(sourceId, targetId, "20.00")).statusCode());
        assertBalance(sourceId, "80.00");
        assertBalance(targetId, "70.00");
    }

    private long createAccount(String initialBalance) throws Exception {
        String body = "{\"name\":\"Test-%s\",\"accountType\":\"PLN\",\"balance\":%s}"
                .formatted(UUID.randomUUID(), initialBalance);
        HttpResponse<String> response = authenticatedRequest("POST", "/api/accounts", body);
        assertEquals(201, response.statusCode(), response.body());
        return json.readTree(response.body()).get("id").asLong();
    }

    private HttpResponse<String> createTransaction(long accountId, String amount, String type) throws Exception {
        String body = """
                {"accountId":%d,"amount":%s,"type":"%s","date":"2026-10-20",
                 "category":"Test","description":"Operacja testowa"}
                """.formatted(accountId, amount, type);
        return authenticatedRequest("POST", "/api/transactions", body);
    }

    private void assertBalance(long accountId, String expected) throws Exception {
        HttpResponse<String> response = authenticatedRequest("GET", "/api/accounts", null);
        assertEquals(200, response.statusCode(), response.body());
        for (JsonNode account : json.readTree(response.body())) {
            if (account.get("id").asLong() == accountId) {
                assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(account.get("balance").asText())));
                return;
            }
        }
        throw new AssertionError("Brak konta testowego " + accountId);
    }

    private HttpResponse<String> authenticatedRequest(String method, String path, String body) throws Exception {
        String credentials = Base64.getEncoder().encodeToString(
                "integration-test:integration-test-password".getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

}
