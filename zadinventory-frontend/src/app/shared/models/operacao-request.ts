// src/app/shared/models/operacao-request.ts
export interface OperacaoRequest {
  id?: number;  // ← ADICIONADO id opcional
  produtoId: number;
  usuarioId: number;
  situacao: string;
  diaOperacao: string;
  quantidade: number;
}