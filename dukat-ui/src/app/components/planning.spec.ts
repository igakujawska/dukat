import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PlanningComponent } from './planning';
import { vi } from 'vitest';
describe('PlanningComponent', () => {
  let http: HttpTestingController;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanningComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  async function setup(view: string) {
    const f = TestBed.createComponent(PlanningComponent);
    f.componentRef.setInput('view', view);
    f.componentRef.setInput('month', '2026-10');
    f.componentRef.setInput('accounts', [
      { id: 1, name: 'Główne', balance: 100 },
      { id: 2, name: 'Cel', balance: 0 },
    ]);
    f.componentRef.setInput('categories', [{ id: 10, name: 'Test', active: true }]);
    await f.whenStable();
    http.expectOne(view === 'budgets' ? '/api/budgets?month=2026-10' : '/api/' + view).flush([]);
    await f.whenStable();
    return f;
  }
  it('keeps a rejected transfer and displays the backend explanation', async () => {
    const f = await setup('transfers');
    const c = f.componentInstance;
    c.transfer.amount = 200;
    c.transfer.description = 'Za dużo';
    c.save();
    http
      .expectOne('/api/transfers')
      .flush(
        { message: 'Brak wystarczających środków' },
        { status: 400, statusText: 'Bad Request' },
      );
    await f.whenStable();
    expect(c.transfer.amount).toBe(200);
    expect(f.nativeElement.textContent).toContain('Brak wystarczających środków');
  });
  it('saves a monthly budget and requests updated spending from the server', async () => {
    const f = await setup('budgets');
    const c = f.componentInstance;
    const changed = vi.fn();
    c.changed.subscribe(changed);
    c.budget.monthlyLimit = 100;
    c.save();
    const r = http.expectOne('/api/budgets');
    expect(r.request.body).toEqual({ categoryId: 10, monthlyLimit: 100, month: '2026-10' });
    r.flush({ id: 1 });
    http.expectOne('/api/budgets?month=2026-10').flush([
      {
        id: 1,
        categoryId: 10,
        category: 'Test',
        monthlyLimit: 100,
        spent: 25,
        remaining: 75,
        month: '2026-10',
      },
    ]);
    await f.whenStable();
    expect(changed).toHaveBeenCalledOnce();
    expect(f.nativeElement.textContent).toContain('Test');
    expect(c.budgets()[0].remaining).toBe(75);
  });
  it('does not send a transfer to the same account', async () => {
    const f = await setup('transfers');
    const c = f.componentInstance;
    c.transfer.sourceAccountId = 1;
    c.transfer.targetAccountId = 1;
    c.transfer.amount = 10;
    c.save();
    http.expectNone('/api/transfers');
    expect(c.error()).toContain('dwa różne konta');
  });
  it('requires a second click before deleting a goal', async () => {
    const f = await setup('goals');
    const c = f.componentInstance;
    c.remove(7);
    http.expectNone('/api/goals/7');
    c.remove(7);
    const r = http.expectOne('/api/goals/7');
    expect(r.request.method).toBe('DELETE');
    r.flush(null);
    http.expectOne('/api/goals').flush([]);
  });
  it('signals an expired session', async () => {
    const f = await setup('goals');
    const c = f.componentInstance;
    const expired = vi.fn();
    c.expired.subscribe(expired);
    c.load();
    http
      .expectOne('/api/goals')
      .flush({ message: 'Zaloguj się ponownie' }, { status: 401, statusText: 'Unauthorized' });
    expect(expired).toHaveBeenCalledOnce();
  });
});
