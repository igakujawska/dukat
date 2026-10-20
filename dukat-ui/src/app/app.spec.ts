import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppComponent } from './app';
import { Account } from './services/account';
import { AccountService } from './services/account';
import { TransactionService } from './services/transaction';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';
import { FinanceTransaction } from './services/transaction';

// Testujemy zachowanie widoku i kontrakt HTTP, bez dostępu do prawdziwej bazy.
describe('AppComponent', () => {
  let http: HttpTestingController;
  const account: Account = {
    id: 1,
    name: 'Konto demonstracyjne',
    accountType: 'PLN',
    balance: 950,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function flushDashboard(
    accounts: Account[] = [account],
    transactions: FinanceTransaction[] = [],
  ) {
    http.expectOne('/api/accounts').flush(accounts);
    http
      .expectOne('/api/categories?includeInactive=true')
      .flush([{ id: 10, name: 'Jedzenie', active: true }]);
    http.expectOne('/api/transactions').flush(transactions);
    flushForecast();
  }

  function flushForecast() {
    const request = http.expectOne((req) => req.url === '/api/forecasts');
    request.flush({
      targetMonth: request.request.params.get('month'),
      status: 'INSUFFICIENT_DATA',
      method: 'MOVING_AVERAGE_3',
      predictedExpenses: null,
      historyMonths: 0,
      history: [],
      scores: [],
      evaluationMonths: 0,
      assumption: 'Test danych',
    });
  }

  function flushAuth() {
    http
      .expectOne('/api/auth/login')
      .flush({
        accessToken: 'test-jwt',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: { id: 1, username: 'user', email: 'user@example.com', role: 'USER' },
      });
  }

  async function loggedIn(transactions: FinanceTransaction[] = []) {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    fixture.componentInstance.password = 'password';
    fixture.componentInstance.loadAccounts();
    flushAuth();
    flushDashboard([account], transactions);
    await fixture.whenStable();
    return fixture;
  }

  it('shows login before requesting protected data', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('dukat');
    expect(fixture.nativeElement.querySelector('input[type="password"]')).not.toBeNull();
    http.expectNone('/api/accounts');
    http.expectNone('/api/transactions');
  });

  it('renders account data after login and clears the password field', async () => {
    const fixture = await loggedIn();
    expect(fixture.nativeElement.textContent).toContain(account.name);
    expect(fixture.componentInstance.password).toBe('');
    expect(fixture.componentInstance.totalBalance()).toBe(950);
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('distinguishes an empty result from loading', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    fixture.componentInstance.password = 'password';
    fixture.componentInstance.loadAccounts();
    flushAuth();
    flushDashboard([], []);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Brak kont do wyświetlenia');
    expect(fixture.nativeElement.querySelector('[role="status"]')).toBeNull();
  });

  it('explains authentication errors without loading private data', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    fixture.componentInstance.password = 'wrong-password';
    fixture.componentInstance.loadAccounts();
    http
      .expectOne('/api/auth/login')
      .flush(
        { message: 'Nieprawidłowy login lub hasło' },
        { status: 401, statusText: 'Unauthorized' },
      );
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Nieprawidłowy login lub hasło',
    );
    http.expectNone('/api/accounts');
    http.expectNone('/api/transactions');
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('explains when the backend cannot be reached', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    fixture.componentInstance.password = 'password';
    fixture.componentInstance.loadAccounts();
    const auth = http.expectOne('/api/auth/login');
    auth.error(new ProgressEvent('error'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Nie można połączyć się z backendem',
    );
  });

  it('explains HTTP 200 JSON parsing errors caused by a missing proxy', async () => {
    vi.spyOn(TestBed.inject(AccountService), 'refreshAccounts').mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 200,
            statusText: 'OK',
            error: new SyntaxError('Unexpected HTML'),
          }),
      ),
    );
    vi.spyOn(TestBed.inject(TransactionService), 'getTransactions').mockReturnValue(of([]));
    const fixture = TestBed.createComponent(AppComponent);
    fixture.componentInstance.password = 'password';
    fixture.componentInstance.loadAccounts();
    flushAuth();
    http.expectNone('/api/categories?includeInactive=true');
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'HTML zamiast danych API',
    );
    expect(fixture.componentInstance.loaded()).toBe(false);
  });

  it('cancels a previous forecast when the month changes', async () => {
    const fixture = await loggedIn();
    fixture.componentInstance.changeMonth('2020-01');
    const previous = http.expectOne((req) => req.url === '/api/forecasts');
    fixture.componentInstance.changeMonth('2020-02');
    expect(previous.cancelled).toBe(true);
    flushForecast();
    await fixture.whenStable();
    expect(fixture.componentInstance.forecast()?.targetMonth).toBe('2020-02');
  });

  it('calculates monthly totals and applies search and type filters', async () => {
    const month = new Date().toLocaleDateString('en-CA').slice(0, 7);
    const transactions: FinanceTransaction[] = [
      {
        id: 1,
        description: 'Wynagrodzenie',
        amount: 1000,
        transactionDate: `${month}-01`,
        category: 'Praca',
        type: 'INCOME',
        accountName: account.name,
      },
      {
        id: 2,
        description: 'Zakupy na weekend',
        amount: 250,
        transactionDate: `${month}-02`,
        category: 'Jedzenie',
        type: 'EXPENSE',
        accountName: account.name,
      },
      {
        id: 3,
        description: 'Stara operacja',
        amount: 9999,
        transactionDate: '2020-01-01',
        category: 'Praca',
        type: 'INCOME',
        accountName: account.name,
      },
    ];
    const fixture = await loggedIn(transactions);
    const app = fixture.componentInstance;
    app.changeMonth(month);
    flushForecast();
    expect(app.income()).toBe(1000);
    expect(app.expenses()).toBe(250);
    expect(app.monthTransactions()).toHaveLength(2);
    app.navigate('transactions');
    app.search.set('weekend');
    expect(app.filteredTransactions().map((t) => t.id)).toEqual([2]);
    app.typeFilter.set('INCOME');
    expect(app.filteredTransactions()).toHaveLength(0);
    app.search.set('');
    expect(app.filteredTransactions().map((t) => t.id)).toEqual([1]);
  });

  it('saves a transaction and reloads authoritative balances and history', async () => {
    const fixture = await loggedIn();
    const app = fixture.componentInstance;
    app.openTransaction();
    app.transactionDraft.description = 'Kawa po pracy';
    app.transactionDraft.amount = 20;
    app.saveTransaction();
    const request = http.expectOne('/api/transactions');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.accountId).toBe(1);
    expect(request.request.body.amount).toBe(20);
    expect(request.request.body.date).toBe(app.transactionDraft.date);
    const saved: FinanceTransaction = {
      id: 12,
      description: 'Kawa po pracy',
      amount: 20,
      transactionDate: app.transactionDraft.date,
      category: 'Jedzenie',
      type: 'EXPENSE',
      accountName: account.name,
    };
    request.flush(saved);
    flushDashboard([{ ...account, balance: 930 }], [saved]);
    await fixture.whenStable();
    expect(app.totalBalance()).toBe(930);
    expect(app.expenses()).toBe(20);
    expect(app.modal()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Kawa po pracy');
  });

  it('keeps a failed transaction form open with its validation message', async () => {
    const fixture = await loggedIn();
    const app = fixture.componentInstance;
    app.openTransaction();
    app.transactionDraft.description = 'Zakupy';
    app.transactionDraft.amount = 20;
    app.saveTransaction();
    http
      .expectOne('/api/transactions')
      .flush({ amount: 'Kwota jest niepoprawna' }, { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();
    expect(app.modal()).toBe('transaction');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Kwota jest niepoprawna',
    );
  });

  it('creates an account and refreshes the account list', async () => {
    const fixture = await loggedIn();
    const app = fixture.componentInstance;
    app.openAccount();
    app.accountDraft.name = 'Oszczędności';
    app.accountDraft.balance = 100;
    app.saveAccount();
    const request = http.expectOne('/api/accounts');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.name).toBe('Oszczędności');
    const created: Account = {
      id: 2,
      name: 'Oszczędności',
      accountType: 'Konto osobiste',
      balance: 100,
    };
    request.flush(created);
    flushDashboard([account, created]);
    await fixture.whenStable();
    expect(app.accounts()).toHaveLength(2);
    expect(app.totalBalance()).toBe(1050);
  });

  it('cancels an unfinished refresh when signing out', async () => {
    const fixture = await loggedIn();
    const app = fixture.componentInstance;
    app.refresh();
    const accounts = http.expectOne('/api/accounts');
    const history = http.expectOne('/api/transactions');
    const categories = http.expectOne('/api/categories?includeInactive=true');
    app.signOut();
    expect(accounts.cancelled).toBe(true);
    expect(history.cancelled).toBe(true);
    expect(categories.cancelled).toBe(true);
    expect(app.loaded()).toBe(false);
  });

  it('returns to login and removes displayed data on sign out', async () => {
    const fixture = await loggedIn();
    fixture.componentInstance.signOut();
    await fixture.whenStable();
    expect(fixture.componentInstance.accounts()).toHaveLength(0);
    expect(fixture.componentInstance.transactions()).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('input[type="password"]')).not.toBeNull();
  });
});
