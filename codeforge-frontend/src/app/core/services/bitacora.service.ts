import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BitacoraEvento } from '../models/bitacora.model';
import { ColaboracionService } from './colaboracion.service';
import { getApiBaseUrl } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class BitacoraService {
  private readonly http = inject(HttpClient);
  private readonly colaboracionService = inject(ColaboracionService);
  private readonly baseUrl = `${getApiBaseUrl()}/api/proyectos`;

  /**
   * Obtiene la lista completa de eventos de la bitácora del proyecto (orden cronológico inverso).
   */
  listar(idProyecto: number): Observable<BitacoraEvento[]> {
    return this.http.get<BitacoraEvento[]>(`${this.baseUrl}/${idProyecto}/bitacora`);
  }

  /**
   * Registra un nuevo evento en la bitácora (por ejemplo un cambio manual en el lienzo).
   */
  registrar(idProyecto: number, evento: Partial<BitacoraEvento>): Observable<BitacoraEvento> {
    return this.http.post<BitacoraEvento>(`${this.baseUrl}/${idProyecto}/bitacora`, evento);
  }

  /**
   * Se suscribe en tiempo real vía WebSocket STOMP a los eventos de la bitácora de este proyecto.
   */
  suscribir(idProyecto: number, callback: (evento: BitacoraEvento) => void): void {
    const destino = `/topic/proyecto/${idProyecto}/bitacora`;
    this.colaboracionService.suscribirTopic(destino, callback);
  }

  /**
   * Desuscribe el canal de la bitácora.
   */
  desuscribir(idProyecto: number): void {
    const destino = `/topic/proyecto/${idProyecto}/bitacora`;
    this.colaboracionService.desuscribirTopic(destino);
  }
}
