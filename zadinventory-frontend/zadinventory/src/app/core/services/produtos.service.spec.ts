import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { ProdutosService } from './produtos.service';
import { Produto } from '../../shared/models/produto';

describe('ProdutosService', () => {
  let service: ProdutosService;
  let http: HttpTestingController;

  const produto: Produto = {
    id: 1,
    nome: 'Martelo',
    descricao: 'Martelo de unha',
    quantidade: 10,
    preco: 29.9,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProdutosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('listar deve fazer GET em /api/produtos', () => {
    let recebidos: Produto[] | undefined;
    service.listar().subscribe((p) => (recebidos = p));

    const req = http.expectOne('/api/produtos');
    expect(req.request.method).toBe('GET');
    req.flush([produto]);

    expect(recebidos?.length).toBe(1);
    expect(recebidos?.[0].nome).toBe('Martelo');
  });

  it('obterPorId deve fazer GET no id informado', () => {
    service.obterPorId(42).subscribe();

    const req = http.expectOne('/api/produtos/42');
    expect(req.request.method).toBe('GET');
    req.flush(produto);
  });

  it('criar deve fazer POST enviando o produto no corpo', () => {
    service.criar(produto).subscribe();

    const req = http.expectOne('/api/produtos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(produto);
    req.flush(produto);
  });

  it('atualizar deve fazer PUT no id informado', () => {
    service.atualizar(1, produto).subscribe();

    const req = http.expectOne('/api/produtos/1');
    expect(req.request.method).toBe('PUT');
    req.flush(produto);
  });

  it('excluir deve fazer DELETE no id informado', () => {
    service.excluir(5).subscribe();

    const req = http.expectOne('/api/produtos/5');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
