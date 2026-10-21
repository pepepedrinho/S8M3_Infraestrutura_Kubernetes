import { inject, Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { LoginRequest } from '../../shared/models/login-request';
import { LoginResponse } from '../../shared/models/login-response';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';

export interface User {
  email: string;
  tipoUsuario: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = environment.SERVIDOR+"/api/auth"; 
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, req).pipe(
      tap(response => {
        // Salva o token e as informações do usuário no localStorage
        if (this.isBrowser) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('userData', JSON.stringify({
            email: response.email,
            tipoUsuario: response.tipoUsuario
          }));
        }
      }),
      catchError(error => {
        // Trata erros de HTTP
        let errorMessage = 'Erro ao realizar login!';
        if (error.status === 401) {
          errorMessage = 'Email ou senha inválidos!';
        } else if (error.status === 0) {
          errorMessage = 'Servidor indisponível. Tente novamente mais tarde.';
        }
        return throwError(() => ({ error: { message: errorMessage } }));
      })
    );
  }

  logout() {
    if (this.isBrowser) {
      localStorage.removeItem('token');
      localStorage.removeItem('userData');
    }
  }

  getToken(): string | null {
    if (this.isBrowser) {
      return localStorage.getItem('token');
    }
    return null;
  }

  getCurrentUser(): User | null {
    if (this.isBrowser) {
      const userData = localStorage.getItem('userData');
      return userData ? JSON.parse(userData) : null;
    }
    return null;
  }

  isLogado(): boolean {
    return !!this.getToken();
  }

  isGerente(): boolean {
    const user = this.getCurrentUser();
    return user?.tipoUsuario === 'GERENTE';
  }

  isFuncionario(): boolean {
    const user = this.getCurrentUser();
    return user?.tipoUsuario === 'FUNCIONARIO';
  }

  // REMOVA o método fakeLogin - não será mais necessário
}