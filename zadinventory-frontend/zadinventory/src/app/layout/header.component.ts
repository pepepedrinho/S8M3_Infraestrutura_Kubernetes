import { Component, Inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  private authService = Inject(AuthService);
  private router = Inject(Router);

  isLogado(): boolean {
    return this.authService.isLogado();
  }
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
