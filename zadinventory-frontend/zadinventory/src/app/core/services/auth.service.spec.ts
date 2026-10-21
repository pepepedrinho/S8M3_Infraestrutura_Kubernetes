import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { LoginResponse } from '../../shared/models/login-response';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const respostaLogin: LoginResponse = {
    token: 'token-de-teste',
    email: 'admin@zad.com',
    tipoUsuario: 'GERENTE',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve enviar POST para /api/auth/login com as credenciais', () => {
    service.login({ email: 'admin@zad.com', senha: '123456' }).subscribe();

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'admin@zad.com',
      senha: '123456',
    });

    req.flush(respostaLogin);
  });

  it('deve guardar token e dados do usuario no localStorage apos o login', () => {
    service.login({ email: 'admin@zad.com', senha: '123456' }).subscribe();
    http.expectOne('/api/auth/login').flush(respostaLogin);

    expect(localStorage.getItem('token')).toBe('token-de-teste');
    expect(JSON.parse(localStorage.getItem('userData')!)).toEqual({
      email: 'admin@zad.com',
      tipoUsuario: 'GERENTE',
    });
  });

  it('deve traduzir o erro 401 para mensagem de credenciais invalidas', (done) => {
    service.login({ email: 'x@x.com', senha: 'errada' }).subscribe({
      next: () => done.fail('nao deveria ter sucesso'),
      error: (erro) => {
        expect(erro.error.message).toBe('Email ou senha inválidos!');
        done();
      },
    });

    http
      .expectOne('/api/auth/login')
      .flush({}, { status: 401, statusText: 'Unauthorized' });
  });

  it('deve traduzir o status 0 para mensagem de servidor indisponivel', (done) => {
    service.login({ email: 'x@x.com', senha: '123' }).subscribe({
      next: () => done.fail('nao deveria ter sucesso'),
      error: (erro) => {
        expect(erro.error.message).toBe(
          'Servidor indisponível. Tente novamente mais tarde.'
        );
        done();
      },
    });

    http
      .expectOne('/api/auth/login')
      .error(new ProgressEvent('erro de rede'), { status: 0 });
  });

  it('nao deve gravar nada no localStorage quando o login falha', () => {
    service.login({ email: 'x@x.com', senha: 'errada' }).subscribe({
      next: () => undefined,
      error: () => undefined,
    });

    http
      .expectOne('/api/auth/login')
      .flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(localStorage.getItem('token')).toBeNull();
  });

  it('logout deve limpar token e dados do usuario', () => {
    localStorage.setItem('token', 'token-de-teste');
    localStorage.setItem('userData', '{"email":"a@a.com"}');

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('userData')).toBeNull();
  });

  it('isLogado deve refletir a presenca do token', () => {
    expect(service.isLogado()).toBeFalse();

    localStorage.setItem('token', 'token-de-teste');
    expect(service.isLogado()).toBeTrue();
  });

  it('isGerente deve ser verdadeiro apenas para o tipo GERENTE', () => {
    localStorage.setItem(
      'userData',
      JSON.stringify({ email: 'a@a.com', tipoUsuario: 'GERENTE' })
    );
    expect(service.isGerente()).toBeTrue();
    expect(service.isFuncionario()).toBeFalse();
  });

  it('isFuncionario deve ser verdadeiro apenas para o tipo FUNCIONARIO', () => {
    localStorage.setItem(
      'userData',
      JSON.stringify({ email: 'b@b.com', tipoUsuario: 'FUNCIONARIO' })
    );
    expect(service.isFuncionario()).toBeTrue();
    expect(service.isGerente()).toBeFalse();
  });

  it('getCurrentUser deve devolver null quando nao ha usuario guardado', () => {
    expect(service.getCurrentUser()).toBeNull();
  });
});
