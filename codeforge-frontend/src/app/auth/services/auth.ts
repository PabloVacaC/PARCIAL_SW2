import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { RegistroRequest } from '../models/registro-request';

export interface RegistroResponse {
  idUsuario: number;
  nombre: string;
  apellido: string;
  username: string;
  correo: string;
  estado: string;
}

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          // Limpiar cualquier sesión anterior
          sessionStorage.clear();
          localStorage.clear();

          // Guardar SOLO en sessionStorage para aislamiento total por pestaña
          sessionStorage.setItem('token', response.token);
          sessionStorage.setItem(
            'usuario',
            JSON.stringify({
              idUsuario: response.idUsuario,
              nombre: response.nombre,
              apellido: response.apellido,
              username: response.username,
              correo: response.correo,
              estado: response.estado
            })
          );
        })
      );
  }

  registrar(datos: RegistroRequest): Observable<RegistroResponse> {
    return this.http.post<RegistroResponse>(
      `${this.apiUrl}/registro`,
      datos
    );
  }

  logout(): void {
    sessionStorage.clear();
    localStorage.clear();
    this.router.navigate(['/']);
  }

  obtenerToken(): string | null {
    return sessionStorage.getItem('token');
  }

  obtenerUsuario(): any {
    const usuario = sessionStorage.getItem('usuario');
    return usuario ? JSON.parse(usuario) : null;
  }

  estaAutenticado(): boolean {
    return !!this.obtenerToken();
  }
}