# Małe commity — aktualna lista z 22.10.2026

Poprzednia seria zmian aplikacji jest już zatwierdzona. Poniżej są wyłącznie pliki, które obecnie pokazuje git status. Nie wykonano stagingu, commita ani push.

W IntelliJ zaznacz tylko pliki jednej grupy, obejrzyj ich diff i wpisz podaną wiadomość. Zrób commit, a następnie przejdź do kolejnej grupy. Nie zaznaczaj wszystkiego naraz.

## 1. Naprawa konfliktu Docker / IntelliJ

Wiadomość: `fix: prevent Docker and IntelliJ startup conflicts`

- `docker-compose.yml`
- `scripts/Prepare-IntelliJ.ps1`
- `scripts/Start-Dukat.ps1`

Cel: PostgreSQL pozostaje w Dockerze, skrypt przygotowuje wolne porty dla IntelliJ i Angulara, a kontenery aplikacji nie włączają się automatycznie. Sprawdzono przygotowanie środowiska, wykrywanie zajętego portu oraz lokalne logowanie i odczyty przez proxy.

## 2. Instrukcja uruchamiania

Wiadomość: `docs: explain IntelliJ and Angular startup`

- `README.md`
- `PLAN_COMMITOW.md`

## 3. Wcześniej przygotowany quiz

Wiadomość: `docs: add application quiz and answer key`

- `QUIZ_DUKAT.md`
- `KLUCZ_QUIZU_DUKAT.md`

Te dwa pliki istniały wcześniej, ale nadal nie były śledzone przez Git.

## 4. Generator indeksu do nauki

Wiadomość: `chore: add study index generator`

- `scripts/generate-study-index.mjs`

To również wcześniej przygotowany plik, który pozostał poza historią Git. Odświeża indeks źródeł/adnotacji poleceniem `node scripts/generate-study-index.mjs`.

## Docker w codziennej pracy

Docker Desktop musi działać, ponieważ PostgreSQL jest w kontenerze. Włącz go raz na sesję pracy; jeśli już działa, nie uruchamiasz go ponownie przed każdym Run.

Przed pracą z IntelliJ, z głównego folderu projektu:

```powershell
.\scripts\Prepare-IntelliJ.ps1
```

Następnie uruchom DemoappApplication przez Run w IntelliJ i Angulara w terminalu:

```powershell
npm --prefix dukat-ui start
```

Start-Dukat.ps1 jest do pełnego trybu Docker, a nie do równoczesnego uruchamiania backendu w IntelliJ.

## Kontrola przed commitem

```powershell
git status
git diff --cached --stat
```

Przy pracy bez staging area sprawdź listę i diff zaznaczonych plików w oknie Commit. Nie dodawaj .env, target, node_modules, dist, logów ani plików IDE. Push pozostaje osobną czynnością.