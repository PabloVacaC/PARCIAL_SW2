import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  Proyecto
} from '../../pages/dashboard/proyecto.model';

import {
  Colaborador
} from '../../pages/editor/colaborador.model';

import {
  getApiBaseUrl
} from '../config/api.config';


export interface CrearProyectoRequest {

  nombre: string;

  descripcion: string;

}


@Injectable({
  providedIn: 'root'
})
export class ProyectoService {

  private readonly http =
    inject(HttpClient);


  private readonly apiUrl =
    `${getApiBaseUrl()}/api/proyectos`;


  obtenerMisProyectos(): Observable<Proyecto[]> {

    return this.http.get<Proyecto[]>(
      `${this.apiUrl}/mis-proyectos`
    );

  }


  crearProyecto(
    datos: CrearProyectoRequest
  ): Observable<Proyecto> {

    return this.http.post<Proyecto>(
      this.apiUrl,
      datos
    );

  }


  obtenerProyecto(
    idProyecto: number
  ): Observable<Proyecto> {

    return this.http.get<Proyecto>(
      `${this.apiUrl}/${idProyecto}`
    );

  }


  obtenerColaboradores(
    idProyecto: number
  ): Observable<Colaborador[]> {

    return this.http.get<Colaborador[]>(
      `${this.apiUrl}/${idProyecto}/colaboradores`
    );

  }

}