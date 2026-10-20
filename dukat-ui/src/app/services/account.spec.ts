import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AccountService } from './account';

describe('AccountService', () => {
  let service: AccountService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AccountService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends Basic Auth to the accounts API and returns its data', () => {
    const accounts = [{ id: 1, name: 'Konto testowe', accountType: 'PLN', balance: 950 }];
    service.getAccounts('user', 'password').subscribe((result) => expect(result).toEqual(accounts));

    const request = http.expectOne('/api/accounts');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.get('Authorization')).toBe('Basic dXNlcjpwYXNzd29yZA==');
    request.flush(accounts);
  });
});
