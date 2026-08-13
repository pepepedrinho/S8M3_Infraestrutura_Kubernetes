// src/app/shared/models/produto.ts
import { Categoria } from './categoria';
import { Usuario } from './usuario';
import { Tag } from './tag';

export interface Produto {
  id?: number;
  nome: string;
  descricao?: string | null;
  quantidade?: number | null;
  preco: number;
  categoria?: Categoria | null; // pode ser null/undefined até o usuário selecionar
  usuario?: Usuario | null;     // idem
  tags?: Tag[] | null;          // lista de tags (ids opcionais)
}
