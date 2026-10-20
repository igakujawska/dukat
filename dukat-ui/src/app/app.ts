import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountService, Account } from './services/account';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  private accountService = inject(AccountService);
  accounts: Account[] = [];

  ngOnInit(): void {
    this.accountService.getAccounts().subscribe({
      next: (data) => this.accounts = data,
      error: (err) => console.error('Błąd pobierania kont:', err)
    });
  }
}
