// src/app/core/services/produtos.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produto } from '../../shared/models/produto';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProdutosService {
  private base = environment.SERVIDOR+"/api/produtos";

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  listar(): Observable<Produto[]> {
    return this.http.get<Produto[]>(this.base);
  }

  obterPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.base}/${id}`);
  }

  criar(produto: Produto): Observable<Produto> {
    return this.http.post<Produto>(this.base, produto, this.httpOptions);
  }

  atualizar(id: number, produto: Produto): Observable<Produto> {
    return this.http.put<Produto>(`${this.base}/${id}`, produto, this.httpOptions);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
