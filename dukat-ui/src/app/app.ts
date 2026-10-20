import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AccountService, Account } from './services/account';
import { SessionService } from './services/session';
import { TransactionService, FinanceTransaction, TransactionType } from './services/transaction';
import { IconComponent } from './components/icon';

type View = 'overview' | 'accounts' | 'transactions';

// @Component łączy klasę (stan i zdarzenia), szablon HTML oraz style.
// standalone oznacza, że zależności widoku deklarujemy w imports, bez AppModule.
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, IconComponent],
  templateUrl: './app.html',
  // Reguły responsywne w app.css są ostatnie, aby nadpisywały style paneli.
  styleUrls: ['./styles/panels.css', './styles/details.css', './app.css'],
})
export class AppComponent {
  private readonly accountService = inject(AccountService);
  private readonly transactionService = inject(TransactionService);
  private readonly session = inject(SessionService);

  // signal przechowuje stan reaktywny. Zmiana wartości odświeża zależny widok
  // również w domyślnym dla Angulara 22 trybie bez Zone.js.
  readonly accounts = signal<Account[]>([]);
  readonly transactions = signal<FinanceTransaction[]>([]);
  readonly loading = signal(false);
  readonly loaded = signal(false);
  readonly errorMessage = signal('');
  readonly saving = signal(false);
  readonly formError = signal('');
  readonly notice = signal('');
  readonly view = signal<View>('overview');
  readonly mobileMenu = signal(false);
  readonly modal = signal<'transaction' | 'account' | null>(null);
  readonly search = signal('');
  readonly typeFilter = signal('ALL');
  readonly accountFilter = signal('');
  readonly selectedMonth = signal(this.today().slice(0, 7));
  username = 'user';
  password = '';
  showPassword = false;

  readonly categories = [
    'Jedzenie i Restauracje',
    'Mieszkanie i Rachunki',
    'Transport',
    'Rozrywka',
    'Wynagrodzenie',
    'Inne',
  ];
  transactionDraft = this.emptyTransaction();
  accountDraft = { name: '', accountType: 'Konto osobiste', balance: 0 };

  // computed wyprowadza dane z sygnałów; podsumowania nie mają osobnej kopii
  // stanu i nie korzystają ze sztucznych kwot demonstracyjnych.
  readonly totalBalance = computed(() =>
    this.accounts().reduce((sum, account) => sum + account.balance, 0),
  );
  readonly monthTransactions = computed(() =>
    this.transactions()
      .filter((transaction) => transaction.transactionDate.startsWith(this.selectedMonth()))
      .sort((a, b) => b.transactionDate.localeCompare(a.transactionDate) || b.id - a.id),
  );
  readonly income = computed(() => this.sumByType(this.monthTransactions(), 'INCOME'));
  readonly expenses = computed(() => this.sumByType(this.monthTransactions(), 'EXPENSE'));
  readonly incomeCount = computed(
    () => this.monthTransactions().filter((t) => t.type === 'INCOME').length,
  );
  readonly expenseCount = computed(
    () => this.monthTransactions().filter((t) => t.type === 'EXPENSE').length,
  );
  readonly filteredTransactions = computed(() => {
    const query = this.search().trim().toLocaleLowerCase('pl');
    return this.monthTransactions().filter(
      (transaction) =>
        (this.typeFilter() === 'ALL' || transaction.type === this.typeFilter()) &&
        (!this.accountFilter() || transaction.accountName === this.accountFilter()) &&
        (!query ||
          `${transaction.description} ${transaction.category} ${transaction.accountName}`
            .toLocaleLowerCase('pl')
            .includes(query)),
    );
  });
  readonly displayedTransactions = computed(() =>
    this.view() === 'overview' ? this.monthTransactions().slice(0, 5) : this.filteredTransactions(),
  );
  readonly monthLabel = computed(() =>
    new Intl.DateTimeFormat('pl-PL', { month: 'long', year: 'numeric' }).format(
      new Date(`${this.selectedMonth()}-01T12:00:00`),
    ),
  );
  readonly chart = computed(() => {
    const end = new Date(`${this.selectedMonth()}-01T12:00:00`);
    const months = Array.from({ length: 6 }, (_, index) => {
      const date = new Date(end.getFullYear(), end.getMonth() - 5 + index, 1);
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      const items = this.transactions().filter((t) => t.transactionDate.startsWith(key));
      return {
        key,
        label: new Intl.DateTimeFormat('pl-PL', { month: 'short' }).format(date),
        income: this.sumByType(items, 'INCOME'),
        expense: this.sumByType(items, 'EXPENSE'),
      };
    });
    const max = Math.max(...months.flatMap((month) => [month.income, month.expense]), 1);
    return months.map((month) => ({
      ...month,
      incomeHeight: (month.income / max) * 100,
      expenseHeight: (month.expense / max) * 100,
    }));
  });
  readonly hasChartData = computed(() =>
    this.chart().some((month) => month.income > 0 || month.expense > 0),
  );
  readonly topCategories = computed(() => {
    const totals = new Map<string, number>();
    this.monthTransactions()
      .filter((t) => t.type === 'EXPENSE')
      .forEach((t) => totals.set(t.category, (totals.get(t.category) || 0) + t.amount));
    return [...totals]
      .sort((a, b) => b[1] - a[1])
      .slice(0, 4)
      .map(([name, amount]) => ({
        name,
        amount,
        share: this.expenses() ? (amount / this.expenses()) * 100 : 0,
      }));
  });

