# Dukat — plan oddania i powtórki

## Cel na ten tydzień

Przygotować działającą demonstrację aplikacji finansowej i umieć wyjaśnić przepływ danych, reguły biznesowe oraz najważniejsze decyzje techniczne. Najpierw pokazać kompletny podstawowy scenariusz, następnie dopracować go do oddania.

To robocza propozycja zakresu, oparta na obecnych plikach projektu. Wymagania opiekuna mają pierwszeństwo. Nie znamy jeszcze jego listy obowiązkowych funkcji ani dokładnego terminu prezentacji.

## Co jest obecnie

Stan sprawdzony 19 października 2026 r.:

- W projekcie są Java 21, Spring Boot i Maven.
- Modele `Account` i `Transaction`, repozytoria oraz obsługa GET/POST transakcji mają implementację. Nie potwierdzono jeszcze działania całego backendu.
- Serwis tworzenia transakcji zapisuje operację i zmienia saldo konta w metodzie z `@Transactional`.
- Wiele pozostałych klas, m.in. budżety, cele, gospodarstwa domowe i konfiguracja bezpieczeństwa, to obecnie szkielety.
- Kontener `dukat-db` z PostgreSQL działa. Port komputera `5433` jest przekierowany do portu `5432` w kontenerze.
- Java, Node i npm są dostępne w terminalu.
- W tym folderze nie znaleziono projektu Angular. Może istnieć w innym miejscu.
- Konfiguracja aplikacji wskazuje PostgreSQL, ale w `pom.xml` nie ma jawnej zależności sterownika PostgreSQL. Trzeba zweryfikować zależności i uruchomienie.
- Plik migracji jest pusty, a schemat jest obecnie konfigurowany przez `spring.jpa.hibernate.ddl-auto=update`.

To rozpoznanie plików i narzędzi, a nie potwierdzenie gotowości aplikacji. Podczas przygotowania planu nie zmieniano kodu aplikacji ani danych w bazie.

## Proponowany zakres pierwszego pokazu

Lokalna aplikacja do ewidencji finansów, na przykładowych danych i w jednej walucie, PLN:

1. Wyświetlenie konta i salda.
2. Dodanie przychodu lub wydatku z opisem, kwotą, datą i kategorią.
3. Historia transakcji z filtrem daty lub typu.
4. Proste podsumowanie przychodów i wydatków za wybrany okres.
5. Czytelna reakcja na błędne dane i błąd połączenia z API.

Budżety, cele oszczędnościowe, płatności cykliczne i gospodarstwa domowe pozostają propozycjami rozszerzeń, dopóki opiekun nie potwierdzi, że są obowiązkowe. Logowanie i uprawnienia wymagają osobnej decyzji o zakresie: jeśli są wymagane na oddanie, należy uwzględnić je przed rozwijaniem podsumowań.

## Jak łączą się technologie

Docelowa droga danych:

`Formularz Angular → żądanie HTTP z JSON → kontroler Java → serwis → repozytorium JPA → PostgreSQL`

Odpowiedź wraca do Angulara i aktualizuje widok. Frontend komunikuje się z API backendu; połączenie z bazą obsługuje backend.

| Element | Do czego służy w tym projekcie | Co umieć pokazać lub wyjaśnić |
| --- | --- | --- |
| HTML | Struktura widoków | Formularz, tabela, etykiety pól |
| CSS | Wygląd i układ | Czytelny formularz i widok na małym ekranie |
| JavaScript / TypeScript | Logika po stronie przeglądarki | Obiekty, tablice, typy i obsługa odpowiedzi z API |
| Angular | Organizacja interfejsu | Komponenty, formularz, serwis HTTP, obsługa ładowania i błędu |
| Java + Spring Boot | API i reguły biznesowe | Rola kontrolera, DTO, serwisu i repozytorium |
| SQL + PostgreSQL | Przechowywanie i odczyt danych | Klucze, relacje, SELECT, JOIN, SUM i GROUP BY |
| Docker + Compose | Uruchomienie bazy według zapisanej konfiguracji | Obraz, kontener, port i wolumen danych |
| Postman | Wysyłanie żądań do API | GET, POST, JSON, status HTTP i zapisane scenariusze |
| pgAdmin | Praca z PostgreSQL przez interfejs graficzny | Odczyt tabel i wykonanie zapytania SQL |
| Terminal | Uruchamianie narzędzi i diagnoza | Katalog roboczy, komendy, logi i kody błędów |
| Git | Historia pracy | Małe zmiany zapisane w zrozumiałych commitach |

Java i JavaScript to odrębne języki. Komponenty Angulara pisze się w TypeScript; warto powtarzać podstawy JavaScript na tych samych przykładach.

## Kolejność pracy

Etapy są punktami kontrolnymi. Czas wykonania trzeba ocenić po pierwszym uruchomieniu backendu; zostawić końcówkę tygodnia na poprawki i prezentację.

### Etap 1 — backend i pierwszy dowód zapisu

- Zweryfikować zależności i uruchomić backend z istniejącą bazą.
- Ustalić sposób dostępu do API na potrzeby lokalnego demo; sprawdzić obecną konfigurację Spring Security.
- Przygotować jedno przykładowe konto bez kasowania istniejących danych.
- W Postmanie dodać wydatek i odczytać historię.
- W pgAdminie potwierdzić zapis transakcji i zmianę salda.
- Sprawdzić, że nieprawidłowa kwota i nieistniejące konto dają zrozumiałą odpowiedź.

