// src/app/shared/models/operacao-response.ts
import { Produto } from './produto';
import { Usuario } from './usuario';

export interface OperacaoResponse {
  id: number;
  situacao: 'REALIZADA' | 'CANCELADA' | 'SEPARADA';
  diaOperacao: string;
  quantidade: number;
  valorTotal: number;
  produto: Produto;
  usuario: Usuario;
}