import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { getWsBrokerUrl } from '../config/api.config';

export interface CursorMessage {
  proyectoId: number;
  usuarioId: number;
  x: number;
  y: number;
  color: string;
  nombre?: string;
  tipo?: string; // 'MOVE' | 'LEAVE' | 'JOIN'
}

export const PALETA_COLORES: string[] = [
  '#3B82F6', // Azul
  '#EF4444', // Rojo
  '#22C55E', // Verde
  '#F59E0B', // Ámbar
  '#A855F7', // Violeta
  '#06B6D4', // Cian
  '#EC4899'  // Rosa
];

@Injectable({
  providedIn: 'root'
})
export class ColaboracionService {
  private client: Client | null = null;
  private suscripcion: StompSubscription | null = null;
  private proyectoIdActual: number | null = null;
  private conectado = false;

  /**
   * Asigna un color consistente de la paleta según el id del usuario.
   */
  obtenerColorUsuario(usuarioId: number): string {
    const indice = Math.abs(usuarioId) % PALETA_COLORES.length;
    return PALETA_COLORES[indice];
  }

  /**
   * Conecta al broker STOMP vía WebSocket nativo y se suscribe al canal de cursores del proyecto.
   */
  conectar(proyectoId: number, onCursorRecibido: (cursor: CursorMessage) => void): void {
    if (this.conectado && this.proyectoIdActual === proyectoId) {
      return;
    }

    this.desconectar();
    this.proyectoIdActual = proyectoId;

    this.client = new Client({
      brokerURL: getWsBrokerUrl(),
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (msg: string) => {
        // console.debug('[STOMP]', msg);
      }
    });

    this.client.onConnect = () => {
      this.conectado = true;
      const destino = `/topic/proyecto/${proyectoId}/cursores`;

      this.suscripcion = this.client!.subscribe(destino, (mensaje) => {
        try {
          const datos: CursorMessage = JSON.parse(mensaje.body);
          onCursorRecibido(datos);
        } catch (error) {
          console.error('Error parseando mensaje de cursor STOMP:', error);
        }
      });

      // Reconectar suscripciones adicionales registradas
      for (const [topico, cb] of this.listenersAdicionales.entries()) {
        const sub = this.client!.subscribe(topico, (m) => {
          try {
            cb(JSON.parse(m.body));
          } catch (e) {
            console.error('Error parseando mensaje topic:', topico, e);
          }
        });
        this.suscripcionesAdicionales.set(topico, sub);
      }
    };

    this.client.onDisconnect = () => {
      this.conectado = false;
      this.suscripcionesAdicionales.clear();
    };

    this.client.onWebSocketClose = () => {
      this.conectado = false;
      this.suscripcionesAdicionales.clear();
    };

    this.client.onStompError = (frame) => {
      console.warn('STOMP Error:', frame.headers['message'], frame.body);
    };

    this.client.activate();
  }

  private listenersAdicionales = new Map<string, (datos: any) => void>();
  private suscripcionesAdicionales = new Map<string, StompSubscription>();

  /**
   * Permite suscribirse a cualquier tópico STOMP (ej. /topic/proyecto/{id}/ia-chat)
   */
  suscribirTopic(destino: string, callback: (datos: any) => void): void {
    this.listenersAdicionales.set(destino, callback);
    if (this.conectado && this.client && this.client.connected) {
      if (this.suscripcionesAdicionales.has(destino)) {
        this.suscripcionesAdicionales.get(destino)?.unsubscribe();
      }
      const sub = this.client.subscribe(destino, (m) => {
        try {
          callback(JSON.parse(m.body));
        } catch (e) {
          console.error('Error en callback topic:', destino, e);
        }
      });
      this.suscripcionesAdicionales.set(destino, sub);
    }
  }

  desuscribirTopic(destino: string): void {
    this.listenersAdicionales.delete(destino);
    if (this.suscripcionesAdicionales.has(destino)) {
      this.suscripcionesAdicionales.get(destino)?.unsubscribe();
      this.suscripcionesAdicionales.delete(destino);
    }
  }

  /**
   * Envía la posición del cursor actual al servidor (/app/cursor).
   */
  enviarCursor(cursor: CursorMessage): void {
    if (!this.client || !this.client.connected) {
      return;
    }

    this.client.publish({
      destination: '/app/cursor',
      body: JSON.stringify(cursor)
    });
  }

  /**
   * Notifica la salida del usuario para que los demás eliminen el cursor inmediatamente.
   */
  enviarSalida(proyectoId: number, usuarioId: number): void {
    if (!this.client || !this.client.connected) {
      return;
    }

    const mensajeSalida: CursorMessage = {
      proyectoId,
      usuarioId,
      x: 0,
      y: 0,
      color: '',
      tipo: 'LEAVE'
    };

    this.client.publish({
      destination: '/app/cursor',
      body: JSON.stringify(mensajeSalida)
    });
  }

  /**
   * Cierra la suscripción y desconecta la sesión STOMP.
   */
  desconectar(): void {
    if (this.suscripcion) {
      this.suscripcion.unsubscribe();
      this.suscripcion = null;
    }

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    this.conectado = false;
    this.proyectoIdActual = null;
  }

  estaConectado(): boolean {
    return this.conectado && (this.client?.connected ?? false);
  }
}
