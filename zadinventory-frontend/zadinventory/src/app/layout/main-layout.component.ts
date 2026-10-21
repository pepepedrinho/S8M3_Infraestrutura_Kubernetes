import { Component, Inject, PLATFORM_ID, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header.component';
import { CommonModule, isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, CommonModule],
  template: `
    <div class="d-flex flex-column vh-100">
      <!-- Mostra header apenas quando estiver no browser e autenticado -->
      <app-header *ngIf="isBrowser && isAuthenticated()"></app-header>

      <main [class.mt-4]="isBrowser && isAuthenticated()" class="flex-fill container">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
})
export class MainLayoutComponent {
  isBrowser: boolean;
  isAuthenticated = signal(false);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    
    // Só verifica autenticação no browser
    if (this.isBrowser) {
      this.isAuthenticated.set(!!localStorage.getItem('token'));
    }
  }
}