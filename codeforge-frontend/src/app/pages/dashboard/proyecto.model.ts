export interface Proyecto {
  idProyecto: number;
  nombre: string;
  descripcion: string | null;
  estado: string;
  idPropietario: number;
  usernamePropietario: string;
  nombrePropietario: string;
  fechaCreacion: string;
  fechaActualizacion: string;
}