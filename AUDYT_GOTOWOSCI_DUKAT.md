# Dukat — stan po wdrożeniu brakujących elementów

Weryfikacja lokalna: 20.10.2026. To stan kodu i uruchomionego środowiska, nie wcześniejsza lista planów.

| Obszar z checklisty | Stan i sposób realizacji |
| --- | --- |
| Trwałość PostgreSQL | Sprawdzona po zatrzymaniu i uruchomieniu całego stacku. Liczby rekordów oraz skróty wartości kont i transakcji identyczne przed/po. |
| Zachowanie dotychczasowych danych | Przed migracją pg_dump, odtworzenie na dukat_upgrade_check, porównanie starych pól. Istniejący profil ma zachowane 1 konto i 1 transakcję. |
| Relacje i FK | Transakcja wskazuje konto i kategorię. Konto zamykane przy saldzie 0, kategoria wyłączana; historia pozostaje. Brak osieroconych powiązań w sprawdzonej bazie. |
| Migracje | Flyway V1–V4, wszystkie zastosowane pomyślnie. Hibernate ddl-auto=validate. Usunięty pusty plik o błędnej nazwie V!_init.sql. |
| PostgreSQL w Dockerze | Usługa db, host 5433 → kontener 5432. Dotychczasowy nazwany wolumen zachowany. |
| Cały stack | docker-compose.yml ma db, backend, frontend, healthchecki i kolejność startu. Dockerfile Javy i Angular/Nginx. |
| Wygodny start i porty | Start-Dukat.ps1 sprawdza zajęte porty i wskazuje proces/PID. Jedno środowisko na 4200/8082; nie uruchamiać równolegle drugiego z IntelliJ. |
| Postman | Eksportowana kolekcja + czysty wzorzec środowiska. Wykonano przez proxy 4200: 25 żądań, 35 asercji, 0 błędów. Osobny profil z fikcyjnymi danymi. |
| Swagger | Publiczne UI/dokument, schemat Bearer JWT. Chronione zapytania wymagają Authorize. Sprawdzono opis i opisy pól wszystkich modeli Request w działającym OpenAPI. |
| Sekrety | DB/JWT/bootstrap w lokalnym ignorowanym .env lub zmiennych środowiskowych. Repo ma wyłącznie .env.example z placeholderami. |
| Gitignore | .env, target, node_modules, dist, cache i pliki IDE wykluczone. Brak automatycznego stagingu/commita/push. |
| Konta | Lista, tworzenie, edycja metadanych, zamknięcie przy zerze, widok zamkniętych i przywrócenie. Saldo nie jest swobodnie edytowane formularzem konta. |
| Transakcje | Historia, wyszukiwanie i filtry, dodawanie INCOME/EXPENSE z datą, kwotą, opisem, kategorią i kontem. Saldo odświeżane z serwera. |
| Walidacja | Angular min/required i objaśnienie błędnej kwoty; API Valid/Positive/Digits. GlobalExceptionHandler pokazuje 400/401/403/404/409/500 bez szczegółów technicznych. |
| Dashboard | Rzeczywiste sumy miesiąca, wykres 6 miesięcy, kategorie, historia i prognoza. Przelewy wykluczone z przychodów i wydatków. |
| Budżety | Formularz i widok limitów miesięcznych, rzeczywiste wykorzystanie, pozostała kwota, edycja i usuwanie. |
| Cele | Formularz, termin, postęp, edycja, usuwanie, dodatnie przyrosty. To ręczna ewidencja, bez obciążania salda konta. |
| Cykle | Formularz i lista, edycja, pauza, wykonanie należnych terminów, scheduler co minutę, ostatni błąd. Blokada i unikalny termin chronią przed duplikatem. Kotwica dnia miesiąca. |
| Przelewy | Formularz i historia, własne aktywne konta, wystarczające środki, atomowe dwa salda i dwa wpisy historii. |
| Logowanie/rejestracja | JWT godzinny + BCrypt. Token tylko w pamięci. Brak hasła zmieniającego się z konsolą i natywnego monitu Basic. |
| Izolacja danych | CurrentUser + ownerId w repozytoriach. Cudze id daje 404; hasła nie występują w DTO odpowiedzi. |
| Współbieżność salda | Pessimistic write + Account.version; blokowanie kont przelewu w kolejności id. Test 12 równoczesnych wydatków potwierdził saldo i poprawny zapis. |
| Prognozy | 3 modele, walidacja czasowa/MAE, tylko własne wcześniejsze miesiące, jawny brak prognozy przy za krótkiej historii. |

## Wykonane sprawdzenia

- Maven: **24 testy**, 0 porażek / 0 błędów / 0 pominiętych. W tym HTTP na prawdziwym tymczasowym PostgreSQL z Flyway, JWT, izolacja, walidacja, pieniądze, cykle, współbieżność i modele.
- Angular: **19 testów**, wszystkie przeszły. Logowanie, błędy, odświeżanie, anulowanie odczytów po logout oraz nowe formularze.
- Build Angulara produkcyjny: poprawny, bez ostrzeżeń limitów stylów. Około 431 kB początkowego JS+CSS przed kompresją.
- Obrazy Docker: oba zbudowane; db/backend/frontend uruchomione i healthy.
- Postman/Newman: **25 żądań / 35 asercji**, bez błędów, przez Nginx/frontend.
- Automatyczny scheduler w uruchomionym kontenerze: sam zaksięgował 1 zł na fikcyjnym profilu, saldo zmieniło się dokładnie o 1 zł, ręczne ponowienie zwróciło posted=0; cykl po sprawdzeniu wstrzymany.
- Restart całego stacku: porównane konta i transakcje przed/po, zachowane dane; wszystkie kontenery wróciły do healthy.
- Logowanie istniejącego profilu z lokalnego .env: HTTP 200. Zachowane dane widoczne tylko w tym profilu.
- Swagger/OpenAPI: publiczne UI, Bearer, opisy modeli i ich pól; publiczne logowanie nie ma wymagania tokenu.

Surowe logi i kopia sprzed migracji są lokalnie w ignorowanym target. Nie należy ich commitować. Stan zdrowia jest sprawdzeniem działania procesu, nie dowodem poprawności całej logiki; dlatego są też testy biznesowe.

## Granice tej wersji

Gotowe do lokalnej demonstracji portfolio. Nie oznacza to kompletnego wdrożenia publicznego: brak resetu hasła, ograniczania prób logowania, odwoływania/refresh JWT, idempotency-key ręcznych POST, produkcyjnego HTTPS/monitoringu i automatycznej polityki backupów. Cykle i cele nie wykonują przelewów bankowych. Szkic gospodarstw nie jest pełnym modułem współdzielenia. Model nie ma niezależnego holdoutu ani dowodu kompletności ręcznych danych.

Następna wartościowa część stażu to przegląd wymagań i testów akceptacyjnych, diagram wdrożenia, ADR dla decyzji o monolicie/JWT/migracjach/blokadach oraz analiza ryzyk i ograniczeń modelu. To oddzielne rozszerzenie ponad wdrożoną checklistę.
