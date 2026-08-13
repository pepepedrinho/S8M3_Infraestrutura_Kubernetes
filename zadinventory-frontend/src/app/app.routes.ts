// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard'; // ← minúsculo (função)
import { roleGuard } from './core/guards/role.guard'; // ← minúsculo (função)

export const routes: Routes = [
  { 
    path: '', 
    redirectTo: 'login', 
    pathMatch: 'full' 
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard], // ← função, não classe
    children: [
      {
        path: 'produtos',
        loadComponent: () =>
          import('./features/produtos/produto-list/produto-list.component').then(
            (m) => m.ProdutoListComponent
          ),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/usuarios/usuario-list/usuario-list.component').then(
            (m) => m.UsuarioListComponent
          ),
        canActivate: [roleGuard], // ← função, não classe
        data: { expectedRole: 'GERENTE' }
      },
      {
        path: 'operacoes',
        loadComponent: () =>
          import('./features/operacoes/operacao-list/operacao-list.component').then(
            (m) => m.OperacaoListComponent
          ),
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/categorias/categoria-list.component').then(
            (m) => m.CategoriaListComponent
          ),
      },
      {
        path: 'tags',
        loadComponent: () =>
          import('./features/tags/tag-list.component').then(
            (m) => m.TagListComponent
          ),
      },
      { 
        path: '', 
        redirectTo: 'produtos', 
        pathMatch: 'full' 
      },
    ],
  },
  { 
    path: '**', 
    redirectTo: 'login' 
  },
];