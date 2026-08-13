import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProdutosService } from '../../../core/services/produtos.service';
import { Produto } from '../../../shared/models/produto';

@Component({
  selector: 'app-produto-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './produto-form.component.html'
})
export class ProdutoFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private service = inject(ProdutosService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form!: FormGroup;
  produtoId?: number;

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', Validators.required],
      descricao: [''],
      preco: [0, [Validators.required, Validators.min(0)]],
      quantidade: [0, [Validators.required, Validators.min(0)]]
    });

    // Verifica se estamos em "editar/:id"
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.produtoId = +id;
        this.service.obterPorId(this.produtoId).subscribe({
          next: (produto) => this.form.patchValue(produto),
          error: (err) => console.error('Erro ao carregar produto', err)
        });
      }
    });
  }

  salvar() {
    const produto: Produto = this.form.value;

    if (this.produtoId) {
      this.service.atualizar(this.produtoId, produto).subscribe({
        next: () => this.router.navigate(['/produtos']),
        error: (err) => console.error('Erro ao atualizar produto', err)
      });
    } else {
      this.service.criar(produto).subscribe({
        next: () => this.router.navigate(['/produtos']),
        error: (err) => console.error('Erro ao salvar produto', err)
      });
    }
  }

  cancelar() {
    this.router.navigate(['/produtos']);
  }
}
