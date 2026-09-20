import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ContenidoUML } from '../models/intercambio.model';

@Injectable({
  providedIn: 'root'
})
export class IntercambioService {

  private readonly http = inject(HttpClient);

  private readonly API =
    'http://localhost:8080/api/intercambio';

  // =========================================================
  // EXPORTAR JSON
  // =========================================================

  exportarJson(
    idModelo: number
  ): Observable<HttpResponse<Blob>> {

    return this.http.get(
      `${this.API}/modelos/${idModelo}/json`,
      {
        observe: 'response',
        responseType: 'blob'
      }
    );
  }

  // =========================================================
  // IMPORTAR JSON
  // =========================================================

  importarJson(
    idModelo: number,
    contenido: ContenidoUML
  ): Observable<ContenidoUML> {

    return this.http.post<ContenidoUML>(
      `${this.API}/modelos/${idModelo}/json`,
      contenido
    );
  }
  exportarXmi(
  idModelo: number
): Observable<HttpResponse<Blob>> {

  return this.http.get(
    `${this.API}/modelos/${idModelo}/xmi`,
    {
      observe: 'response',
      responseType: 'blob'
    }
  );
}

importarXmi(
  idModelo: number,
  archivo: File
): Observable<ContenidoUML> {

  return this.http.post<ContenidoUML>(
    `${this.API}/modelos/${idModelo}/xmi`,
    archivo,
    {
      headers: {
        'Content-Type': 'application/xml'
      }
    }
  );
}
}