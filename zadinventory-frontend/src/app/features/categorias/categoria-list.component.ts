import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { CategoriasService } from '../../../app/core/services/categorias.service';
import { Categoria } from '../../../app/shared/models/categoria';

@Component({
  selector: 'app-categoria-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categoria-list.component.html',
  styleUrls: ['./categoria-list.component.scss'],
})
export class CategoriaListComponent implements OnInit {
  categorias: Categoria[] = [];
  categoriasFiltradas: Categoria[] = [];
  categoriaSelecionada: Categoria | null = null;
  isLoading = false;

  // Filtros SIMPLIFICADOS para Categorias (apenas campos relevantes)
  filtro = {
    nome: '',
    descricao: ''
  };

  constructor(private categoriasService: CategoriasService) {}

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.categoriasService.listar().subscribe({
      next: (data: Categoria[]) => {
        this.categorias = data;
        this.categoriasFiltradas = [...data]; // Inicializa com todas as categorias
      },
      error: (err: any) => {
        console.error('Erro ao carregar categorias', err);
        Swal.fire('Erro', 'Não foi possível carregar as categorias', 'error');
      }
    });
  }

  // === MÉTODOS DE FILTRO PERSONALIZADOS PARA CATEGORIAS ===

  aplicarFiltros(): void {
    this.categoriasFiltradas = this.categorias.filter(categoria => {
      // Filtro por nome (case insensitive)
      if (this.filtro.nome && 
          !categoria.nome.toLowerCase().includes(this.filtro.nome.toLowerCase())) {
        return false;
      }

      // Filtro por descrição (case insensitive) - apenas se descrição existir
      if (this.filtro.descricao) {
        const descricao = categoria.descricao || '';
        if (!descricao.toLowerCase().includes(this.filtro.descricao.toLowerCase())) {
          return false;
        }
      }

      return true;
    });
  }

  limparFiltros(): void {
    this.filtro = {
      nome: '',
      descricao: ''
    };
    this.categoriasFiltradas = [...this.categorias];
  }

  temFiltrosAtivos(): boolean {
    return !!this.filtro.nome || !!this.filtro.descricao;
  }

  // === MÉTODOS CRUD (mantidos originais) ===

  novaCategoria(): void {
    this.categoriaSelecionada = { nome: '', descricao: '' };
  }

  editar(categoria: Categoria): void {
    this.categoriaSelecionada = { ...categoria };
  }

  excluir(categoria: Categoria): void {
    Swal.fire({
      title: 'Tem certeza?',
      text: `Excluir categoria "${categoria.nome}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir!',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.categoriasService.excluir(categoria.id!).subscribe({
          next: () => {
            Swal.fire('Excluída!', 'Categoria removida com sucesso.', 'success');
            this.carregarCategorias();
          },
          error: (err) => {
            const mensagemErro = err.error?.message || 'Não foi possível excluir a categoria';
            Swal.fire('Erro', mensagemErro, 'error');
        }
          });
      }
    });
  }

  salvar(): void {
    if (!this.categoriaSelecionada) return;

    this.isLoading = true;

    const req = this.categoriaSelecionada.id
      ? this.categoriasService.atualizar(
          this.categoriaSelecionada.id,
          this.categoriaSelecionada
        )
      : this.categoriasService.criar(this.categoriaSelecionada);

    req.subscribe({
      next: () => {
        Swal.fire('Sucesso', 'Categoria salva com sucesso!', 'success');
        this.carregarCategorias();
        this.fecharModal();
      },
      error: (err) => {
        const mensagemErro = err.error?.message || 'Não foi possível salvar a categoria';
        Swal.fire('Erro', mensagemErro, 'error');
        },
        complete: () => {
          this.isLoading = false;
        }
    });
  }

  fecharModal(): void {
    this.categoriaSelecionada = null;
    this.isLoading = false;
  }
}