  loadAccounts(): void {
    if (this.loading() || !this.username.trim() || !this.password) {
      return;
    }
    this.session.setCredentials(this.username.trim(), this.password);
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    // forkJoin czeka na obie odpowiedzi API. Salda i historia pochodzą z backendu.
    forkJoin({
      accounts: this.accountService.refreshAccounts(),
      transactions: this.transactionService.getTransactions(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ accounts, transactions }) => {
          this.accounts.set(accounts);
          this.transactions.set(transactions);
          this.loaded.set(true);
          this.password = '';
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage.set(this.describeError(error));
          if (error.status === 401 && this.loaded()) {
            this.signOut();
            this.errorMessage.set('Sesja wygasła. Zaloguj się ponownie.');
          }
        },
      });
  }

  signOut(): void {
    this.session.clear();
    this.accounts.set([]);
    this.transactions.set([]);
    this.loaded.set(false);
    this.errorMessage.set('');
    this.password = '';
    this.modal.set(null);
    this.notice.set('');
    this.view.set('overview');
  }

  navigate(view: View): void {
    this.view.set(view);
    this.mobileMenu.set(false);
  }

  changeMonth(value: string): void {
    if (/^\d{4}-\d{2}$/.test(value)) this.selectedMonth.set(value);
  }

  showAccountHistory(account: Account): void {
    this.accountFilter.set(account.name);
    this.typeFilter.set('ALL');
    this.search.set('');
    this.navigate('transactions');
  }

  openTransaction(): void {
    if (!this.accounts().length) return;
    this.transactionDraft = this.emptyTransaction();
    this.transactionDraft.accountId = this.accounts()[0].id;
    this.formError.set('');
    this.modal.set('transaction');
  }

  openAccount(): void {
    this.accountDraft = { name: '', accountType: 'Konto osobiste', balance: 0 };
    this.formError.set('');
    this.modal.set('account');
  }

  // @HostListener podpina zdarzenie klawiatury; Escape zamyka otwarty dialog.
  @HostListener('document:keydown.escape')
  closeModal(): void {
    if (!this.saving()) this.modal.set(null);
  }

  saveTransaction(): void {
    const draft = this.transactionDraft;
    if (
      this.saving() ||
      !draft.accountId ||
      !draft.amount ||
      draft.amount <= 0 ||
      !draft.description.trim()
    )
      return;
    this.saving.set(true);
    this.formError.set('');
    this.transactionService
      .createTransaction({
        ...draft,
        accountId: draft.accountId,
        amount: draft.amount,
        description: draft.description.trim(),
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.selectedMonth.set(draft.date.slice(0, 7));
          this.modal.set(null);
          this.notice.set('Transakcja została zapisana. Saldo i historia są aktualne.');
          this.refresh();
        },
        error: (error: HttpErrorResponse) => this.formError.set(this.describeError(error)),
      });
  }

  saveAccount(): void {
    if (this.saving() || !this.accountDraft.name.trim()) return;
    this.saving.set(true);
    this.formError.set('');
    this.accountService
      .createAccount({ ...this.accountDraft, name: this.accountDraft.name.trim() })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.modal.set(null);
          this.notice.set('Nowe konto jest gotowe. Możesz dodać pierwszą transakcję.');
          this.refresh();
        },
        error: (error: HttpErrorResponse) => this.formError.set(this.describeError(error)),
      });
  }

  private describeError(error: HttpErrorResponse): string {
    if (error.status === 401)
      return 'Nieprawidłowy login lub hasło. Użyj danych z aktualnego uruchomienia backendu.';
    if ([0, 502, 503, 504].includes(error.status))
      return 'Nie można połączyć się z backendem. Sprawdź, czy działa na porcie 8082.';
    if (error.status === 400 && error.error && typeof error.error === 'object')
      return Object.values(error.error).join(' ');
    if (error.status === 409) return 'Konto o takiej nazwie już istnieje. Wybierz inną nazwę.';
    return `Nie udało się wykonać operacji (HTTP ${error.status}). Spróbuj ponownie.`;
  }

  private emptyTransaction() {
    return {
      accountId: null as number | null,
      amount: null as number | null,
      type: 'EXPENSE' as TransactionType,
      date: this.today(),
      category: 'Jedzenie i Restauracje',
      description: '',
    };
  }

  private today(): string {
    const date = new Date();
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  private sumByType(items: FinanceTransaction[], type: TransactionType): number {
    return items
      .filter((transaction) => transaction.type === type)
      .reduce((sum, transaction) => sum + transaction.amount, 0);
  }
}
