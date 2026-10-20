import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  inject,
  signal,
  OnDestroy,
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize, Observable, Subscription } from 'rxjs';
import { Account } from '../services/account';
import {
  PlanningService,
  PlanningView,
  Category,
  Budget,
  Goal,
  Recurring,
  Transfer,
} from '../services/planning';
import { apiError } from '../services/api-error';

// Jeden komponent planowania, osobne DTO i endpointy. Salda i limity liczy serwer.
@Component({
  selector: 'app-planning',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './planning.html',
  styleUrl: './planning.css',
})
export class PlanningComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) view: PlanningView = 'budgets';
  @Input() accounts: Account[] = [];
  @Input() categories: Category[] = [];
  @Input() month = '';
  @Input() revision = 0;
  @Output() changed = new EventEmitter<void>();
  @Output() expired = new EventEmitter<void>();
  private api = inject(PlanningService);
  private request?: Subscription;
  readonly budgets = signal<Budget[]>([]);
  readonly goals = signal<Goal[]>([]);
  readonly recurring = signal<Recurring[]>([]);
  readonly transfers = signal<Transfer[]>([]);
  readonly allCategories = signal<Category[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  editId: number | null = null;
  confirmDelete: number | null = null;
  contribution: Record<number, number | null> = {};
  budget = { categoryId: null as number | null, monthlyLimit: null as number | null };
  goal = { name: '', targetAmount: null as number | null, currentAmount: 0, targetDate: '' };
  payment = {
    title: '',
    amount: null as number | null,
    frequency: 'MONTHLY',
    nextDueDate: this.today(),
    accountId: null as number | null,
    categoryId: null as number | null,
    type: 'EXPENSE',
    active: true,
  };
  transfer = {
    sourceAccountId: null as number | null,
    targetAccountId: null as number | null,
    amount: null as number | null,
    date: this.today(),
    description: '',
  };
  category = { name: '' };
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['view'] || changes['month']) {
      this.reset();
      this.notice.set('');
      this.load();
    } else if (changes['revision']) {
      this.load();
    }
  }
  ngOnDestroy(): void {
    this.request?.unsubscribe();
  }
  load(): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.request = this.api
      .list<Category | Budget | Goal | Recurring | Transfer>(this.view, this.month)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => {
          switch (this.view) {
            case 'budgets':
              this.budgets.set(rows as Budget[]);
              break;
            case 'goals':
              this.goals.set(rows as Goal[]);
              break;
            case 'recurring-payments':
              this.recurring.set(rows as Recurring[]);
              break;
            case 'transfers':
              this.transfers.set(rows as Transfer[]);
              break;
            case 'categories':
              this.allCategories.set(rows as Category[]);
          }
        },
        error: (e: HttpErrorResponse) => this.failure(e),
      });
  }
  save(): void {
    if (this.saving()) return;
    let body: unknown;
    switch (this.view) {
      case 'budgets':
        body = { ...this.budget, month: this.month };
        break;
      case 'goals':
        body = {
          ...this.goal,
          name: this.goal.name.trim(),
          targetDate: this.goal.targetDate || null,
        };
        break;
      case 'recurring-payments':
        body = { ...this.payment, title: this.payment.title.trim() };
        break;
      case 'transfers':
        if (this.transfer.sourceAccountId === this.transfer.targetAccountId) {
          this.error.set('Wybierz dwa różne konta.');
          return;
        }
        body = { ...this.transfer, description: this.transfer.description.trim() };
        break;
      case 'categories':
        body = { name: this.category.name.trim() };
        break;
    }
    this.mutate(this.api.save(this.view, body, this.editId), 'Zapisano zmiany.');
  }
  reset(): void {
    this.editId = null;
    this.confirmDelete = null;
    this.error.set('');
    this.budget = { categoryId: this.categories[0]?.id ?? null, monthlyLimit: null };
    this.goal = { name: '', targetAmount: null, currentAmount: 0, targetDate: '' };
    this.payment = {
      title: '',
      amount: null,
      frequency: 'MONTHLY',
      nextDueDate: this.today(),
      accountId: this.accounts[0]?.id ?? null,
      categoryId: this.categories[0]?.id ?? null,
      type: 'EXPENSE',
      active: true,
    };
    this.transfer = {
      sourceAccountId: this.accounts[0]?.id ?? null,
      targetAccountId: this.accounts[1]?.id ?? null,
      amount: null,
      date: this.today(),
      description: '',
    };
    this.category = { name: '' };
  }
  editBudget(b: Budget): void {
    this.editId = b.id;
    this.budget = { categoryId: b.categoryId, monthlyLimit: b.monthlyLimit };
    this.error.set('');
  }
  editGoal(g: Goal): void {
    this.editId = g.id;
    this.goal = {
      name: g.name,
      targetAmount: g.targetAmount,
      currentAmount: g.currentAmount,
      targetDate: g.targetDate || '',
    };
    this.error.set('');
  }
  editPayment(p: Recurring): void {
    this.editId = p.id;
    this.payment = {
      title: p.title,
      amount: p.amount,
      frequency: p.frequency,
      nextDueDate: p.nextDueDate,
      accountId: p.accountId,
      categoryId: p.categoryId,
      type: p.type,
      active: p.active,
    };
    this.error.set('');
  }
  editCategory(c: Category): void {
    this.editId = c.id;
    this.category = { name: c.name };
    this.error.set('');
  }
  remove(id: number): void {
    if (this.confirmDelete !== id) {
      this.confirmDelete = id;
      return;
    }
    this.mutate(
      this.api.remove(this.view, id),
      this.view === 'categories'
        ? 'Kategoria wyłączona. Historia pozostaje dostępna.'
        : this.view === 'recurring-payments'
          ? 'Automatyczne księgowanie wstrzymane.'
          : 'Usunięto wpis.',
    );
  }
  contribute(g: Goal): void {
    const amount = this.contribution[g.id];
    if (!amount || amount <= 0) return;
    this.mutate(
      this.api.contribute(g.id, amount),
      'Postęp celu zaktualizowany. Salda kont pozostały bez zmian.',
    );
    this.contribution[g.id] = null;
  }
  execute(p: Recurring): void {
    this.mutate(this.api.execute(p.id), 'Sprawdzono i zaksięgowano należne terminy.');
  }
  percent(current: number, target: number): number {
    return Math.max(0, Math.min(100, target ? (100 * current) / target : 0));
  }
  frequency(value: string): string {
    return (
      ({ WEEKLY: 'Co tydzień', MONTHLY: 'Co miesiąc', YEARLY: 'Co rok' } as Record<string, string>)[
        value
      ] || value
    );
  }
  private mutate<T>(request: Observable<T>, message: string): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    this.notice.set('');
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.reset();
        this.notice.set(message);
        this.load();
        this.changed.emit();
      },
      error: (e: HttpErrorResponse) => this.failure(e),
    });
  }
  private failure(e: HttpErrorResponse): void {
    this.error.set(apiError(e));
    if (e.status === 401) this.expired.emit();
  }
  private today(): string {
    const d = new Date();
    return (
      d.getFullYear() +
      '-' +
      String(d.getMonth() + 1).padStart(2, '0') +
      '-' +
      String(d.getDate()).padStart(2, '0')
    );
  }
}
