export interface BitacoraEvento {
  id?: number;
  idProyecto: number;
  usuarioId?: number | null;
  usuarioNombre: string;
  usuarioUsername?: string | null;
  esIa: boolean;
  tipoAccion: string;
  origen: 'LIENZO_MANUAL' | 'CHAT_COLABORATIVO';
  descripcion: string;
  detallesJson?: string | null;
  fechaHora: string;
}
