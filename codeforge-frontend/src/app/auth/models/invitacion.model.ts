export interface Invitacion {
  idInvitacion: number;
  idProyecto: number;
  nombreProyecto: string;

  idEmisor: number;
  nombreEmisor: string;

  idReceptor: number;
  nombreReceptor: string;

  rolPropuesto: string;
  estado: string;

  fechaEnvio: string;
  fechaRespuesta: string | null;
}