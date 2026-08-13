// src/app/features/operacoes/operacao-list/operacao-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { OperacoesService } from '../../../core/services/operacoes.service';
import { ProdutosService } from '../../../core/services/produtos.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { Operacao } from '../../../shared/models/operacao';
import { OperacaoRequest } from '../../../shared/models/operacao-request';
import { Produto } from '../../../shared/models/produto';
import { Usuario } from '../../../shared/models/usuario';

@Component({
  selector: 'app-operacao-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operacao-list.component.html',
  styleUrls: ['./operacao-list.component.scss'],
})
export class OperacaoListComponent implements OnInit {
  // Listas
  operacoes: Operacao[] = [];
  operacoesFiltradas: Operacao[] = [];
  produtos: Produto[] = [];
  usuarios: Usuario[] = [];

  // Estados
  operacaoEdit: OperacaoRequest | null = null;
  operacaoSituacao: Operacao | null = null;
  novaSituacao: string = 'REALIZADA';
  
  // Flags de carregamento
  carregando: boolean = false;
  carregandoProdutos: boolean = false;
  carregandoUsuarios: boolean = false;
  salvando: boolean = false;
  
  // Modais
  showModal: boolean = false;
  showSituacaoModal: boolean = false;

  // Filtros
  filtroSituacao: string = '';

  constructor(
    private operacoesService: OperacoesService,
    private produtosService: ProdutosService,
    private usuariosService: UsuariosService
  ) {}

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.carregando = true;
    
