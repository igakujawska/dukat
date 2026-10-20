import { Component, Input } from '@angular/core';

// Mały komponent prezentacyjny: wspólny wygląd ikon, bez logiki finansowej.
@Component({
  selector: 'app-icon',
  standalone: true,
  template: `<svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path [attr.d]="paths[name] || paths['grid']" />
  </svg>`,
  styles: [
    ':host{display:inline-flex;width:1.25em;height:1.25em;flex-shrink:0}svg{width:100%;height:100%}',
  ],
})
export class IconComponent {
  @Input() name = 'grid';
  readonly paths: Record<string, string> = {
    grid: 'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z',
    wallet:
      'M20 7V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h15V7H5a2 2 0 0 1 0-4 M20 11h-6v6h6 M17 14h.01',
    arrows: 'M3 7h16 M15 3l4 4-4 4 M21 17H5 M9 13l-4 4 4 4',
    plus: 'M12 5v14 M5 12h14',
    down: 'M7 7l10 10 M17 7v10H7',
    up: 'M7 17L17 7 M7 7h10v10',
    chevron: 'M9 5l7 7-7 7',
    search: 'M21 21l-5-5 M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0',
    logout: 'M9 4H4v16h5 M14 8l4 4-4 4 M8 12h10',
    close: 'M6 6l12 12 M18 6 6 18',
    check: 'M5 12l4 4L19 6',
    calendar: 'M5 4h14v17H5z M8 2v4 M16 2v4 M5 9h14',
    refresh: 'M20 7a9 9 0 0 0-15-1L3 9 M3 3v6h6 M4 17a9 9 0 0 0 15 1l2-3 M21 21v-6h-6',
    arrow: 'M4 12h15 M13 6l6 6-6 6',
    lock: 'M7 10V7a5 5 0 0 1 10 0v3 M5 10h14v11H5z M12 14v3',
    menu: 'M4 6h16 M4 12h16 M4 18h16',
    receipt: 'M6 3h12v18l-3-2-3 2-3-2-3 2z M9 8h6 M9 12h6',
    eye: 'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12 M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0',
  };
}
