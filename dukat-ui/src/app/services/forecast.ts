import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { SessionService } from './session';

export interface Forecast {
  targetMonth: string;
  status: 'READY' | 'INSUFFICIENT_DATA';
  method: string;
  predictedExpenses: number | null;
  historyMonths: number;
  history: { month: string; expenses: number }[];
  scores: { method: string; mae: number }[];
  evaluationMonths: number;
  assumption: string;
}

@Injectable({ providedIn: 'root' })
export class ForecastService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  getForecast(month: string) {
    return this.http.get<Forecast>('/api/forecasts', {
      headers: this.session.headers(),
      params: new HttpParams().set('month', month),
    });
  }
}
