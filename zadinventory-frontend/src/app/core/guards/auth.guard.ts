// src/app/core/guards/auth.guard.ts
import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // No SSR, sempre permite a navegação e deixa o cliente redirecionar
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.isLogado()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};