**Warunek zakończenia:** potrafię wysłać żądanie, zobaczyć odpowiedź i potwierdzić efekt w bazie. Potrafię wskazać, które fragmenty kodu obsłużyły operację.

### Etap 2 — pierwszy pokaz przez Angular

- Utworzyć frontend albo odnaleźć istniejący projekt.
- Zrobić listę transakcji, saldo oraz formularz przychodu/wydatku.
- Połączyć widoki z backendem przez serwis HTTP.
- Dodać walidację formularza oraz stan ładowania, pustej listy i błędu.

**Warunek zakończenia:** wydatek dodany w przeglądarce zmienia historię i saldo, a dane pozostają po odświeżeniu strony. To pierwszy moment na pokazanie działającego rdzenia opiekunowi.

### Etap 3 — dopracowanie wymaganych zachowań

- Dodać filtr i podsumowanie okresu, jeśli mieszczą się w uzgodnionym zakresie.
- Uporządkować reguły: dodatnia kwota, wymagane pola, dozwolone saldo i sposób traktowania dat.
- Sprawdzić scenariusz poprawny, błędne dane i brak wskazanego konta.
- Dodać testy backendu dla obliczania salda i wycofania zapisu po błędzie. Kolekcja Postmana uzupełnia te testy.
- Przygotować powtarzalne tworzenie schematu i danych demonstracyjnych; jeśli zostaje automatyczne tworzenie schematu, jawnie opisać to jako ograniczenie wersji.

**Warunek zakończenia:** podstawowe reguły mają sprawdzone przykłady, a prezentacja nie zależy od przypadkowego stanu danych.

### Etap 4 — portfolio i przygotowanie prezentacji

- Napisać README: cel, uruchomienie, funkcje gotowe i planowane oraz ograniczenia.
- Narysować schemat Angular–backend–baza i diagram tabel z relacjami.
- Zapisać trzy krótkie decyzje architektoniczne: problem, rozważane opcje, wybór i konsekwencje.
- Wyeksportować kolekcję Postmana z przykładowymi żądaniami bez prywatnych danych.
- Przećwiczyć demonstrację od uruchomienia środowiska do zapisu transakcji i sprawdzenia jej w bazie.

**Warunek zakończenia:** druga osoba może uruchomić projekt z instrukcji, a ja umiem wyjaśnić jego główny scenariusz i granice implementacji.

## Pierwsze ćwiczenie do zrozumienia

Przykład: konto ma 1000 PLN. Dodajemy wydatek 50 PLN z opisem „Zakupy”. Oczekiwane saldo wynosi 950 PLN.

Prześledzić w istniejącym kodzie:

1. `CreateTransactionRequest`: dane, które klient wysyła do API.
2. `TransactionController.createTransaction`: odbiór żądania POST.
3. `TransactionService.createTransaction`: odszukanie konta, zmiana salda i utworzenie transakcji.
4. `AccountRepository` i `TransactionRepository`: dostęp do zapisu danych.
5. `TransactionResponse`: dane odpowiedzi dla klienta.

Następnie wyjaśnić własnymi słowami, dlaczego zmiana salda i zapis transakcji powinny zakończyć się razem albo zostać razem wycofane po błędzie.

Przy każdej funkcji zapisać trzy zdania: **co zrobiłam, dlaczego tak, jak sprawdziłam wynik**. To materiał do powtórki i rozmowy o projekcie.

## Co pokazać z perspektywy solution architecta

Proponowana architektura to Angular, jeden backend Spring Boot podzielony na obszary funkcjonalne i jedna baza PostgreSQL.

Na prezentacji wyjaśnić:

- Kto korzysta z aplikacji i jaki problem rozwiązuje.
- Gdzie są granice interfejsu, reguł biznesowych i przechowywania danych.
- Jakie wymagania zdecydowały o wyborze technologii i zakresu.
- Co dzieje się po błędnym żądaniu lub nieudanym zapisie.
- Co zostało zaimplementowane, a co jest projektem dalszego rozwoju.

Plan cech enterprise może obejmować uprawnienia, historię zmian, migracje, monitorowanie i kopie zapasowe. Każdy element oznaczyć zgodnie ze stanem: gotowy, częściowy albo planowany. Wartość dokumentacji wynika z uzasadnienia decyzji i zgodności z działającym kodem.

## Dokumentacja do bieżących ćwiczeń

- [Angular: komponenty, TypeScript i HTML](https://angular.dev/essentials/components)
- [Spring: dostęp do danych przez JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Docker: czym jest kontener](https://docs.docker.com/get-started/docker-concepts/the-basics/what-is-a-container/)
- [Docker Compose: konfiguracja usług i podstawowe komendy](https://docs.docker.com/compose/intro/compose-application-model/)
- [Postman: pierwsze żądanie i kolekcja](https://learning.postman.com/docs/getting-started/quick-start/)
- [pgAdmin: wykonywanie zapytań SQL](https://www.pgadmin.org/docs/pgadmin4/latest/query_tool.html)
