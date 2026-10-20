package com.example.demoapp;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.*;

// Prawdziwy PostgreSQL i Flyway: H2 nie odtwarza blokad ani składni migracji PostgreSQL.
@Testcontainers
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "app.bootstrap.enabled=false",
    "app.scheduling.enabled=false",
    "app.jwt.secret=QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=",
    "spring.jpa.hibernate.ddl-auto=validate",
  }
)
class DemoappApplicationTests {

  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer(
    "postgres:15"
  );

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Value("${local.server.port}")
  int port;

  @Autowired
  JdbcTemplate jdbc;

  @Autowired
  JwtEncoder encoder;

  private final ObjectMapper json = new ObjectMapper();
  private String token;
  private String username;
  private long categoryId;
  private final String password = "Integration-password-2026!";

  @BeforeEach
  void user() throws Exception {
    username = "test-" + UUID.randomUUID().toString().substring(0, 12);
    var r = request(
      "POST",
      "/api/auth/register",
      Map.of(
        "username",
        username,
        "email",
        username + "@example.com",
        "password",
        password
      ),
      null
    );
    assertEquals(201, r.statusCode(), r.body());
    token = tree(r).get("accessToken").asText();
    categoryId = tree(request("GET", "/api/categories", null, token))
      .get(0)
      .get("id")
      .asLong();
  }

  @Test
  void migrationCreatesRealPostgresSchemaAndSwaggerSupportsBearer()
    throws Exception {
    assertTrue(
      jdbc.queryForObject(
        "SELECT count(*) FROM flyway_schema_history WHERE success AND version IN ('1','2')",
        Integer.class
      ) == 2
    );
    var doc = request("GET", "/v3/api-docs", null, null);
    assertEquals(200, doc.statusCode());
    var schema = tree(doc);
    assertEquals(
      "bearer",
      schema.at("/components/securitySchemes/bearerAuth/scheme").asText()
    );
    assertTrue(doc.body().contains("/api/transfers"));
    assertTrue(doc.body().contains("categoryId"));
    var swagger = request("GET", "/swagger-ui/index.html", null, null);
    assertEquals(200, swagger.statusCode());
    assertTrue(swagger.body().contains("Swagger UI"));
  }

  @Test
  void loginUsesHashedPasswordsAndDoesNotExposeTheHash() throws Exception {
    var response = request(
      "POST",
      "/api/auth/login",
      Map.of("username", username.toUpperCase(), "password", password),
      null
    );
    assertEquals(200, response.statusCode(), response.body());
    assertEquals("Bearer", tree(response).get("tokenType").asText());
    assertFalse(tree(response).get("user").has("password"));
    String stored = jdbc.queryForObject(
      "SELECT password FROM users WHERE username=?",
      String.class,
      username
    );
    assertTrue(stored.startsWith("$2"));
    assertNotEquals(password, stored);
    assertEquals(
      401,
      request(
        "POST",
        "/api/auth/login",
        Map.of("username", username, "password", "wrong"),
        null
      ).statusCode()
    );
    assertEquals(
      409,
      request(
        "POST",
        "/api/auth/register",
        Map.of(
          "username",
          username,
          "email",
          "other@example.com",
          "password",
          password
        ),
        null
      ).statusCode()
    );
    assertEquals(
      400,
      request(
        "POST",
        "/api/auth/register",
        Map.of("username", "short", "email", "bad", "password", "123"),
        null
      ).statusCode()
    );
    assertEquals(404, request("GET", "/api/users", null, token).statusCode());
    assertFalse(
      request("GET", "/api/users/me", null, token).body().contains("password")
    );
  }

  @Test
  void missingInvalidAndExpiredTokensReturn401WithoutNativeBrowserPrompt()
    throws Exception {
    for (String credential : Arrays.asList(
      null,
      "invalid.jwt.token",
      expiredToken()
    )) {
      var r = request("GET", "/api/accounts", null, credential);
      assertEquals(401, r.statusCode(), r.body());
      assertTrue(r.headers().firstValue("WWW-Authenticate").isEmpty());
      assertTrue(
        r
          .headers()
          .firstValue("Content-Type")
          .orElse("")
          .contains("application/json")
      );
    }
  }

