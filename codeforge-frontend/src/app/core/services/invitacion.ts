import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Invitacion } from '../../auth/models/invitacion.model';
import { getApiBaseUrl } from '../config/api.config';

export interface UsuarioBusqueda {
  idUsuario: number;
  nombre: string;
  apellido: string;
  username: string;
  correo: string;
}

export interface CrearInvitacionRequest {
  idProyecto: number;
  idEmisor: number;
  idReceptor: number;
  rolPropuesto: string;
}

@Injectable({
  providedIn: 'root'
})
export class InvitacionService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${getApiBaseUrl()}/api/invitaciones`;

  private readonly usuariosUrl =
    `${getApiBaseUrl()}/api/usuarios`;


  obtenerRecibidas(
    idUsuario: number
  ): Observable<Invitacion[]> {

    return this.http.get<Invitacion[]>(
      `${this.apiUrl}/recibidas/${idUsuario}`
    );

  }


  obtenerEnviadas(
    idUsuario: number
  ): Observable<Invitacion[]> {

    return this.http.get<Invitacion[]>(
      `${this.apiUrl}/enviadas/${idUsuario}`
    );

  }


  crear(
    datos: CrearInvitacionRequest
  ): Observable<Invitacion> {

    return this.http.post<Invitacion>(
      this.apiUrl,
      datos
    );

  }


  aceptar(
    idInvitacion: number
  ): Observable<Invitacion> {

    return this.http.put<Invitacion>(
      `${this.apiUrl}/${idInvitacion}/aceptar`,
      {}
    );

  }


  rechazar(
    idInvitacion: number
  ): Observable<Invitacion> {

    return this.http.put<Invitacion>(
      `${this.apiUrl}/${idInvitacion}/rechazar`,
      {}
    );

  }


  buscarUsuario(
    username: string
  ): Observable<UsuarioBusqueda> {

    return this.http.get<UsuarioBusqueda>(
      `${this.usuariosUrl}/buscar`,
      {
        params: {
          username
        }
      }
    );

  }

}