import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin, Subscription, Observable } from 'rxjs';
import { AccountService, Account } from './services/account';
import { SessionService } from './services/session';
import { TransactionService, FinanceTransaction, TransactionType } from './services/transaction';
import { AuthService } from './services/auth';
import { PlanningService, Category, PlanningView } from './services/planning';
import { PlanningComponent } from './components/planning';
import { apiError } from './services/api-error';
import { IconComponent } from './components/icon';
import { Forecast, ForecastService } from './services/forecast';
import { ForecastPanelComponent } from './components/forecast-panel';

type View = 'overview' | 'accounts' | 'transactions' | PlanningView;

// @Component łączy klasę (stan i zdarzenia), szablon HTML oraz style.
// standalone oznacza, że zależności widoku deklarujemy w imports, bez AppModule.
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    IconComponent,
    ForecastPanelComponent,
    PlanningComponent,
  ],
  templateUrl: './app.html',
  // Reguły responsywne w app.css są ostatnie, aby nadpisywały style paneli.
  styleUrls: [
    './styles/navigation.css',
    './styles/panels.css',
    './styles/charts.css',
    './styles/details.css',
    './app.css',
    './styles/responsive.css',
    './styles/sapphire.css',
  ],
})
export class AppComponent {
  private readonly auth = inject(AuthService);
  private readonly planning = inject(PlanningService);
  private readonly accountService = inject(AccountService);
  private readonly transactionService = inject(TransactionService);
  private readonly session = inject(SessionService);
  private readonly forecasts = inject(ForecastService);
  private forecastRequest?: Subscription;
  private dashboardRequest?: Subscription;
  private authRequest?: Subscription;
  readonly forecast = signal<Forecast | null>(null);
  readonly forecastLoading = signal(false);
  readonly forecastError = signal('');

  // signal przechowuje stan reaktywny. Zmiana wartości odświeża zależny widok
  // również w domyślnym dla Angulara 22 trybie bez Zone.js.
  readonly revision = signal(0);
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
  email = '';
  registerMode = false;
  editingAccountId: number | null = null;
  readonly includeArchived = signal(false);
  readonly activeAccounts = computed(() => this.accounts().filter((a) => !a.archived));
  readonly activeCategories = computed(() => this.categories().filter((c) => c.active));
  readonly planningView = computed(() =>
    ['budgets', 'goals', 'recurring-payments', 'transfers', 'categories'].includes(this.view())
      ? (this.view() as PlanningView)
      : null,
  );
  readonly navigation: { id: PlanningView; label: string; icon: string }[] = [
    { id: 'budgets', label: 'Budżety', icon: 'receipt' },
    { id: 'goals', label: 'Cele oszczędnościowe', icon: 'grid' },
    { id: 'recurring-payments', label: 'Płatności cykliczne', icon: 'calendar' },
    { id: 'transfers', label: 'Przelewy', icon: 'arrows' },
    { id: 'categories', label: 'Kategorie', icon: 'receipt' },
  ];
  readonly viewDescription = computed(
    () =>
      ({
        overview: 'Sprawdź, co wpływa, co wypływa i ile zostaje na Twoich kontach.',
        accounts: 'Zarządzaj kontami, ich nazwami i historią operacji.',
        transactions: 'Przeglądaj i wyszukuj przychody, wydatki oraz przelewy.',
        budgets: 'Ustal limity i sprawdzaj wykonanie z rzeczywistych wydatków.',
        goals: 'Śledź postęp oszczędzania na swoje plany.',
        'recurring-payments': 'Zaplanuj regularne zapisy i kontroluj ich wykonanie.',
        transfers: 'Przenoś środki między własnymi kontami z pełną historią.',
        categories: 'Dopasuj kategorie do swojego sposobu planowania.',
      })[this.view()],
  );
  readonly viewTitle = computed(
    () =>
      ({
        overview: 'Spokojnie. Masz pełen obraz.',
        accounts: 'Każde konto. Jeden widok.',
        transactions: 'Każda złotówka ma swoją historię.',
        budgets: 'Dobry plan zostawia przestrzeń.',
        goals: 'Małe kroki do ważnych rzeczy.',
        'recurring-payments': 'Regularność daje spokój.',
        transfers: 'Twoje pieniądze. We właściwym miejscu.',
        categories: 'Wszystko ma swoje miejsce.',
      })[this.view()],
  );
  readonly viewLabel = computed(
    () =>
      ({
        overview: 'Pulpit',
        accounts: 'Konta',
        transactions: 'Transakcje',
        budgets: 'Budżety',
        goals: 'Cele oszczędnościowe',
        'recurring-payments': 'Płatności cykliczne',
        transfers: 'Przelewy',
        categories: 'Kategorie',
      })[this.view()],
  );
  password = '';
  showPassword = false;