  @Test
  void validatesAmountsAndRollsBackFailedOperations() throws Exception {
    long id = account("1000.00");
    assertEquals(
      201,
      transaction(id, "500.00", "INCOME", categoryId).statusCode()
    );
    assertEquals(
      201,
      transaction(id, "200.00", "EXPENSE", categoryId).statusCode()
    );
    balance(id, "1300.00");
    for (String amount : List.of("-1", "0", "1.001"))
      assertEquals(
        400,
        transaction(id, amount, "EXPENSE", categoryId).statusCode()
      );
    assertEquals(
      400,
      transaction(id, "10", "TRANSFER_IN", categoryId).statusCode()
    );
    assertEquals(
      400,
      request("POST", "/api/transactions", Map.of(), token).statusCode()
    );
    var malformed = raw("POST", "/api/transactions", "{broken", token);
    assertEquals(400, malformed.statusCode());
    balance(id, "1300.00");
  }

  @Test
  void transfersPersistBothSidesAndConserveTotalMoney() throws Exception {
    long source = account("100.00"),
      target = account("50.00");
    assertEquals(400, transfer(source, target, "1000").statusCode());
    assertEquals(400, transfer(source, source, "10").statusCode());
    balance(source, "100.00");
    balance(target, "50.00");
    var r = transfer(source, target, "20");
    assertEquals(201, r.statusCode(), r.body());
    long transferId = tree(r).get("id").asLong();
    balance(source, "80.00");
    balance(target, "70.00");
    assertEquals(
      2,
      jdbc.queryForObject(
        "SELECT count(*) FROM transactions WHERE transfer_id=?",
        Integer.class,
        transferId
      )
    );
    assertEquals(
      2,
      jdbc.queryForObject(
        "SELECT count(DISTINCT type) FROM transactions WHERE transfer_id=?",
        Integer.class,
        transferId
      )
    );
    assertEquals(1, tree(request("GET", "/api/transfers", null, token)).size());
  }

  @Test
  void accountArchiveAndCategoryRenamePreserveHistory() throws Exception {
    long id = account("0");
    transaction(id, "10", "INCOME", categoryId);
    assertEquals(
      409,
      request("DELETE", "/api/accounts/" + id, null, token).statusCode()
    );
    transaction(id, "10", "EXPENSE", categoryId);
    String name = "Renamed-" + UUID.randomUUID();
    assertEquals(
      200,
      request(
        "PUT",
        "/api/categories/" + categoryId,
        Map.of("name", name),
        token
      ).statusCode()
    );
    assertTrue(
      request("GET", "/api/transactions", null, token).body().contains(name)
    );
    assertEquals(
      204,
      request(
        "DELETE",
        "/api/categories/" + categoryId,
        null,
        token
      ).statusCode()
    );
    assertEquals(400, transaction(id, "1", "EXPENSE", categoryId).statusCode());
    assertEquals(
      204,
      request("DELETE", "/api/accounts/" + id, null, token).statusCode()
    );
    assertEquals(0, tree(request("GET", "/api/accounts", null, token)).size());
    assertEquals(
      1,
      tree(
        request("GET", "/api/accounts?includeArchived=true", null, token)
      ).size()
    );
    assertEquals(
      2,
      tree(request("GET", "/api/transactions", null, token)).size()
    );
    assertEquals(
      200,
      request(
        "POST",
        "/api/accounts/" + id + "/restore",
        Map.of(),
        token
      ).statusCode()
    );
  }

