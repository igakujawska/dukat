# Małe commity — co zaznaczyć

Plan dotyczy zmian z 20.10.2026 po poprzednim commicie pulpitu. Nie wykonałam `git add`, commita ani push. Kolejność ma znaczenie: backend prognozy powstaje przed frontendem, który go wywołuje.

W oknie Commit zaznaczaj wyłącznie podane pliki lub fragment. Pozostałe zostaw odznaczone. W terminalu pracuj z katalogu głównego:

```powershell
Set-Location -LiteralPath 'C:\Users\igaku\Desktop\głupotki w html\demoapp'
git status
```

Przed każdym commitem sprawdź `git --no-pager diff --cached --stat`. Jeśli pracujesz przez okno Commit bez staging area, obejrzyj listę i diff zaznaczonych zmian w tym oknie.

## 1. Poprawka logowania i uruchamiania proxy

Wiadomość: `fix: prevent duplicate browser login prompts`

Zaznacz całe pliki:

- `src/main/java/com/example/demoapp/config/SecurityConfig.java`
- `dukat-ui/src/app/services/session.ts`
- `dukat-ui/src/app/services/account.spec.ts`
- `dukat-ui/package.json`

Cel: odpowiedź 401 bez natywnego monitu przeglądarki, nagłówek żądania Angulara i jawne proxy w `npm start`. Regresyjne testy HTTP backendu są w pliku testowym należącym do następnego commita.

## 2. Prognozowanie i regresyjne testy API backendu

Wiadomość: `feat: forecast monthly expenses with temporal validation`

Zaznacz całe pliki:

- `src/main/java/com/example/demoapp/controller/ForecastController.java`
- `src/main/java/com/example/demoapp/dto/forecast/ForecastResponse.java`
- `src/main/java/com/example/demoapp/service/ForecastService.java`
- `src/main/java/com/example/demoapp/repository/TransactionRepository.java`
- `src/test/java/com/example/demoapp/ForecastServiceTest.java`
- `src/test/java/com/example/demoapp/DemoappApplicationTests.java`

Cel: rzeczywisty odczyt historii, trzy metody prognozowania, MAE, braki danych oraz testy API. Plik integracyjny zawiera również sprawdzenie poprawki logowania z commita 1.

## 3. Szafirowy motyw, szkło i identyfikacja wizualna

Wiadomość: `style: add sapphire and champagne theme`

Zaznacz całe pliki:

- `dukat-ui/src/styles.css`
- `dukat-ui/src/app/app.css`
- `dukat-ui/src/app/styles/panels.css`
- `dukat-ui/src/app/styles/details.css`
- `dukat-ui/src/app/styles/sapphire.css`
- `dukat-ui/src/index.html`
- `dukat-ui/public/dukat.svg`

**Dodatkowo w `dukat-ui/src/app/app.ts` zaznacz wyłącznie zmianę `styleUrls`, która dodaje `./styles/sapphire.css`.** Nie zaznaczaj teraz całego `app.ts`: pozostałe zmiany podłączają prognozę i należą do commita 4.

W podglądzie różnic zaznacz usuwaną i dodawaną linię tablicy stylów. Jeśli narzędzie pokazuje grupy zmian, rozwiń grupę do wyboru konkretnych linii. Dla tego jednego pliku potrzebne jest zatwierdzenie fragmentu. Przy włączonym staging area można użyć `git add -p dukat-ui/src/app/app.ts`, `s` do podziału fragmentu i `y` wyłącznie dla tablicy stylów; pozostałe fragmenty odrzuć `n`. Nie używaj `a`, które zaznacza wszystkie pozostałe fragmenty.

Ten podział pozwala zachować kompilowalny commit: styl i jego rejestracja trafiają razem, bez importu niezatwierdzonych komponentów prognozy. Zmiana dekoracyjnego znaku w HTML trafi wraz z pozostałym HTML w commicie 4.

## 4. Prognoza na pulpicie i obsługa odpowiedzi API

Wiadomość: `feat: display forecasts and model comparison on dashboard`

Zaznacz całe pozostałe zmiany w:

- `dukat-ui/src/app/app.ts`
- `dukat-ui/src/app/app.html`
- `dukat-ui/src/app/app.spec.ts`
- `dukat-ui/src/app/services/forecast.ts`
- `dukat-ui/src/app/components/forecast-panel.ts`
- `dukat-ui/src/app/components/forecast-panel.html`
- `dukat-ui/src/app/components/forecast-panel.css`

Cel: pobranie prognozy, prezentacja historii/modeli/MAE, anulowanie nieaktualnego odczytu oraz czytelny komunikat dla błędu parsowania HTTP 200.

## 5. Notatka, model card i instrukcje

Wiadomość: `docs: explain application flow annotations and forecast limits`

Zaznacz całe pliki:

- `README.md`
- `NOTATKA_DO_NAUKI_DUKAT.txt`
- `INDEKS_PLIKOW_I_ADNOTACJI_DUKAT.txt`
- `MODEL_CARD_DUKAT.md`
- `PLAN_COMMITOW.md`

Po każdym commicie pozostałe pliki nadal mogą być widoczne jako zmienione — to oczekiwane. Po ostatnim wykonaj `git status` i `git log --oneline -5`. Nie zaznaczaj `target`, `node_modules`, `dist`, cache, logów ani żadnych haseł.

`commit` zapisuje lokalną historię. `push` wysyła ją na GitHub i jest oddzielnym krokiem. Publikacja repozytorium nie uruchamia aplikacji ani bazy.
