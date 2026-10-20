import { HttpErrorResponse } from '@angular/common/http';
export function apiError(error: HttpErrorResponse): string {
  if (error.status === 200)
    return 'Serwer zwrócił HTML zamiast danych API. Uruchom frontend z proxy albo przez Docker Compose.';
  if ([0, 502, 503, 504].includes(error.status))
    return 'Nie można połączyć się z backendem. Sprawdź, czy działa na porcie 8082.';
  if (error.error && typeof error.error === 'object')
    return (
      Object.values(error.error)
        .filter((v) => typeof v === 'string')
        .join(' ') || 'Operacja nie powiodła się.'
    );
  if (typeof error.error === 'string' && error.error && !error.error.startsWith('<'))
    return error.error;
  if (error.status === 401)
    return 'Nieprawidłowy login lub hasło albo wygasła sesja. Zaloguj się ponownie.';
  return 'Nie udało się wykonać operacji (HTTP ' + error.status + ').';
}
