// src/app/features/produtos/produto-list/produto-list.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

import { ProdutosService } from '../../../core/services/produtos.service';
import { CategoriasService } from '../../../core/services/categorias.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { TagsService } from '../../../core/services/tags.service';

import { Produto } from '../../../shared/models/produto';
import { Categoria } from '../../../shared/models/categoria';
import { Usuario } from '../../../shared/models/usuario';
import { Tag } from '../../../shared/models/tag';

@Component({
  selector: 'app-produto-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produto-list.component.html',
  styleUrls: ['./produto-list.component.scss']
})
export class ProdutoListComponent implements OnInit {
  produtos: Produto[] = [];
  produtosFiltrados: Produto[] = [];
  produtoSelecionado: Produto | null = null;

  categorias: Categoria[] = [];
  usuarios: Usuario[] = [];
  tagsDisponiveis: Tag[] = [];

  // Objeto para armazenar os filtros
 filtro = {
  nome: '',
  categoriaId: null as number | null,
  precoMaximo: null as number | null,
  tagsIds: [] as number[]  // Sempre será um array, nunca undefined
};

  constructor(
    private produtoService: ProdutosService,
    private categoriaService: CategoriasService,
    private usuarioService: UsuariosService,
    private tagService: TagsService
  ) {}

  ngOnInit(): void {
    this.carregarProdutos();
    this.carregarCategorias();
    this.carregarUsuarios();
    this.carregarTags();
  }

  carregarProdutos(): void {
    this.produtoService.listar().subscribe({
      next: (data: Produto[]) => {
        this.produtos = data;
        this.produtosFiltrados = [...data]; // Inicializa com todos os produtos
      },
      error: (err) => {
        console.error('Erro ao carregar produtos', err);
        Swal.fire('Erro', 'Não foi possível carregar os produtos', 'error');
      }
    });
  }

  carregarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (data: Categoria[]) => (this.categorias = data),
      error: (err) => console.error('Erro ao carregar categorias', err)
    });
  }

  carregarUsuarios(): void {
    this.usuarioService.listar().subscribe({
      next: (data: Usuario[]) => (this.usuarios = data),
      error: (err) => console.error('Erro ao carregar usuarios', err)
    });
  }

  carregarTags(): void {
    this.tagService.listar().subscribe({
      next: (data: Tag[]) => (this.tagsDisponiveis = data),
      error: (err) => console.error('Erro ao carregar tags', err)
    });
  }

  // === MÉTODOS DE FILTRO ===

  aplicarFiltros(): void {
  this.produtosFiltrados = this.produtos.filter(produto => {
    // Filtro por nome (case insensitive)
    if (this.filtro.nome && 
        !produto.nome.toLowerCase().includes(this.filtro.nome.toLowerCase())) {
      return false;
    }

    // Filtro por categoria - CORRIGIDO
    if (this.filtro.categoriaId !== null && this.filtro.categoriaId !== undefined) {
      const categoriaIdFiltro = Number(this.filtro.categoriaId);
      const categoriaIdProduto = produto.categoria?.id;
      
      // console.log para debug (remova depois)
      console.log('Filtrando categoria:', {
        filtro: categoriaIdFiltro,
        produto: categoriaIdProduto,
        iguais: categoriaIdProduto === categoriaIdFiltro
      });
      
      if (categoriaIdProduto !== categoriaIdFiltro) {
        return false;
      }
    }

    // Filtro por preço máximo - TAMBÉM PODE TER O MESMO PROBLEMA
    if (this.filtro.precoMaximo !== null && 
        this.filtro.precoMaximo !== undefined) {
      const precoMaximo = Number(this.filtro.precoMaximo);
      if ((produto.preco ?? 0) > precoMaximo) {
        return false;
      }
    }

    // Filtro por tags
    if (this.filtro.tagsIds.length > 0) {
      const produtoTagsIds = produto.tags?.map(t => t.id).filter(id => id !== undefined) || [];
      const temTodasTags = this.filtro.tagsIds.every(tagId => 
        produtoTagsIds.includes(tagId)
      );
      if (!temTodasTags) return false;
    }

    return true;
  });
}

  toggleTagFiltro(tag: Tag): void {
    if (!tag.id) return;

    const index = this.filtro.tagsIds.indexOf(tag.id);
    if (index > -1) {
      this.filtro.tagsIds.splice(index, 1);
    } else {
      this.filtro.tagsIds.push(tag.id);
    }
    
    this.aplicarFiltros();
  }

  limparFiltros(): void {
  this.filtro = {
    nome: '',
    categoriaId: null,
    precoMaximo: null,
    tagsIds: []  // Garante que sempre retorne um array vazio
  };
  this.produtosFiltrados = [...this.produtos];
}

  temFiltrosAtivos(): boolean {
    return !!this.filtro.nome || 
           this.filtro.categoriaId !== null || 
           this.filtro.precoMaximo !== null || 
           this.filtro.tagsIds.length > 0;
  }

  // === MÉTODOS CRUD ===

  novoProduto(): void {
    this.produtoSelecionado = {
      nome: '',
      descricao: '',
      quantidade: 0,
      preco: 0,
      categoria: null,
      usuario: null,
      tags: []
    } as Produto;
  }

  editar(produto: Produto): void {
    this.produtoSelecionado = {
      ...produto,
      categoria: produto.categoria ? { ...produto.categoria } : null,
      usuario: produto.usuario ? { ...produto.usuario } : null,
      tags: produto.tags ? produto.tags.map(t => ({ ...t })) : []
    } as Produto;
  }

  excluir(produto: Produto): void {
    if (!produto.id) {
      Swal.fire('Erro', 'Produto sem ID válido.', 'error');
      return;
    }

    Swal.fire({
      title: 'Tem certeza?',
      text: `Excluir "${produto.nome}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir!',
      cancelButtonText: 'Cancelar',
    }).then(result => {
      if (result.isConfirmed) {
        this.produtoService.excluir(produto.id!).subscribe({
          next: () => {
            Swal.fire('Excluído!', 'Produto removido com sucesso.', 'success');
            this.carregarProdutos(); // Recarrega a lista
          },
          error: () => Swal.fire('Erro', 'Não foi possível excluir', 'error')
        });
      }
    });
  }

  salvar(): void {
    if (!this.produtoSelecionado) return;

    if (!this.produtoSelecionado.nome || !this.produtoSelecionado.categoria || !this.produtoSelecionado.usuario) {
      Swal.fire('Erro', 'Nome, categoria e usuário são obrigatórios.', 'error');
      return;
    }

    // garantir número com 2 casas
    this.produtoSelecionado.preco = Number(Number(this.produtoSelecionado.preco).toFixed(2));

    const req = this.produtoSelecionado.id
      ? this.produtoService.atualizar(this.produtoSelecionado.id, this.produtoSelecionado)
      : this.produtoService.criar(this.produtoSelecionado);

    req.subscribe({
      next: () => {
        Swal.fire('Sucesso', 'Produto salvo com sucesso!', 'success');
        this.carregarProdutos();
        this.fecharModal();
      },
      error: err => {
        console.error(err);
        Swal.fire('Erro', 'Não foi possível salvar', 'error');
      }
    });
  }

  fecharModal(): void {
    this.produtoSelecionado = null;
  }

  // Tags helpers
  isTagSelected(tag: Tag): boolean {
    if (!this.produtoSelecionado || !this.produtoSelecionado.tags) return false;
    if (tag.id == null) return false;
    return this.produtoSelecionado.tags.some(t => t.id === tag.id);
  }

  toggleTag(tag: Tag, event: Event): void {
    if (!this.produtoSelecionado) return;
    if (!this.produtoSelecionado.tags) this.produtoSelecionado.tags = [];

    const input = event.target as HTMLInputElement | null;
    const checked = input?.checked ?? false;

    if (tag.id == null) {
      console.warn('toggleTag chamado com tag sem id — ignorando', tag);
      return;
    }

    if (checked) {
      if (!this.produtoSelecionado.tags.some(t => t.id === tag.id)) {
        this.produtoSelecionado.tags.push({ id: tag.id, nome: tag.nome });
      }
    } else {
      this.produtoSelecionado.tags = this.produtoSelecionado.tags.filter(t => t.id !== tag.id);
    }
  }
}