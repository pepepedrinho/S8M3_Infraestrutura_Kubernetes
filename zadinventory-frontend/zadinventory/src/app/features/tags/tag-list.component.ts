import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { TagsService } from '../../../app/core/services/tags.service';
import { Tag } from '../../../app/shared/models/tag';

@Component({
  selector: 'app-tag-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tag-list.component.html',
  styleUrls: ['./tag-list.component.scss'],
})
export class TagListComponent implements OnInit {
  tags: Tag[] = [];
  tagsFiltradas: Tag[] = [];
  tagSelecionada: Tag | null = null;

  // Filtro MÍNIMO para Tags - apenas nome
  filtro = {
    nome: ''
  };

  constructor(private tagsService: TagsService) {}

  ngOnInit(): void {
    this.carregarTags();
  }

  carregarTags(): void {
    this.tagsService.listar().subscribe({
      next: (data: Tag[]) => {
        this.tags = data;
        this.tagsFiltradas = [...data]; // Inicializa com todas as tags
      },
      error: (err: any) => {
        console.error('Erro ao carregar tags', err);
        Swal.fire('Erro', 'Não foi possível carregar as tags', 'error');
      }
    });
  }

  // === MÉTODOS DE FILTRO SIMPLIFICADOS PARA TAGS ===

  aplicarFiltros(): void {
    if (!this.filtro.nome) {
      this.tagsFiltradas = [...this.tags];
      return;
    }

    this.tagsFiltradas = this.tags.filter(tag => 
      tag.nome.toLowerCase().includes(this.filtro.nome.toLowerCase())
    );
  }

  limparFiltros(): void {
    this.filtro.nome = '';
    this.tagsFiltradas = [...this.tags];
  }

  temFiltrosAtivos(): boolean {
    return !!this.filtro.nome;
  }

  // === MÉTODOS CRUD (mantidos originais) ===

  novaTag(): void {
    this.tagSelecionada = { nome: '' };
  }

  editar(tag: Tag): void {
    this.tagSelecionada = { ...tag };
  }

  excluir(tag: Tag): void {
    Swal.fire({
      title: 'Tem certeza?',
      text: `Excluir tag "${tag.nome}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir!',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.tagsService.excluir(tag.id!).subscribe({
          next: () => {
            Swal.fire('Excluída!', 'Tag removida com sucesso.', 'success');
            this.carregarTags();
          },
          error: () =>
            Swal.fire('Erro', 'Não foi possível excluir a tag', 'error'),
        });
      }
    });
  }

  salvar(): void {
    if (!this.tagSelecionada) return;

    // Validação básica
    if (!this.tagSelecionada.nome || this.tagSelecionada.nome.trim() === '') {
      Swal.fire('Erro', 'O nome da tag é obrigatório.', 'error');
      return;
    }

    const req = this.tagSelecionada.id
      ? this.tagsService.atualizar(this.tagSelecionada.id, this.tagSelecionada)
      : this.tagsService.criar(this.tagSelecionada);

    req.subscribe({
      next: () => {
        Swal.fire('Sucesso', 'Tag salva com sucesso!', 'success');
        this.carregarTags();
        this.tagSelecionada = null;
      },
      error: () => Swal.fire('Erro', 'Não foi possível salvar a tag', 'error'),
    });
  }

  fecharModal(): void {
    this.tagSelecionada = null;
  }
}