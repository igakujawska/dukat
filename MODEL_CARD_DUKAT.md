# Prognoza wydatków Dukat

Cel: oszacowanie sumy zapisanych wydatków w wybranym miesiącu z poprzednich miesięcy. To demonstracja modelowania szeregu czasowego, bez rekomendacji inwestycyjnych i decyzji kredytowych.

## Dane i granica czasowa

Źródłem jest tabela `transactions`. API `/api/forecasts?month=2026-10` korzysta wyłącznie z dat przed `2026-10-01`, maksymalnie z 12 poprzednich miesięcy. Nie używa częściowych wydatków miesiąca celu do uczenia.

Bierzemy ciąg miesięcy kończący się bezpośrednio przed miesiącem celu. Miesiąc bez żadnej operacji przerywa ciąg, zamiast otrzymać sztuczne zero. Miesiąc z przychodami bez wydatków ma zero **zapisanych** wydatków. Kompletność ręcznych wpisów pozostaje założeniem, a nie zweryfikowaną cechą danych.

- Mniej niż 3 miesiące: `INSUFFICIENT_DATA`, prognoza `null`.
- 3–5 miesięcy: średnia z ostatnich trzech miesięcy, bez oceny MAE.
- Co najmniej 6 miesięcy: porównanie trzech modeli na tych samych miesiącach.
- Miesiąc celu może być bieżący lub historyczny; dalszy przyszły miesiąc jest odrzucany, żeby nie traktować niepełnej historii jako zamkniętego miesiąca.

## Modele

| Metoda | Sposób obliczania |
| --- | --- |
| `MOVING_AVERAGE_3` | Średnia ostatnich trzech miesięcznych sum |
| `LINEAR_TREND` | Regresja OLS względem indeksu czasu, maks. 6 ostatnich miesięcy; prognoza obcięta do minimum 0 |
| `LAST_MONTH` | Powtórzenie ostatniej obserwacji, naiwny punkt odniesienia |

Regresja uczy dwa parametry z danych. Nie jest wywołaniem LLM ani integracją Python/R. Zastosowano prostą implementację w Javie bez dodatkowego serwera modeli.

## Ocena i wybór

Walidacja krocząca zaczyna się od trzech wcześniejszych miesięcy. Na każdym kroku model widzi tylko wcześniejsze obserwacje, przewiduje kolejny miesiąc i otrzymuje błąd bezwzględny. `MAE = średnia(|prognoza − obserwacja|)` w PLN. Wszystkie modele oceniamy na tych samych datach. Najniższe MAE wybiera model; przy remisie preferowana jest średnia, potem trend, potem ostatnia wartość.

**Ten wynik służy również wyborowi modelu, więc nie jest niezależnym wynikiem testowym wybranego modelu.** Mała liczba miesięcy może dawać niestabilny wybór. Na syntetycznym, idealnie liniowym szeregu regresja może uzyskać MAE 0; nie dowodzi to skuteczności na rzeczywistych finansach.

## Implementacja i weryfikacja

- `ForecastController`: parametr miesiąca i kontrakt HTTP.
- `ForecastService`: agregacja, kontrola ciągłości, modele i walidacja.
- `TransactionRepository`: zakres dat ograniczony przed miesiącem celu.
- `ForecastResponse`: historia, wynik, oceny modeli, liczba miesięcy i założenia.
- Komponent `forecast-panel`: wykres z oddzieloną prognozą, tabela danych i opis ograniczeń.
- `ForecastServiceTest`: m.in. za krótka historia, trend liniowy, luki, miesiące z samymi przychodami i przyszły cel.
- `DemoappApplicationTests`: autoryzacja endpointu, błędny parametr i odpowiedź bez wystarczających danych.

Wartości finansowe w bazie są `BigDecimal`; obliczenia statystyczne używają `double`. Odpowiedź zaokrąglamy do dwóch miejsc.

## Ograniczenia i następny eksperyment

Brak sezonowości, planowanych rachunków, cech zewnętrznych, przedziałów predykcji i monitoringu dryfu. Dane nie są jeszcze izolowane po właścicielu — taka sama granica jak w reszcie demonstracyjnego API. Prognoza jest oparta na zapisach, nie na niezależnie potwierdzonej historii bankowej.

Następny eksperyment: większy, opisany zbiór; potwierdzenie kompletności; zamrożony końcowy holdout; walidacja czasowa do wyboru modelu na wcześniejszej części; jednorazowy pomiar na holdoucie; analiza błędów i porównanie z naiwnym modelem na tych samych miesiącach. Dopiero potem rozszerzenie modeli i cech.
