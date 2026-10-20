import { Component, Input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Forecast } from '../services/forecast';

// Komponent prezentuje wynik obliczony w Javie. Nie trenuje drugiego modelu w przeglądarce.
@Component({
  selector: 'app-forecast-panel',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './forecast-panel.html',
  styleUrl: './forecast-panel.css',
})
export class ForecastPanelComponent {
  @Input() forecast: Forecast | null = null;
  @Input() loading = false;
  @Input() error = '';

  methodName(method: string): string {
    return (
      (
        {
          MOVING_AVERAGE_3: 'Średnia z 3 miesięcy',
          LINEAR_TREND: 'Regresja liniowa',
          LAST_MONTH: 'Ostatni miesiąc',
        } as Record<string, string>
      )[method] || method
    );
  }
  monthName(key: string): string {
    return new Intl.DateTimeFormat('pl-PL', { month: 'short', year: '2-digit' }).format(
      new Date(`${key}-01T12:00:00`),
    );
  }
  get max(): number {
    return (
      Math.max(
        1,
        ...(this.forecast?.history.map((p) => p.expenses) || []),
        this.forecast?.predictedExpenses || 0,
      ) * 1.15
    );
  }
  x(index: number): number {
    return 46 + index * (668 / Math.max(1, this.forecast?.history.length || 1));
  }
  y(value: number): number {
    return 164 - (value / this.max) * 132;
  }
  get points(): string {
    return (
      this.forecast?.history.map((p, i) => `${this.x(i)},${this.y(p.expenses)}`).join(' ') || ''
    );
  }
  get lastPoint(): string {
    const history = this.forecast?.history || [];
    return history.length
      ? `${this.x(history.length - 1)},${this.y(history.at(-1)!.expenses)}`
      : '';
  }
  get predictionLine(): string {
    return `${this.lastPoint} ${this.x(this.forecast?.history.length || 0)},${this.y(this.forecast?.predictedExpenses || 0)}`;
  }
}
