/**
 * Configuración dinámica de URLs para CodeForge Frontend.
 *
 * - Si estamos en desarrollo local con `ng serve` en localhost:4200 -> 'http://localhost:8080'
 * - En producción, Docker o AWS EC2 (cualquier IP o dominio) -> '' (usa la misma IP/host vía Proxy Nginx)
 */
export function getApiBaseUrl(): string {
  if (typeof window !== 'undefined') {
    if (window.location.hostname === 'localhost' && window.location.port === '4200') {
      return 'http://localhost:8080';
    }
  }
  return '';
}

/**
 * Retorna la URL del broker WebSocket STOMP.
 * - En localhost:4200 -> 'ws://localhost:8080/ws-colaboracion'
 * - En AWS / Docker / Producción -> 'ws://<IP_O_DOMINIO>/ws-colaboracion' (o wss:// en HTTPS)
 */
export function getWsBrokerUrl(): string {
  if (typeof window !== 'undefined') {
    if (window.location.hostname === 'localhost' && window.location.port === '4200') {
      return 'ws://localhost:8080/ws-colaboracion';
    }
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${proto}//${window.location.host}/ws-colaboracion`;
  }
  return 'ws://localhost:8080/ws-colaboracion';
}
