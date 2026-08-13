import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../../shared/models/usuario';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private apiUrl = environment.SERVIDOR+"/api/usuarios";

  constructor(private http: HttpClient) {}

  // O interceptor já adiciona o token automaticamente
  // Não precisamos mais do httpOptions com headers manualmente

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  obterPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  criar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }

  atualizar(id: number, usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, usuario);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorEmail(email: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/por-email?email=${email}`);
  }

  buscarPorTipo(tipo: string): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/por-tipo?tipo=${tipo}`);
  }
}