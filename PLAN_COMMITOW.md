# Małe commity — co zaznaczać

Plan dotyczy OBECNYCH zmian po 67646e8, a nie wcześniejszych commitów motywu/prognozy. Nie wykonano stagingu, commita ani push. Każda lista poniżej oznacza osobną grupę w oknie Commit. Zostaw resztę odznaczoną.

Zmiana DTO i właścicieli jest przekrojowa: grupy backendu zależą od siebie, a nowy frontend wymaga nowego API. Pełny zestaw testów uruchom po całej serii; nie przedstawiaj pośredniego etapu jako gotowej wersji aplikacji. Podział jest tematyczny do przeglądu, nie oznacza niezależnych wdrożeń.

Przed każdym commitem sprawdź diff wybranych plików i, przy staging area, `git diff --cached`. Nie używaj `git add .`. W IntelliJ zaznaczaj wymienione pliki; dla usuniętego pustego V!_init.sql zaznacz także usunięcie.

## 1. Konfiguracja i migracje

Wiadomość: `build: configure environment and Flyway migrations`

- `.env.example`
- `src/main/resources/db/migration/V1__legacy_schema.sql`
- `src/main/resources/db/migration/V2__ownership_and_financial_integrity.sql`
- `src/main/resources/db/migration/V3__case_insensitive_user_identity.sql`
- `src/main/resources/db/migration/V4__recurring_calendar_anchor.sql`
- `.gitignore`
- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/resources/db/migration/V!_init.sql`

## 2. Encje i typy domeny

Wiadomość: `refactor: model owned financial resources`

- `src/main/java/com/example/demoapp/model/OwnedResource.java`
- `src/main/java/com/example/demoapp/model/Transfer.java`
- `src/main/java/com/example/demoapp/model/Account.java`
- `src/main/java/com/example/demoapp/model/Budget.java`
- `src/main/java/com/example/demoapp/model/Category.java`
- `src/main/java/com/example/demoapp/model/Goal.java`
- `src/main/java/com/example/demoapp/model/Household.java`
- `src/main/java/com/example/demoapp/model/RecurringPayment.java`
- `src/main/java/com/example/demoapp/model/Transaction.java`
- `src/main/java/com/example/demoapp/model/TransactionType.java`
- `src/main/java/com/example/demoapp/model/User.java`

## 3. Repozytoria i blokady

Wiadomość: `feat: query and lock resources by owner`

- `src/main/java/com/example/demoapp/repository/TransferRepository.java`
- `src/main/java/com/example/demoapp/repository/AccountRepository.java`
- `src/main/java/com/example/demoapp/repository/BudgetRepository.java`
- `src/main/java/com/example/demoapp/repository/CategoryRepository.java`
- `src/main/java/com/example/demoapp/repository/GoalRepository.java`
- `src/main/java/com/example/demoapp/repository/HouseholdRepository.java`
- `src/main/java/com/example/demoapp/repository/RecurringPaymentRepository.java`
- `src/main/java/com/example/demoapp/repository/TransactionRepository.java`
- `src/main/java/com/example/demoapp/repository/UserRepository.java`

## 4. Logowanie, profil i konfiguracja bezpieczeństwa

Wiadomość: `feat: authenticate users with BCrypt and JWT`

- `src/main/java/com/example/demoapp/controller/AuthController.java`
- `src/main/java/com/example/demoapp/dto/auth/AuthDtos.java`
- `src/main/java/com/example/demoapp/service/AuthService.java`
- `src/main/java/com/example/demoapp/service/CurrentUser.java`
- `src/main/java/com/example/demoapp/config/JwtConfig.java`
- `src/main/java/com/example/demoapp/config/OpenApiConfig.java`
- `src/main/java/com/example/demoapp/config/SecurityConfig.java`
- `src/main/java/com/example/demoapp/controller/UserController.java`
- `src/main/java/com/example/demoapp/init/DemoDataLoader.java`

## 5. Konta, kategorie i wspólne kontrakty finansowe

Wiadomość: `feat: manage accounts and persistent categories`

- `src/main/java/com/example/demoapp/controller/CategoryController.java`
- `src/main/java/com/example/demoapp/dto/finance/FinanceDtos.java`
- `src/main/java/com/example/demoapp/service/CategoryService.java`
- `src/main/java/com/example/demoapp/controller/AccountController.java`
- `src/main/java/com/example/demoapp/dto/account/CreateAccountRequest.java`
- `src/main/java/com/example/demoapp/exception/GlobalExceptionHandler.java`
- `src/main/java/com/example/demoapp/service/AccountService.java`

## 6. Historia, saldo i izolacja prognozy

Wiadomość: `fix: post ledger entries atomically and isolate forecasts`

- `src/main/java/com/example/demoapp/service/FinancialLedger.java`
- `src/main/java/com/example/demoapp/controller/TransactionController.java`
- `src/main/java/com/example/demoapp/dto/forecast/ForecastResponse.java`
- `src/main/java/com/example/demoapp/dto/transaction/CreateTransactionRequest.java`
- `src/main/java/com/example/demoapp/dto/transaction/TransactionResponse.java`
- `src/main/java/com/example/demoapp/service/ForecastService.java`
- `src/main/java/com/example/demoapp/service/TransactionService.java`

## 7. Przelewy

Wiadomość: `feat: persist atomic transfers and both history entries`

- `src/main/java/com/example/demoapp/controller/TransferController.java`
- `src/main/java/com/example/demoapp/dto/transfer/CreateTransferRequest.java`
- `src/main/java/com/example/demoapp/service/TransferService.java`

## 8. Planowanie i harmonogram

Wiadomość: `feat: manage budgets goals and recurring execution`

- `src/main/java/com/example/demoapp/init/RecurringScheduler.java`
- `src/main/java/com/example/demoapp/service/RecurringProcessor.java`
- `src/main/java/com/example/demoapp/DemoappApplication.java`
- `src/main/java/com/example/demoapp/controller/BudgetController.java`
- `src/main/java/com/example/demoapp/controller/DashboardController.java`
- `src/main/java/com/example/demoapp/controller/GoalController.java`
- `src/main/java/com/example/demoapp/controller/HouseholdController.java`
- `src/main/java/com/example/demoapp/controller/RecurringPaymentController.java`
- `src/main/java/com/example/demoapp/service/BudgetService.java`
- `src/main/java/com/example/demoapp/service/DashboardService.java`
- `src/main/java/com/example/demoapp/service/GoalService.java`
- `src/main/java/com/example/demoapp/service/HouseholdService.java`
- `src/main/java/com/example/demoapp/service/RecurringPaymentService.java`

## 9. Kontenery, porty i skrypty uruchamiania

Wiadomość: `build: run the complete stack with Docker Compose`

- `.dockerignore`
- `Dockerfile`
- `dukat-ui/.dockerignore`
- `dukat-ui/Dockerfile`
- `dukat-ui/nginx.conf`
- `scripts/Initialize-Dukat.ps1`
- `scripts/Start-Dukat.ps1`
- `scripts/Stop-Dukat.ps1`
- `docker-compose.yml`

## 10. Style i nawigacja

Wiadomość: `style: organize responsive sapphire layouts`

- `dukat-ui/src/app/components/planning.css`
- `dukat-ui/src/app/styles/charts.css`
- `dukat-ui/src/app/styles/navigation.css`
- `dukat-ui/src/app/styles/responsive.css`
- `dukat-ui/src/app/app.css`
- `dukat-ui/src/app/styles/panels.css`
- `dukat-ui/src/app/styles/sapphire.css`

## 11. Nowe widoki Angulara

Wiadomość: `feat: add financial planning screens`

- `dukat-ui/src/app/components/planning.html`
- `dukat-ui/src/app/components/planning.ts`
- `dukat-ui/src/app/services/planning.ts`

## 12. Główny widok, JWT i kontrakty HTTP Angulara

Wiadomość: `feat: connect the frontend to JWT and financial APIs`

- `dukat-ui/src/app/services/api-error.ts`
- `dukat-ui/src/app/services/auth.ts`
- `dukat-ui/src/app/app.html`
- `dukat-ui/src/app/app.ts`
- `dukat-ui/src/app/services/account.ts`
- `dukat-ui/src/app/services/session.ts`
- `dukat-ui/src/app/services/transaction.ts`

## 13. Testy JUnit, PostgreSQL i Angular

Wiadomość: `test: verify ownership money concurrency and UI flows`

- `dukat-ui/src/app/components/planning.spec.ts`
- `src/test/java/com/example/demoapp/RecurringProcessorTest.java`
- `dukat-ui/src/app/app.spec.ts`
- `dukat-ui/src/app/services/account.spec.ts`
- `src/test/java/com/example/demoapp/DemoappApplicationTests.java`
- `src/test/java/com/example/demoapp/ForecastServiceTest.java`
- `src/test/java/com/example/demoapp/TransactionServiceTest.java`

## 14. Kolekcja Postmana

Wiadomość: `test: add a runnable Postman portfolio scenario`

- `postman/Dukat.postman_collection.json`
- `postman/Local.postman_environment.json`

## 15. Notatki, quiz i instrukcje

Wiadomość: `docs: explain the completed application and learning path`

- `AUDYT_GOTOWOSCI_DUKAT.md`
- `KLUCZ_QUIZU_DUKAT.md`
- `QUIZ_DUKAT.md`
- `scripts/generate-study-index.mjs`
- `INDEKS_PLIKOW_I_ADNOTACJI_DUKAT.txt`
- `MODEL_CARD_DUKAT.md`
- `NOTATKA_DO_NAUKI_DUKAT.txt`
- `PLAN_COMMITOW.md`
- `README.md`

## Końcowe sprawdzenie

```powershell
git status
git diff --cached --stat
.\mvnw.cmd test
npm --prefix dukat-ui test -- --watch=false
npm --prefix dukat-ui run build
```

W testach backendu Docker Desktop musi działać. `.env`, `target`, `node_modules`, `dist`, logi i lokalne backupy nie należą do żadnego commita. Sprawdź `git check-ignore .env`, a także listę plików w oknie Commit. Push jest osobną decyzją po sprawdzeniu serii.
