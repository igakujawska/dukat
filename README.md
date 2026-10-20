# Dukat

Aplikacja do ewidencji finansów osobistych: Angular 22, Java 21, Spring Boot 4.1, PostgreSQL i Docker. Frontend komunikuje się z backendem przez REST API.

## Dostępne funkcje

- Logowanie do demonstracyjnego API przez HTTP Basic.
- Pulpit z saldem kont oraz przychodami i wydatkami wybranego miesiąca.
- Wykres przepływów z sześciu miesięcy i zestawienie kategorii wydatków.
- Dodawanie kont oraz przychodów i wydatków.
- Historia z wyszukiwaniem, filtrem typu, konta i miesiąca.
- Walidacja formularzy i odpowiedzi błędów API.
- Szafirowy motyw ze szklanymi panelami oraz mobilnym układem.
- Prognoza wydatków ze średnią ruchomą, regresją liniową i naiwnym modelem; porównanie MAE w walidacji czasowej.

Wykres i podsumowania korzystają z odpowiedzi backendu. Pusta historia jest pokazywana jako brak danych. Saldo początkowe konta nie jest przychodem i nie pojawia się jako operacja w historii.

## Uruchomienie lokalne — trzy części

Z katalogu głównego projektu, w PowerShell:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Można też uruchomić `DemoappApplication` w IntelliJ. Backend działa na `http://localhost:8082`, baza na porcie `5433`, a [Swagger UI](http://localhost:8082/swagger-ui/index.html) opisuje API.

W drugim terminalu:

```powershell
cd .\dukat-ui
npm.cmd ci
npm.cmd start
```

Otwórz `http://localhost:4200`. Przy kolejnych uruchomieniach wystarczy `npm.cmd start`, jeśli zależności są już zainstalowane i lockfile się nie zmienił.

Login aplikacji: `user`. Hasło odczytaj z konsoli **bieżącego uruchomienia backendu**, przy `Using generated security password`. Dane `dukatuser` w Compose służą połączeniu z bazą. Sesja frontendu trzyma nagłówek logowania tylko w pamięci; odświeżenie strony wymaga ponownego logowania.

Proxy z `dukat-ui/proxy.conf.json` przekazuje `/api/**` z Angulara do backendu. Po zmianie proxy uruchom ponownie `npm.cmd start`. Proxy serwera developerskiego nie jest konfiguracją wdrożenia produkcyjnego.

Jeśli pojawia się błąd parsowania przy HTTP 200, sprawdź odpowiedź w Network: HTML zamiast JSON oznacza problem z proxy lub adresem. `npm start` jawnie wczytuje proxy. API przy błędzie logowania zwraca 401 z JSON bez nagłówka wywołującego natywne okno logowania przeglądarki; błąd wyświetla formularz Dukata.

## Prognoza

`GET /api/forecasts?month=2026-10` szacuje październik wyłącznie na podstawie wcześniejszych miesięcy. Potrzebne są co najmniej 3 kolejne miesiące z zapisami; od 6 porównujemy modele w walidacji kroczącej. Brak historii daje `null`, zamiast zmyślonej kwoty. Zasady agregacji, MAE i ograniczenia opisuje [model card](MODEL_CARD_DUKAT.md). Aplikacja nie dopisuje automatycznie syntetycznych transakcji do Twojego PostgreSQL.

## Mapa kodu — co za co odpowiada

| Część | Odpowiedzialność |
| --- | --- |
| `dukat-ui/src/app/app.ts` | Stan widoku, nawigacja, filtry i obsługa formularzy; `signal` i `computed` |
| `dukat-ui/src/app/app.html` | Widoki i powiązania danych; `@if`, `@for`, `ngModel` i zdarzenia |
| `dukat-ui/src/app/services/session.ts` | Nagłówek Basic Auth w pamięci sesji |
| `dukat-ui/src/app/services/account.ts` | Żądania API dotyczące kont |
| `dukat-ui/src/app/services/transaction.ts` | Kontrakt JSON i żądania transakcji |
| `controller` | Odbiór HTTP, wywołanie serwisu, status odpowiedzi |
| `dto` | Dane wejściowe/wyjściowe oraz ograniczenia walidacji |
| `service` | Reguły biznesowe, przeliczenie salda, granice transakcji bazodanowej |
| `repository` | Interfejsy dostępu do danych implementowane przez Spring Data JPA |
| `model` | Encje JPA, relacje i enumy |
| `exception` | Wspólne odpowiedzi błędów API |

Przykład: formularz → `TransactionService` Angulara → `POST /api/transactions` → kontroler Java → serwis Java → repozytoria → baza. Dopiero po zapisie frontend odczytuje aktualne saldo i historię. Kwota po stronie Javy ma typ `BigDecimal`.

## Testy

```powershell
.\mvnw.cmd verify
cd .\dukat-ui
npm.cmd test -- --watch=false
npm.cmd run build
```

Testy integracyjne backendu używają osobnej bazy H2 w pamięci. Testy frontendu używają kontrolowanego klienta HTTP, a podgląd w przeglądarce sprawdza także komunikację z działającym backendem.

## Jak zapisać zmiany w Git

Z katalogu głównego `demoapp` sprawdź bieżące zmiany:

```powershell
git status
git diff --stat
```

Wybierz pliki do **małych, tematycznych commitów** według [dokładnej listy zaznaczeń](PLAN_COMMITOW.md). Jeden fragment `app.ts` dotyczący rejestracji motywu należy wybrać osobno. `git add` wybiera zawartość następnego commita, a `git diff --cached` pozwala ją obejrzeć. `git commit` zapisuje lokalny punkt historii; `git push` jest osobnym wysłaniem commitów do zdalnego repozytorium.

Do repozytorium trafiają źródła i testy. `target`, `node_modules`, `dist` i cache Angulara są ignorowane.

## Granice aktualnej wersji

To lokalny projekt demonstracyjny. Dane kont nie są jeszcze rozdzielone według właściciela, logowanie użytkowników z tabeli `users` nie jest połączone ze Spring Security, a istniejący endpoint użytkowników wymaga oddzielnej poprawy obsługi haseł i DTO. JWT, rozliczenia gospodarstwa, integracje zewnętrzne i audyt to nadal obszary rozwoju. Przelewy backendu nie tworzą jeszcze pełnej historii operacji. Równoczesne zmiany salda wymagają dodatkowej strategii blokowania lub wersjonowania.

[Aktualna notatka do nauki](NOTATKA_DO_NAUKI_DUKAT.txt) opisuje języki, warstwy, przepływy, adnotacje i granice projektu. [Pełny indeks źródeł i wystąpień adnotacji](INDEKS_PLIKOW_I_ADNOTACJI_DUKAT.txt) ułatwia znalezienie przykładów w kodzie.
