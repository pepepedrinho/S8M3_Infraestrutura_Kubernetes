import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['getToken']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('deve anexar o header Authorization quando existe token', () => {
    authService.getToken.and.returnValue('token-de-teste');

    http.get('/api/produtos').subscribe();

    const req = controller.expectOne('/api/produtos');
    expect(req.request.headers.get('Authorization')).toBe(
      'Bearer token-de-teste'
    );
    req.flush([]);
  });

  it('nao deve anexar o header quando nao ha token', () => {
    authService.getToken.and.returnValue(null);

    http.get('/api/produtos').subscribe();

    const req = controller.expectOne('/api/produtos');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('deve preservar o metodo e o corpo da requisicao original', () => {
    authService.getToken.and.returnValue('token-de-teste');

    http.post('/api/produtos', { nome: 'Martelo' }).subscribe();

    const req = controller.expectOne('/api/produtos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Martelo' });
    req.flush({});
  });
});