  @Test
  void budgetsUseActualExpensesAndGoalsTrackContributions() throws Exception {
    long id = account("500");
    transaction(id, "75", "EXPENSE", categoryId);
    String month = YearMonth.now().toString();
    var body = Map.of(
      "categoryId",
      categoryId,
      "monthlyLimit",
      100,
      "month",
      month
    );
    var budget = request("POST", "/api/budgets", body, token);
    assertEquals(201, budget.statusCode(), budget.body());
    assertEquals(
      409,
      request("POST", "/api/budgets", body, token).statusCode()
    );
    var b = tree(
      request("GET", "/api/budgets?month=" + month, null, token)
    ).get(0);
    assertEquals(75, b.get("spent").asInt());
    assertEquals(25, b.get("remaining").asInt());
    var g = request(
      "POST",
      "/api/goals",
      Map.of("name", "Trip", "targetAmount", 1000, "currentAmount", 20),
      token
    );
    assertEquals(201, g.statusCode(), g.body());
    long goal = tree(g).get("id").asLong();
    var contribution = request(
      "POST",
      "/api/goals/" + goal + "/contributions",
      Map.of("amount", 30),
      token
    );
    assertEquals(50, tree(contribution).get("currentAmount").asInt());
    balance(id, "425.00");
    assertEquals(
      204,
      request("DELETE", "/api/goals/" + goal, null, token).statusCode()
    );
    assertEquals(
      204,
      request(
        "DELETE",
        "/api/budgets/" + tree(budget).get("id").asLong(),
        null,
        token
      ).statusCode()
    );
  }

  @Test
  void recurringPaymentPostsDueOccurrenceOnlyOnce() throws Exception {
    long id = account("100");
    var r = request(
      "POST",
      "/api/recurring-payments",
      Map.of(
        "title",
        "Subscription",
        "amount",
        10,
        "frequency",
        "MONTHLY",
        "nextDueDate",
        LocalDate.now().toString(),
        "accountId",
        id,
        "categoryId",
        categoryId,
        "type",
        "EXPENSE",
        "active",
        true
      ),
      token
    );
    assertEquals(201, r.statusCode(), r.body());
    long payment = tree(r).get("id").asLong();
    var first = request(
      "POST",
      "/api/recurring-payments/" + payment + "/execute",
      Map.of(),
      token
    );
    assertEquals(200, first.statusCode(), first.body());
    assertEquals(1, tree(first).get("posted").asInt());
    assertEquals(
      0,
      tree(
        request(
          "POST",
          "/api/recurring-payments/" + payment + "/execute",
          Map.of(),
          token
        )
      )
        .get("posted")
        .asInt()
    );
    balance(id, "90.00");
    assertEquals(
      1,
      jdbc.queryForObject(
        "SELECT count(*) FROM transactions WHERE recurring_payment_id=?",
        Integer.class,
        payment
      )
    );
  }

  @Test
  void usersCannotReadOrChangeEachOthersResources() throws Exception {
    long id = account("100");
    var ownGoal = request(
      "POST",
      "/api/goals",
      Map.of("name", "Private", "targetAmount", 100, "currentAmount", 0),
      token
    );
    long goal = tree(ownGoal).get("id").asLong();
    String ownToken = token;
    var other = registerOther();
    token = other;
    assertEquals(0, tree(request("GET", "/api/accounts", null, token)).size());
    assertEquals(
      0,
      tree(request("GET", "/api/transactions", null, token)).size()
    );
    assertEquals(0, tree(request("GET", "/api/goals", null, token)).size());
    assertEquals(
      404,
      request(
        "PUT",
        "/api/accounts/" + id,
        Map.of("name", "Hijack", "accountType", "PLN"),
        token
      ).statusCode()
    );
    assertEquals(
      404,
      request("DELETE", "/api/accounts/" + id, null, token).statusCode()
    );
    assertEquals(
      404,
      request(
        "DELETE",
        "/api/categories/" + categoryId,
        null,
        token
      ).statusCode()
    );
    assertEquals(
      404,
      request(
        "POST",
        "/api/goals/" + goal + "/contributions",
        Map.of("amount", 1),
        token
      ).statusCode()
    );
    long ownOther = account("50");
    assertEquals(
      404,
      transaction(ownOther, "10", "EXPENSE", categoryId).statusCode()
    );
    assertEquals(404, transfer(id, ownOther, "10").statusCode());
    token = ownToken;
    balance(id, "100.00");
  }

  @Test
  void concurrentExpensesDoNotLoseBalanceUpdates() throws Exception {
    long id = account("1000");
    try (var pool = Executors.newFixedThreadPool(6)) {
      List<Future<Integer>> results = new ArrayList<>();
      for (int i = 0; i < 12; i++) results.add(
        pool.submit(() ->
          transaction(id, "1", "EXPENSE", categoryId).statusCode()
        )
      );
      for (var result : results)
        assertEquals(201, result.get(25, TimeUnit.SECONDS));
    }
    balance(id, "988.00");
  }

