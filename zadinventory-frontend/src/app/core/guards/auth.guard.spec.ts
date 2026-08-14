import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const rota = {} as ActivatedRouteSnapshot;
  const estado = { url: '/produtos' } as RouterStateSnapshot;

  const executar = () =>
    TestBed.runInInjectionContext(() => authGuard(rota, estado));

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isLogado']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('deve liberar a navegacao para usuario autenticado', () => {
    authService.isLogado.and.returnValue(true);

    expect(executar()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve bloquear e redirecionar para /login quando nao autenticado', () => {
    authService.isLogado.and.returnValue(false);

    expect(executar()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
