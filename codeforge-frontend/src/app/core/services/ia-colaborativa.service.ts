import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  IaChatMensaje,
  IaConversacionResponse,
  IaDecisionRequest,
  IaPreguntaRequest,
  IaPropuesta,
  IaUmlFinding,
  ModoIa
} from '../models/ia-colaborativa.model';
import { ColaboracionService } from './colaboracion.service';

@Injectable({
  providedIn: 'root'
})
export class IaColaborativaService {
  private readonly http = inject(HttpClient);
  private readonly colaboracionService = inject(ColaboracionService);
  private readonly baseUrl = 'http://localhost:8080/api/ia/colaborativa';

  /**
   * Obtiene la conversación compartida del proyecto y su historial completo.
   */
  obtenerConversacion(idProyecto: number): Observable<IaConversacionResponse> {
    return this.http.get<IaConversacionResponse>(
      `${this.baseUrl}/proyecto/${idProyecto}/conversacion`
    );
  }

  /**
   * Envía un mensaje a la conversación y genera respuesta de la IA.
   */
  enviarMensaje(
    idProyecto: number,
    solicitud: IaPreguntaRequest
  ): Observable<IaChatMensaje[]> {
    return this.http.post<IaChatMensaje[]>(
      `${this.baseUrl}/proyecto/${idProyecto}/mensaje`,
      solicitud
    );
  }

  /**
   * Lista todas las propuestas de cambio de este proyecto.
   */
  listarPropuestas(idProyecto: number): Observable<IaPropuesta[]> {
    return this.http.get<IaPropuesta[]>(
      `${this.baseUrl}/proyecto/${idProyecto}/propuestas`
    );
  }

  /**
   * Obtiene el detalle de una propuesta.
   */
  obtenerPropuesta(idProyecto: number, idPropuesta: number): Observable<IaPropuesta> {
    return this.http.get<IaPropuesta>(
      `${this.baseUrl}/proyecto/${idProyecto}/propuestas/${idPropuesta}`
    );
  }

  /**
   * Registra una decisión explícita (APROBAR o RECHAZAR).
   */
  decidirPropuesta(
    idProyecto: number,
    idPropuesta: number,
    decision: IaDecisionRequest
  ): Observable<IaPropuesta> {
    return this.http.post<IaPropuesta>(
      `${this.baseUrl}/proyecto/${idProyecto}/propuestas/${idPropuesta}/decision`,
      decision
    );
  }

  /**
   * Aplica formalmente una propuesta aprobada sobre el modelo UML oficial.
   */
  aplicarPropuesta(idProyecto: number, idPropuesta: number): Observable<IaPropuesta> {
    return this.http.post<IaPropuesta>(
      `${this.baseUrl}/proyecto/${idProyecto}/propuestas/${idPropuesta}/aplicar`,
      {}
    );
  }

  /**
   * Revierte una propuesta aplicada (vuelve atrás al estado previo).
   */
  revertirPropuesta(idProyecto: number, idPropuesta: number): Observable<IaPropuesta> {
    return this.http.post<IaPropuesta>(
      `${this.baseUrl}/proyecto/${idProyecto}/propuestas/${idPropuesta}/revertir`,
      {}
    );
  }

  /**
   * Ejecuta o consulta el diagnóstico programático del modelo UML.
   */
  obtenerDiagnostico(idProyecto: number, idModelo?: number): Observable<IaUmlFinding[]> {
    const params = idModelo ? `?idModelo=${idModelo}` : '';
    return this.http.get<IaUmlFinding[]>(
      `${this.baseUrl}/proyecto/${idProyecto}/diagnostico${params}`
    );
  }

  /**
   * Suscribe a los eventos del chat colaborativo en tiempo real.
   */
  suscribirChat(idProyecto: number, callback: (mensaje: IaChatMensaje) => void): void {
    this.colaboracionService.suscribirTopic(
      `/topic/proyecto/${idProyecto}/ia-chat`,
      callback
    );
  }

  /**
   * Suscribe a los eventos de propuestas en tiempo real.
   */
  suscribirPropuestas(idProyecto: number, callback: (propuesta: IaPropuesta) => void): void {
    this.colaboracionService.suscribirTopic(
      `/topic/proyecto/${idProyecto}/ia-propuestas`,
      callback
    );
  }

  /**
   * Suscribe a la notificación de que el modelo fue actualizado para recargar el canvas.
   */
  suscribirModeloActualizado(idProyecto: number, callback: (evento: any) => void): void {
    this.colaboracionService.suscribirTopic(
      `/topic/proyecto/${idProyecto}/modelo-actualizado`,
      callback
    );
  }
}
