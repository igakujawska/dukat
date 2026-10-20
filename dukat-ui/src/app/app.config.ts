import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePl from '@angular/common/locales/pl';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

// Jeden format dat i kwot dla całej aplikacji, np. 5 000,00 zł.
registerLocaleData(localePl);

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'pl-PL' },
    provideRouter(routes),
    // Klient HTTP jest wstrzykiwany do serwisów. Angular 22 domyślnie działa bez Zone.js.
    provideHttpClient(),
  ],
};
