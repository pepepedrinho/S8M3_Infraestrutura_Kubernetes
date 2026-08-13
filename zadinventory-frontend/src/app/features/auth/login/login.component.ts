import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import Swal from 'sweetalert2';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  email = '';
  senha = '';
  isLoading = false;
  private isBrowser: boolean;

  constructor(
    private authService: AuthService, 
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  onSubmit() {
    if (!this.email || !this.senha) {
      if (this.isBrowser) {
        Swal.fire('Erro', 'Preencha todos os campos!', 'error');
      }
      return;
    }

    this.isLoading = true;

    this.authService.login({ email: this.email, senha: this.senha }).subscribe({
      next: (response) => {
        this.isLoading = false;
        
        if (this.isBrowser) {
          Swal.fire({
            title: 'Bem-vindo!',
            text: 'Login realizado com sucesso!',
            icon: 'success',
            timer: 1500,
            showConfirmButton: false
          }).then(() => {
            this.router.navigate(['/produtos']);
          });
        } else {
          this.router.navigate(['/produtos']);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Erro no login:', error);
        
        if (this.isBrowser) {
          Swal.fire('Erro', error.error.message, 'error');
        }
      }
    });
  }
}