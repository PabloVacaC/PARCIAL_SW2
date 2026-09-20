import { Routes } from '@angular/router';

import { Inicio } from './pages/inicio/inicio';
import { Login } from './auth/pages/login/login';
import { Registro } from './auth/pages/registro/registro';
import { Dashboard } from './pages/dashboard/dashboard';
import { Editor } from './pages/editor/editor';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [

  {
    path: '',
    component: Inicio
  },

  {
    path: 'login',
    component: Login
  },

  {
    path: 'registro',
    component: Registro
  },

  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard]
  },

  {
    path: 'editor/:idProyecto',
    component: Editor,
    canActivate: [authGuard]
  },

  {
    path: '**',
    redirectTo: ''
  }

];