  @Test
  void forecastsAreProtectedAndValidateMonth() throws Exception {
    assertEquals(
      401,
      request("GET", "/api/forecasts", null, null).statusCode()
    );
    assertEquals(
      400,
      request("GET", "/api/forecasts?month=bad", null, token).statusCode()
    );
    var r = request("GET", "/api/forecasts?month=2020-01", null, token);
    assertEquals(200, r.statusCode(), r.body());
    assertTrue(tree(r).get("predictedExpenses").isNull());
  }

  private String registerOther() throws Exception {
    String name = "other-" + UUID.randomUUID().toString().substring(0, 10);
    return tree(
      request(
        "POST",
        "/api/auth/register",
        Map.of(
          "username",
          name,
          "email",
          name + "@example.com",
          "password",
          password
        ),
        null
      )
    )
      .get("accessToken")
      .asText();
  }

  private long account(String balance) throws Exception {
    var r = request(
      "POST",
      "/api/accounts",
      Map.of(
        "name",
        "Account-" + UUID.randomUUID(),
        "accountType",
        "PLN",
        "balance",
        new BigDecimal(balance)
      ),
      token
    );
    assertEquals(201, r.statusCode(), r.body());
    return tree(r).get("id").asLong();
  }

  private HttpResponse<String> transaction(
    long account,
    String amount,
    String type,
    long category
  ) throws Exception {
    return request(
      "POST",
      "/api/transactions",
      Map.of(
        "accountId",
        account,
        "amount",
        new BigDecimal(amount),
        "type",
        type,
        "date",
        LocalDate.now().toString(),
        "categoryId",
        category,
        "description",
        "Test operation"
      ),
      token
    );
  }

  private HttpResponse<String> transfer(long source, long target, String amount)
    throws Exception {
    return request(
      "POST",
      "/api/transfers",
      Map.of(
        "sourceAccountId",
        source,
        "targetAccountId",
        target,
        "amount",
        new BigDecimal(amount),
        "date",
        LocalDate.now().toString(),
        "description",
        "Savings transfer"
      ),
      token
    );
  }

  private void balance(long id, String expected) throws Exception {
    for (JsonNode row : tree(request("GET", "/api/accounts", null, token)))
      if (row.get("id").asLong() == id) {
        assertEquals(
          0,
          new BigDecimal(expected).compareTo(
            new BigDecimal(row.get("balance").asText())
          )
        );
        return;
      }
    fail("Missing account " + id);
  }

  private String expiredToken() {
    var claims = JwtClaimsSet.builder()
      .issuer("dukat")
      .subject(username)
      .issuedAt(Instant.now().minusSeconds(7200))
      .expiresAt(Instant.now().minusSeconds(3600))
      .build();
    return encoder
      .encode(
        JwtEncoderParameters.from(
          JwsHeader.with(MacAlgorithm.HS256).build(),
          claims
        )
      )
      .getTokenValue();
  }

  private JsonNode tree(HttpResponse<String> r) {
    return json.readTree(r.body());
  }

  private HttpResponse<String> request(
    String method,
    String path,
    Object body,
    String bearer
  ) throws Exception {
    return raw(
      method,
      path,
      body == null ? null : json.writeValueAsString(body),
      bearer
    );
  }

  private HttpResponse<String> raw(
    String method,
    String path,
    String body,
    String bearer
  ) throws Exception {
    var builder = HttpRequest.newBuilder(
      URI.create("http://127.0.0.1:" + port + path)
    )
      .timeout(Duration.ofSeconds(25))
      .header("Content-Type", "application/json");
    if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
    try (var client = HttpClient.newHttpClient()) {
      return client.send(
        builder
          .method(
            method,
            body == null
              ? HttpRequest.BodyPublishers.noBody()
              : HttpRequest.BodyPublishers.ofString(body)
          )
          .build(),
        HttpResponse.BodyHandlers.ofString()
      );
    }
  }
}
