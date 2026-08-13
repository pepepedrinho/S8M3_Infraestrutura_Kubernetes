// src/app/shared/models/operacao.ts
import { Produto } from './produto';
import { Usuario } from './usuario';

export interface Operacao {
  id: number;
  situacao: 'REALIZADA' | 'CANCELADA' | 'SEPARADA';
  diaOperacao: string;
  quantidade: number;
  valorTotal: number;
  produto: Produto;
  usuario: Usuario;
}