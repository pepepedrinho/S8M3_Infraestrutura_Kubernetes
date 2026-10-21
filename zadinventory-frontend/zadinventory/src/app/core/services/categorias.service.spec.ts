import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { CategoriasService } from './categorias.service';
import { Categoria } from '../../shared/models/categoria';

describe('CategoriasService', () => {
  let service: CategoriasService;
  let http: HttpTestingController;

  const categoria: Categoria = {
    id: 1,
    nome: 'Ferramentas',
    descricao: 'Itens de oficina',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CategoriasService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('listar deve fazer GET em /api/categorias', () => {
    let recebidas: Categoria[] | undefined;
    service.listar().subscribe((c) => (recebidas = c));

    const req = http.expectOne('/api/categorias');
    expect(req.request.method).toBe('GET');
    req.flush([categoria]);

    expect(recebidas).toEqual([categoria]);
  });

  it('obterPorId deve fazer GET no id informado', () => {
    service.obterPorId(7).subscribe();

    const req = http.expectOne('/api/categorias/7');
    expect(req.request.method).toBe('GET');
    req.flush(categoria);
  });

  it('criar deve fazer POST enviando a categoria no corpo', () => {
    service.criar(categoria).subscribe();

    const req = http.expectOne('/api/categorias');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(categoria);
    req.flush(categoria);
  });

  it('atualizar deve fazer PUT no id informado', () => {
    service.atualizar(1, categoria).subscribe();

    const req = http.expectOne('/api/categorias/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(categoria);
    req.flush(categoria);
  });

  it('excluir deve fazer DELETE no id informado', () => {
    service.excluir(3).subscribe();

    const req = http.expectOne('/api/categorias/3');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('deve propagar o erro quando a API responde 500', (done) => {
    service.listar().subscribe({
      next: () => done.fail('nao deveria ter sucesso'),
      error: (erro) => {
        expect(erro.status).toBe(500);
        done();
      },
    });

    http
      .expectOne('/api/categorias')
      .flush('falhou', { status: 500, statusText: 'Server Error' });
  });
});