    Promise.all([
      this.carregarOperacoes(),
      this.carregarProdutos(),
      this.carregarUsuarios()
    ]).finally(() => {
      this.carregando = false;
    });
  }

  carregarOperacoes(): Promise<void> {
    return new Promise((resolve) => {
      this.operacoesService.listar().subscribe({
        next: (data: Operacao[]) => {
          console.log('Operações carregadas:', data);
          this.operacoes = data;
          this.aplicarFiltros();
          resolve();
        },
        error: (err: any) => {
          console.error('Erro ao carregar operações:', err);
          Swal.fire('Erro', 'Não foi possível carregar as operações', 'error');
          resolve();
        }
      });
    });
  }

  carregarProdutos(): Promise<void> {
    this.carregandoProdutos = true;
    return new Promise((resolve) => {
      this.produtosService.listar().subscribe({
        next: (data: Produto[]) => {
          this.produtos = data;
          this.carregandoProdutos = false;
          resolve();
        },
        error: (err: any) => {
          console.error('Erro ao carregar produtos:', err);
          this.carregandoProdutos = false;
          resolve();
        }
      });
    });
  }

  carregarUsuarios(): Promise<void> {
    this.carregandoUsuarios = true;
    return new Promise((resolve) => {
      this.usuariosService.listar().subscribe({
        next: (data: Usuario[]) => {
          this.usuarios = data;
          this.carregandoUsuarios = false;
          resolve();
        },
        error: (err: any) => {
          console.error('Erro ao carregar usuários:', err);
          this.carregandoUsuarios = false;
          resolve();
        }
      });
    });
  }

  novaOperacao(): void {
    this.operacaoEdit = {
      produtoId: 0,
      usuarioId: 0,
      situacao: 'REALIZADA',
      diaOperacao: new Date().toISOString().split('T')[0],
      quantidade: 1
    };
    this.showModal = true;
  }

  editar(operacao: Operacao): void {
    console.log('Editando operação:', operacao);
    
    // CORREÇÃO: Garantir que temos IDs válidos
    const produtoId = operacao.produto?.id || 0;
    const usuarioId = operacao.usuario?.id || 0;

    this.operacaoEdit = {
      id: operacao.id,
      produtoId: produtoId,
      usuarioId: usuarioId,
      situacao: operacao.situacao,
      diaOperacao: operacao.diaOperacao,
      quantidade: operacao.quantidade
    };
    this.showModal = true;
  }

  alterarSituacao(operacao: Operacao): void {
    console.log('Alterando situação da operação:', operacao);
    this.operacaoSituacao = operacao;
    this.novaSituacao = operacao.situacao;
    this.showSituacaoModal = true;
  }

  confirmarSituacao(): void {
    if (!this.operacaoSituacao) return;

    console.log('Confirmando situação:', this.novaSituacao, 'para operação:', this.operacaoSituacao.id);

    this.salvando = true;
    this.operacoesService.atualizarSituacao(this.operacaoSituacao.id, this.novaSituacao)
      .subscribe({
        next: (response) => {
          console.log('Situação atualizada com sucesso:', response);
          Swal.fire('Sucesso', 'Situação atualizada com sucesso!', 'success');
          this.carregarOperacoes();
          this.fecharSituacaoModal();
        },
        error: (err: any) => {
          console.error('Erro ao atualizar situação:', err);
          Swal.fire('Erro', 'Não foi possível atualizar a situação', 'error');
          this.salvando = false;
        }
      });
  }

  excluir(operacao: Operacao): void {
    console.log('Excluindo operação:', operacao);

    Swal.fire({
      title: 'Tem certeza?',
      text: `Excluir operação #${operacao.id}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir!',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.operacoesService.excluir(operacao.id).subscribe({
          next: () => {
            console.log('Operação excluída com sucesso');
            Swal.fire('Excluída!', 'Operação removida com sucesso.', 'success');
            this.carregarOperacoes();
          },
          error: (err: any) => {
            console.error('Erro ao excluir operação:', err);
            Swal.fire('Erro', 'Não foi possível excluir a operação', 'error');
          }
        });
      }
    });
  }

  salvar(): void {
    if (!this.operacaoEdit) return;

    console.log('Salvando operação:', this.operacaoEdit);

    // Validações
    if (!this.operacaoEdit.produtoId || !this.operacaoEdit.usuarioId || !this.operacaoEdit.quantidade) {
      Swal.fire('Atenção', 'Preencha todos os campos obrigatórios', 'warning');
      return;
    }

    if (this.operacaoEdit.quantidade <= 0) {
      Swal.fire('Atenção', 'A quantidade deve ser maior que zero', 'warning');
      return;
    }

    this.salvando = true;
    const request = this.operacaoEdit;
    
    const operacaoId = this.operacaoEdit.id;
    const req = operacaoId
      ? this.operacoesService.atualizar(operacaoId, request)
      : this.operacoesService.criar(request);

    req.subscribe({
      next: (response) => {
        console.log('Operação salva com sucesso:', response);
        Swal.fire('Sucesso', 'Operação salva com sucesso!', 'success');
        this.carregarOperacoes();
        this.fecharModal();
      },
      error: (err: any) => {
        console.error('Erro ao salvar operação:', err);
        const mensagem = err.error?.message || 'Não foi possível salvar a operação';
        Swal.fire('Erro', mensagem, 'error');
        this.salvando = false;
      }
    });
  }

  calcularValorTotal(): number {
    if (!this.operacaoEdit?.produtoId || !this.operacaoEdit?.quantidade) return 0;
    
    const produto = this.produtos.find(p => p.id === this.operacaoEdit!.produtoId);
    if (!produto) return 0;
    
    return produto.preco * this.operacaoEdit.quantidade;
  }

  filtrarPorSituacao(event: any): void {
    this.filtroSituacao = event.target.value;
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    if (!this.filtroSituacao) {
      this.operacoesFiltradas = this.operacoes;
    } else {
      this.operacoesFiltradas = this.operacoes.filter(
        op => op.situacao === this.filtroSituacao
      );
    }
  }

  getBadgeClass(situacao: string): string {
    switch (situacao) {
      case 'REALIZADA': return 'badge bg-success';
      case 'CANCELADA': return 'badge bg-danger';
      case 'SEPARADA': return 'badge bg-warning text-dark';
      default: return 'badge bg-secondary';
    }
  }

  fecharModal(): void {
    this.showModal = false;
    this.operacaoEdit = null;
    this.salvando = false;
  }

  fecharSituacaoModal(): void {
    this.showSituacaoModal = false;
    this.operacaoSituacao = null;
    this.salvando = false;
  }
}