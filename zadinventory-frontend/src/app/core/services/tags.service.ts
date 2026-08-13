// src/app/core/services/tags.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tag } from '../../shared/models/tag';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TagsService {
 private apiUrl = environment.SERVIDOR+"/api/tags";

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  listar(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.apiUrl);
  }

  obterPorId(id: number): Observable<Tag> {
    return this.http.get<Tag>(`${this.apiUrl}/${id}`);
  }

  criar(tag: Tag): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, tag, this.httpOptions);
  }

  atualizar(id: number, tag: Tag): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}`, tag, this.httpOptions);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, this.httpOptions);
  }
}
