export type ModoIa =
  | 'GENERAL'
  | 'MODELADOR_UML'
  | 'REQUISITOS'
  | 'ARQUITECTO'
  | 'DESARROLLADOR'
  | 'REVISOR_PRUEBAS'
  | 'DOCUMENTADOR'
  | 'ANALISTA_ERRORES'
  | 'IMPACTO'
  | 'DECISIONES';

export interface IaUmlFinding {
  id: string;
  elementoAfectado: string;
  tipoProblema: 'ERROR_ESTRUCTURAL' | 'INCONSISTENCIA' | 'REGLA_NEGOCIO' | 'RECOMENDACION_DISENO' | string;
  severidad: 'ALTA' | 'MEDIA' | 'BAJA' | 'INFO' | string;
  descripcion: string;
  evidencia?: string;
  propuestaSolucion?: string;
  posiblesEfectosSecundarios?: string;
  requiereConfirmacion?: boolean;
}

export interface IaPropuesta {
  idPropuesta: number;
  idProyecto: number;
  idModelo: number;
  idConversacion: number;
  versionOrigen: string;
  titulo: string;
  descripcionProblema: string;
  solucionPropuesta: string;
  riesgosEfectos?: string;
  operacionesJson: string;
  estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'APLICADA' | 'OBSOLETA' | 'REVERTIDA';
  idSolicitante: number;
  nombreSolicitante: string;
  idAprobador?: number;
  nombreAprobador?: string;
  fechaCreacion: string;
  fechaDecision?: string;
  fechaAplicacion?: string;
  versionResultante?: string;
  resultadoAplicacion?: string;
  totalOperaciones?: number;
}

export interface IaChatMensaje {
  idMensaje?: number;
  idConversacion?: number;
  idUsuario?: number;
  nombreEmisor: string;
  usernameEmisor?: string;
  tipoEmisor: 'USUARIO' | 'IA' | 'SISTEMA';
  modoIa?: ModoIa | string;
  contenido: string;
  metadatosJson?: string;
  idPropuestaAsociada?: number;
  propuestaAsociada?: IaPropuesta;
  fechaEnvio: string;
}

export interface IaConversacionResponse {
  idConversacion: number;
  idProyecto: number;
  titulo: string;
  mensajes: IaChatMensaje[];
}

export interface IaPreguntaRequest {
  mensaje: string;
  modoIa: ModoIa | string;
  idModelo?: number;
  generarPropuesta?: boolean;
}

export interface IaDecisionRequest {
  decision: 'APROBAR' | 'RECHAZAR' | 'SOLICITAR_AJUSTE';
  comentario?: string;
}
