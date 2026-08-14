import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import Swal from 'sweetalert2';

import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const estado = { url: '/usuarios' } as RouterStateSnapshot;

  const rotaEsperando = (papel: string) =>
    ({ data: { expectedRole: papel } } as unknown as ActivatedRouteSnapshot);

  const executar = (papel: string) =>
    TestBed.runInInjectionContext(() => roleGuard(rotaEsperando(papel), estado));

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', [
      'isLogado',
      'getCurrentUser',
    ]);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });

    // Evita abrir o modal de verdade durante os testes.
    spyOn(Swal, 'fire').and.returnValue(Promise.resolve({}) as never);
  });

  it('deve liberar quando o papel do usuario corresponde ao esperado', () => {
    authService.isLogado.and.returnValue(true);
    authService.getCurrentUser.and.returnValue({
      email: 'admin@zad.com',
      tipoUsuario: 'GERENTE',
    });

    expect(executar('GERENTE')).toBeTrue();
    expect(Swal.fire).not.toHaveBeenCalled();
  });

  it('deve bloquear quando o papel do usuario e diferente do esperado', () => {
    authService.isLogado.and.returnValue(true);
    authService.getCurrentUser.and.returnValue({
      email: 'func@zad.com',
      tipoUsuario: 'FUNCIONARIO',
    });

    expect(executar('GERENTE')).toBeFalse();
    expect(Swal.fire).toHaveBeenCalled();
  });

  it('deve bloquear quando nao ha usuario autenticado', () => {
    authService.isLogado.and.returnValue(false);
    authService.getCurrentUser.and.returnValue(null);

    expect(executar('GERENTE')).toBeFalse();
    expect(Swal.fire).toHaveBeenCalled();
  });
});