  readonly categories = signal<Category[]>([]);
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
    if (this.loading() || !this.username.trim() || !this.password) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const request = this.registerMode
      ? this.auth.register(this.username.trim(), this.email.trim(), this.password)
      : this.auth.login(this.username.trim(), this.password);
    this.authRequest = request.subscribe({
      next: (response) => {
        this.session.setToken(response.accessToken);
        this.username = response.user.username;
        this.password = '';
        this.refresh();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(apiError(error, this.registerMode ? 'operation' : 'login'));
        this.loading.set(false);
      },
    });
  }

  refresh(): void {
    this.dashboardRequest?.unsubscribe();
    this.loading.set(true);
    this.errorMessage.set('');
    // forkJoin czeka na obie odpowiedzi API. Salda i historia pochodzą z backendu.
    this.dashboardRequest = forkJoin({
      accounts: this.accountService.refreshAccounts(this.includeArchived()),
      categories: this.planning.list<Category>('categories'),
      transactions: this.transactionService.getTransactions(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ accounts, transactions, categories }) => {
          this.accounts.set(accounts);
          this.categories.set(categories);
          this.transactions.set(transactions);
          this.loaded.set(true);
          this.revision.update((value) => value + 1);
          this.password = '';
          this.loadForecast();
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
    this.authRequest?.unsubscribe();
    this.dashboardRequest?.unsubscribe();
    this.loading.set(false);
    this.forecastRequest?.unsubscribe();
    this.forecast.set(null);
    this.forecastLoading.set(false);
    this.forecastError.set('');
    this.session.clear();
    this.categories.set([]);
    this.includeArchived.set(false);
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
    if (/^\d{4}-(0[1-9]|1[0-2])$/.test(value)) {
      this.selectedMonth.set(value);
      if (this.loaded()) this.loadForecast();
    }
  }

  private loadForecast(): void {
    // Anulowanie chroni przed pokazaniem prognozy dla starego filtra miesiąca.
    this.forecastRequest?.unsubscribe();
    this.forecast.set(null);
    this.forecastError.set('');
    this.forecastLoading.set(true);
    this.forecastRequest = this.forecasts
      .getForecast(this.selectedMonth())
      .pipe(finalize(() => this.forecastLoading.set(false)))
      .subscribe({
        next: (data) => this.forecast.set(data),
        error: (error: HttpErrorResponse) =>
          this.forecastError.set(
            error.status === 400 && typeof error.error === 'string'
              ? error.error
              : 'Prognoza jest niedostępna. Uruchom ponownie backend i odśwież dane.',
          ),
      });
  }

  showAccountHistory(account: Account): void {
    this.accountFilter.set(account.name);
    this.typeFilter.set('ALL');
    this.search.set('');
    this.navigate('transactions');
  }

  openTransaction(): void {
    if (!this.activeAccounts().length || !this.activeCategories().length) return;
    this.transactionDraft = this.emptyTransaction();
    this.transactionDraft.accountId = this.activeAccounts()[0].id;
    this.transactionDraft.categoryId = this.activeCategories()[0].id;
    this.formError.set('');
    this.modal.set('transaction');
  }

  openAccount(account?: Account): void {
    this.editingAccountId = account?.id ?? null;
    this.accountDraft = account
      ? { name: account.name, accountType: account.accountType, balance: account.balance }
      : { name: '', accountType: 'Konto osobiste', balance: 0 };
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
      !draft.categoryId ||
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
        categoryId: draft.categoryId,
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
    (this.editingAccountId === null
      ? this.accountService.createAccount({
          ...this.accountDraft,
          name: this.accountDraft.name.trim(),
        })
      : this.accountService.update(this.editingAccountId, {
          name: this.accountDraft.name.trim(),
          accountType: this.accountDraft.accountType,
        })
    )
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.modal.set(null);
          this.notice.set(
            this.editingAccountId === null
              ? 'Nowe konto jest gotowe.'
              : 'Dane konta zostały zaktualizowane.',
          );
          this.refresh();
        },
        error: (error: HttpErrorResponse) => this.formError.set(this.describeError(error)),
      });
  }

  toggleArchived(): void {
    this.includeArchived.update((v) => !v);
    this.refresh();
  }
  archiveAccount(account: Account): void {
    if (this.saving()) return;
    this.saving.set(true);
    const request: Observable<unknown> = account.archived
      ? this.accountService.restore(account.id)
      : this.accountService.archive(account.id);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notice.set(
          account.archived
            ? 'Konto przywrócone.'
            : 'Konto zarchiwizowane; historia została zachowana.',
        );
        this.refresh();
      },
      error: (e: HttpErrorResponse) => this.errorMessage.set(this.describeError(e)),
    });
  }
  isIncoming(type: TransactionType): boolean {
    return type === 'INCOME' || type === 'TRANSFER_IN';
  }
  typeLabel(type: TransactionType): string {
    return {
      INCOME: 'Przychód',
      EXPENSE: 'Wydatek',
      TRANSFER_IN: 'Przelew przychodzący',
      TRANSFER_OUT: 'Przelew wychodzący',
    }[type];
  }
  private describeError(error: HttpErrorResponse): string {
    return apiError(error);
  }

  private emptyTransaction() {
    return {
      accountId: null as number | null,
      amount: null as number | null,
      type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
      date: this.today(),
      categoryId: null as number | null,
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
