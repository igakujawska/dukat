package com.example.demoapp.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Wspólne odpowiedzi błędów dla kontrolerów, zamiast powielania try/catch.
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(
    GlobalExceptionHandler.class
  );

  @ExceptionHandler(MethodArgumentNotValidException.class) // Błędy @Valid -> HTTP 400 i mapa pole: komunikat.
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
    MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
      .getFieldErrors()
      .forEach(error ->
        errors.putIfAbsent(error.getField(), error.getDefaultMessage())
      );
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    IllegalStateException.class,
  })
  public ResponseEntity<String> handleInvalidOperation(RuntimeException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleUnreadableRequest(
    HttpMessageNotReadableException ex
  ) {
    return ResponseEntity.badRequest().body(
      "Niepoprawny JSON lub format danych w żądaniu."
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataConflict(
    DataIntegrityViolationException ex
  ) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
      "Dane naruszają ograniczenia bazy, np. unikalność nazwy."
    );
  }

  @ExceptionHandler(
    org.springframework.web.server.ResponseStatusException.class
  )
  public ResponseEntity<Map<String, String>> handleStatus(
    org.springframework.web.server.ResponseStatusException ex
  ) {
    return ResponseEntity.status(ex.getStatusCode()).body(
      Map.of(
        "message",
        ex.getReason() == null
          ? "Nie udało się wykonać operacji"
          : ex.getReason()
      )
    );
  }

  @ExceptionHandler(
    org.springframework.security.core.AuthenticationException.class
  )
  public ResponseEntity<Map<String, String>> handleAuthentication(
    org.springframework.security.core.AuthenticationException ex
  ) {
    return ResponseEntity.status(401).body(
      Map.of("message", "Nieprawidłowy login lub hasło.")
    );
  }

  @ExceptionHandler({
    org.springframework.dao.OptimisticLockingFailureException.class,
    org.springframework.dao.PessimisticLockingFailureException.class,
  })
  public ResponseEntity<Map<String, String>> handleConcurrentChange(
    RuntimeException ex
  ) {
    return ResponseEntity.status(409).body(
      Map.of(
        "message",
        "Dane zmieniły się podczas zapisu. Odśwież widok i spróbuj ponownie."
      )
    );
  }

  @ExceptionHandler({
    java.time.DateTimeException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
  })
  public ResponseEntity<Map<String, String>> handleFormat(Exception ex) {
    return ResponseEntity.badRequest().body(
      Map.of("message", "Niepoprawny format parametru, daty lub miesiąca.")
    );
  }

  @ExceptionHandler(
    org.springframework.web.servlet.resource.NoResourceFoundException.class
  )
  public ResponseEntity<Map<String, String>> handleMissingResource(
    Exception ex
  ) {
    return ResponseEntity.status(404).body(
      Map.of("message", "Nie znaleziono zasobu.")
    );
  }

  @ExceptionHandler(
    org.springframework.security.access.AccessDeniedException.class
  )
  public ResponseEntity<Map<String, String>> handleForbidden(Exception ex) {
    return ResponseEntity.status(403).body(
      Map.of("message", "Brak dostępu do operacji.")
    );
  }

  @ExceptionHandler(
    org.springframework.web.HttpRequestMethodNotSupportedException.class
  )
  public ResponseEntity<Map<String, String>> handleMethod(Exception ex) {
    return ResponseEntity.status(405).body(
      Map.of("message", "Ta metoda HTTP nie jest obsługiwana.")
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGeneral(Exception ex) {
    log.error("Nieobsłużony błąd podczas obsługi żądania", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      "Wystąpił błąd serwera. Szczegóły są dostępne w logu backendu."
    );
  }
}
