// src/app/core/guards/role.guard.ts
import { CanActivateFn } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import Swal from 'sweetalert2';
import { isPlatformBrowser } from '@angular/common';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  const expectedRole = route.data['expectedRole'];
  const user = authService.getCurrentUser();
  const isBrowser = isPlatformBrowser(platformId);

  // Permissão OK → libera acesso
  if (authService.isLogado() && user?.tipoUsuario === expectedRole) {
    return true;
  }

  // Sem permissão → mensagem SweetAlert2
  if (isBrowser) {
    Swal.fire({
      title: 'Acesso negado',
      text: 'Você não tem permissão para acessar esta área.',
      icon: 'error',
      confirmButtonText: 'OK'
    }).then(() => {
      // Redirecionamento opcional
      router.navigate(['/produtos']);
    });
  } else {
    router.navigate(['/produtos']);
  }

  return false;
};
