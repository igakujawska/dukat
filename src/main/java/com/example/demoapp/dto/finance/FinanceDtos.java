package com.example.demoapp.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class FinanceDtos {

  private FinanceDtos() {}

  @Schema(description = "Edycja nazwy i rodzaju konta, bez zmiany salda.")
  public record AccountEdit(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nazwa widoczna w aplikacji.")
    String name,
    @NotBlank
    @Size(max = 50)
    @Schema(description = "Rodzaj rachunku, np. Konto osobiste.")
    String accountType
  ) {}

  @Schema(description = "Utworzenie, zmiana nazwy lub przywrócenie kategorii.")
  public record CategoryRequest(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nazwa widoczna w aplikacji.")
    String name
  ) {}

  @Schema(description = "Konto zalogowanego użytkownika.")
  public record AccountResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Nazwa widoczna w aplikacji.") String name,
    @Schema(description = "Rodzaj rachunku, np. Konto osobiste.")
    String accountType,
    @Schema(
      description = "Saldo w PLN, dokładność do dwóch miejsc po przecinku."
    )
    BigDecimal balance,
    @Schema(
      description = "Konto zamknięte do nowych operacji; historia jest zachowana."
    )
    boolean archived,
    @Schema(description = "Wersja rekordu wykrywa równoczesne modyfikacje.")
    long version
  ) {}

  @Schema(
    description = "Kategoria użytkownika; nieaktywne wpisy pozostają w historii."
  )
  public record CategoryResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Nazwa widoczna w aplikacji.") String name,
    @Schema(description = "Czy wpis jest aktywny.") boolean active
  ) {}

  @Schema(description = "Limit wydatków kategorii na miesiąc.")
  public record BudgetRequest(
    @NotNull
    @Schema(description = "Identyfikator własnej, aktywnej kategorii.")
    Long categoryId,
    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 2)
    @Schema(description = "Miesięczny limit wydatków w PLN.")
    BigDecimal monthlyLimit,
    @NotBlank
    @Pattern(regexp = "[0-9]{4}-(0[1-9]|1[0-2])")
    @Schema(description = "Miesiąc w formacie YYYY-MM.")
    String month
  ) {}

  @Schema(description = "Limit i wykonanie z rzeczywistych transakcji.")
  public record BudgetResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Identyfikator własnej, aktywnej kategorii.")
    Long categoryId,
    @Schema(description = "Aktualna nazwa kategorii.") String category,
    @Schema(description = "Miesiąc w formacie YYYY-MM.") String month,
    @Schema(description = "Miesięczny limit wydatków w PLN.")
    BigDecimal monthlyLimit,
    @Schema(
      description = "Suma wydatków EXPENSE w kategorii i miesiącu; bez przelewów."
    )
    BigDecimal spent,
    @Schema(description = "Limit pomniejszony o wydatki; może być ujemny.")
    BigDecimal remaining
  ) {}

  @Schema(description = "Cel i ręcznie prowadzony postęp oszczędzania.")
  public record GoalRequest(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nazwa widoczna w aplikacji.")
    String name,
    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 2)
    @Schema(description = "Docelowa kwota celu w PLN.")
    BigDecimal targetAmount,
    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 2)
    @Schema(
      description = "Ręcznie ewidencjonowany postęp; nie zmienia sald kont."
    )
    BigDecimal currentAmount,
    @Schema(description = "Opcjonalny termin celu.") LocalDate targetDate
  ) {}

  @Schema(description = "Postęp celu; nie jest oddzielnym rachunkiem.")
  public record GoalResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Nazwa widoczna w aplikacji.") String name,
    @Schema(description = "Docelowa kwota celu w PLN.") BigDecimal targetAmount,
    @Schema(
      description = "Ręcznie ewidencjonowany postęp; nie zmienia sald kont."
    )
    BigDecimal currentAmount,
    @Schema(description = "Opcjonalny termin celu.") LocalDate targetDate
  ) {}

  @Schema(description = "Dodatni przyrost ewidencjonowanego postępu.")
  public record ContributionRequest(
    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 2)
    @Schema(
      description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
    )
    BigDecimal amount
  ) {}

  @Schema(
    description = "Szablon cyklicznego zapisu, bez komunikacji z bankiem."
  )
  public record RecurringRequest(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Tytuł cyklicznego zapisu w historii.")
    String title,
    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 2)
    @Schema(
      description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
    )
    BigDecimal amount,
    @NotBlank
    @Pattern(regexp = "WEEKLY|MONTHLY|YEARLY")
    @Schema(description = "WEEKLY, MONTHLY albo YEARLY.")
    String frequency,
    @NotNull
    @Schema(description = "Najbliższy termin do zaksięgowania, także zaległy.")
    LocalDate nextDueDate,
    @NotNull
    @Schema(description = "Identyfikator własnego konta.")
    Long accountId,
    @NotNull
    @Schema(description = "Identyfikator własnej, aktywnej kategorii.")
    Long categoryId,
    @NotBlank
    @Pattern(regexp = "INCOME|EXPENSE")
    @Schema(
      description = "Typ operacji; ręczny zapis dopuszcza tylko INCOME lub EXPENSE."
    )
    String type,
    @Schema(description = "Czy wpis jest aktywny.") boolean active
  ) {}

  @Schema(description = "Terminy i stan automatycznego księgowania.")
  public record RecurringResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Tytuł cyklicznego zapisu w historii.") String title,
    @Schema(
      description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
    )
    BigDecimal amount,
    @Schema(description = "WEEKLY, MONTHLY albo YEARLY.") String frequency,
    @Schema(description = "Najbliższy termin do zaksięgowania, także zaległy.")
    LocalDate nextDueDate,
    @Schema(description = "Identyfikator własnego konta.") Long accountId,
    @Schema(description = "Nazwa konta.") String accountName,
    @Schema(description = "Identyfikator własnej, aktywnej kategorii.")
    Long categoryId,
    @Schema(description = "Aktualna nazwa kategorii.") String category,
    @Schema(
      description = "Typ operacji; ręczny zapis dopuszcza tylko INCOME lub EXPENSE."
    )
    String type,
    @Schema(description = "Czy wpis jest aktywny.") boolean active,
    @Schema(description = "Ostatni błąd harmonogramu; null gdy brak błędu.")
    String lastError
  ) {}

  @Schema(
    description = "Przelew zapisany atomowo wraz z dwoma wpisami historii."
  )
  public record TransferResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Identyfikator własnego konta źródłowego.")
    Long sourceAccountId,
    @Schema(description = "Nazwa konta źródłowego.") String sourceAccountName,
    @Schema(description = "Identyfikator innego własnego konta docelowego.")
    Long targetAccountId,
    @Schema(description = "Nazwa konta docelowego.") String targetAccountName,
    @Schema(
      description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
    )
    BigDecimal amount,
    @Schema(description = "Data operacji w formacie YYYY-MM-DD.")
    LocalDate date,
    @Schema(description = "Opis operacji wyświetlany w historii.")
    String description
  ) {}

  @Schema(description = "Nazwa zasobu.")
  public record NameRequest(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nazwa widoczna w aplikacji.")
    String name
  ) {}

  @Schema(description = "Gospodarstwo w obrębie profilu użytkownika.")
  public record HouseholdResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(description = "Nazwa widoczna w aplikacji.") String name
  ) {}
}
