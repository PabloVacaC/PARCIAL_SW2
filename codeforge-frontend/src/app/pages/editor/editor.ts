import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  OnInit,
  ViewChild,
  inject
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, concatMap, finalize, from, map, of, toArray } from 'rxjs';
import { dia, shapes } from '@joint/core';
import { CommonModule } from '@angular/common';
import { ProyectoService } from '../../core/services/proyecto';
import { InvitacionService, UsuarioBusqueda } from '../../core/services/invitacion';
import { Auth } from '../../auth/services/auth';
import { Proyecto } from '../dashboard/proyecto.model';
import { Colaborador } from './colaborador.model';
import { IntercambioService } from '../../intercambio/services/intercambio.service';
import { ColaboracionService, CursorMessage } from '../../core/services/colaboracion.service';
import { IaColaborativaService } from '../../core/services/ia-colaborativa.service';
import { IaChatMensaje, IaPropuesta, ModoIa, IaPreguntaRequest, IaUmlFinding } from '../../core/models/ia-colaborativa.model';
import { MarkdownPipe } from '../../core/pipes/markdown.pipe';

export interface CursorRemoto {
  usuarioId: number;
  x: number;
  y: number;
  color: string;
  nombre?: string;
  ultimoMovimiento?: number;
}

export interface ParametroUML {
  id: string;
  nombre: string;
  tipo: string;
  direccion?: string;
  valorInicial?: string | null;
}

export interface AtributoUML {
  id: string;
  nombre: string;
  tipo: string;
  visibilidad?: string;
  multiplicidad?: string;
  derivado?: boolean;
  estatico?: boolean;
  valorInicial?: string | null;
}

export interface OperacionUML {
  id: string;
  nombre: string;
  estatica?: boolean;
  abstracta?: boolean;
  tipoRetorno?: string;
  visibilidad?: string;
  parametros?: ParametroUML[];
}

export interface ElementoUML {
  id: string;
  tipo: string; // CLASE, INTERFACE, ENUM
  nombre: string;
  visibilidad?: string;
  estereotipo?: string | null;
  abstracto?: boolean;
  finalElemento?: boolean;
  posicion?: {
    x: number;
    y: number;
  };
  tamaño?: {
    ancho: number;
    alto: number;
  };
  atributos?: AtributoUML[];
  operaciones?: OperacionUML[];
}

export interface RelacionUML {
  id: string;
  tipo: string; // ASOCIACION, GENERALIZACION, REALIZACION, AGREGACION, COMPOSICION, DEPENDENCIA, CLASE_ASOCIACION
  origen: string;
  destino: string;
  rolOrigen?: string | null;
  rolDestino?: string | null;
  multiplicidadOrigen?: string | null;
  multiplicidadDestino?: string | null;
}

export interface EliminacionRelacionIA {
  origen: string;
  destino: string;
  tipo?: string | null;
}

export interface EliminacionPropiedadIA {
  clase: string;
  nombre: string;
}

export interface EliminacionesUML {
  clases?: string[];
  relaciones?: EliminacionRelacionIA[];
  atributos?: EliminacionPropiedadIA[];
  operaciones?: EliminacionPropiedadIA[];
}

export interface ContenidoUML {
  versionUML?: string;
  elementos?: ElementoUML[];
  relaciones?: RelacionUML[];
  eliminaciones?: EliminacionesUML;
}

export interface ModeloUML {
  idModelo: number;
  idProyecto: number;
  nombre: string;
  descripcion?: string;
  version: string;
  contenidoJson: ContenidoUML;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

/* =========================================================
   ELEMENTO JOINTJS PERSONALIZADO PARA UML CON MANEJADOR
   ========================================================= */
const UMLClass = dia.Element.define(
  'codeforge.UMLClass',
  {
    size: {
      width: 260,
      height: 180
    },
    attrs: {
      body: {
        refWidth: '100%',
        refHeight: '100%',
        fill: '#0f172a',
        stroke: '#8b5cf6',
        strokeWidth: 2,
        rx: 8,
        ry: 8,
        magnet: true
      },
      header: {
        refWidth: '100%',
        height: 38,
        fill: '#1e1b4b',
        stroke: 'none',
        rx: 8,
        ry: 8
      },
      headerLine: {
        x1: 0,
        y1: 38,
        x2: 'calc(w)',
        y2: 38,
        stroke: '#334155',
        strokeWidth: 1.5
      },
      attributesLine: {
        x1: 0,
        y1: 85,
        x2: 'calc(w)',
        y2: 85,
        stroke: '#334155',
        strokeWidth: 1.5
      },
      className: {
        x: 'calc(w / 2)',
        y: 20,
        textAnchor: 'middle',
        textVerticalAnchor: 'middle',
        fill: '#ffffff',
        fontSize: 13,
        fontWeight: 700,
        fontFamily: 'Inter, system-ui, sans-serif'
      },
      stereotype: {
        x: 'calc(w / 2)',
        y: 7,
        textAnchor: 'middle',
        fill: '#a78bfa',
        fontSize: 9,
        fontWeight: 600,
        fontFamily: 'Inter, system-ui, sans-serif'
      },
      connectHandle: {
        r: 8,
        cx: 'calc(w - 14)',
        cy: 19,
        fill: '#8b5cf6',
        stroke: '#ffffff',
        strokeWidth: 1.5,
        cursor: 'crosshair',
        magnet: true,
        title: 'Arrastra para conectar con otra clase'
      },
      attributesText: {
        x: 12,
        y: 48,
        textVerticalAnchor: 'top',
        fill: '#cbd5e1',
        fontSize: 11,
        fontFamily: 'Consolas, "Fira Code", monospace',
        lineHeight: 18
      },
      operationsText: {
        x: 12,
        y: 95,
        textVerticalAnchor: 'top',
        fill: '#94a3b8',
        fontSize: 11,
        fontFamily: 'Consolas, "Fira Code", monospace',
        lineHeight: 18
      }
    }
  },
  {
    markup: [
      { tagName: 'rect', selector: 'body' },
      { tagName: 'rect', selector: 'header' },
      { tagName: 'line', selector: 'headerLine' },
      { tagName: 'line', selector: 'attributesLine' },
      { tagName: 'text', selector: 'stereotype' },
      { tagName: 'text', selector: 'className' },
      { tagName: 'circle', selector: 'connectHandle' },
      { tagName: 'text', selector: 'attributesText' },
      { tagName: 'text', selector: 'operationsText' }
    ]
  }
);

const cellNamespace = {
  ...shapes,
  codeforge: {
    UMLClass
  }
};

interface MensajeAsistente {
  id: number;
  rol: 'usuario' | 'agente';
  contenido: string;
}

interface ContextoAsistente {
  proyecto: string | null;
  modelo: string | null;
  totalElementos: number;
  totalRelaciones: number;
  elementoSeleccionado: {
    nombre: string;
    tipo: string;
    atributos: Array<{ nombre: string; tipo: string }>;
    operaciones: Array<{
      nombre: string;
      tipoRetorno?: string;
      parametros: Array<{ nombre: string; tipo: string }>;
    }>;
  } | null;
  elementos: Array<{
    nombre: string;
    tipo: string;
    atributos: Array<{ nombre: string; tipo: string }>;
    operaciones: Array<{ nombre: string; tipoRetorno?: string }>;
  }>;
  relaciones: Array<{
    tipo: string;
    origen: string;
    destino: string;
    multiplicidadOrigen?: string | null;
    multiplicidadDestino?: string | null;
  }>;
}

@Component({
  selector: 'app-editor',
  imports: [
    FormsModule,
    CommonModule,
    MarkdownPipe
  ],
  templateUrl: './editor.html',
  styleUrl: './editor.css'
})
export class Editor implements OnInit, AfterViewInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly proyectoService = inject(ProyectoService);
  private readonly invitacionService = inject(InvitacionService);
  private readonly auth = inject(Auth);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly http = inject(HttpClient);
  private readonly intercambioService = inject(IntercambioService);
  private readonly colaboracionService = inject(ColaboracionService);
  private readonly iaColaborativaService = inject(IaColaborativaService);
  private readonly ngZone = inject(NgZone);

  // =========================================================
  // AGENTE DE AYUDA DE CODEFORGE Y DETECCIÓN DE INACTIVIDAD
  // =========================================================

  asistenteAbierto = false;
  asistenteProcesando = false;
  asistenteParpadeando = false;
  private readonly TIEMPO_INACTIVIDAD_MS = 60000; // 1 minuto (60 segundos)
  private idleTimer: any = null;
  private unbindIdleListeners?: () => void;
  preguntaAsistente = '';
  mensajesAsistente: MensajeAsistente[] = [
    {
      id: 1,
      rol: 'agente',
      contenido:
        'Hola 👋 Soy el asistente de CodeForge. Puedo explicarte cómo crear clases, atributos, operaciones, relaciones, importar/exportar modelos y generar el backend. También puedo explicarte lo que estás viendo ahora mismo.'
    }
  ];
  private siguienteIdMensajeAsistente = 2;

  sugerenciasAsistente = [
    '¿Cómo creo una clase?',
    '¿Cómo agrego un atributo?',
    '¿Cómo creo una relación?',
    '¿Cómo genero el backend?',
    '¿Cómo importo un XMI?',
    '¿Cómo uso la IA con una imagen?'
  ];

  abrirAsistente(): void {
    // Al entrar al asistente, se apaga el parpadeo de llamado de atención
    this.asistenteParpadeando = false;
    if (this.idleTimer) {
      clearTimeout(this.idleTimer);
      this.idleTimer = null;
    }

    this.asistenteAbierto = true;
    this.changeDetector.detectChanges();

    setTimeout(() => {
      const input = document.getElementById('preguntaAsistente') as HTMLInputElement | null;
      input?.focus();
    }, 50);
  }

  cerrarAsistente(): void {
    if (this.asistenteProcesando) {
      return;
    }

    this.asistenteAbierto = false;
    this.asistenteParpadeando = false;
    this.changeDetector.detectChanges();

    // Al salir, no parpadeará hasta que se quede sin hacer nada durante 1 minuto completo
    this.reiniciarTemporizadorInactividad();
  }

  // =========================================================
  // DETECCIÓN DE INACTIVIDAD (ASISTENTE IA)
  // =========================================================

  private iniciarDetectorInactividad(): void {
    this.reiniciarTemporizadorInactividad();

    this.ngZone.runOutsideAngular(() => {
      const resetOnActivity = () => {
        // Si no está parpadeando y el modal no está abierto, reiniciamos el temporizador de 1 minuto
        if (!this.asistenteParpadeando && !this.asistenteAbierto) {
          this.reiniciarTemporizadorInactividad();
        }
      };

      const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'wheel'];
      events.forEach(eventName => {
        window.addEventListener(eventName, resetOnActivity, { passive: true });
      });

      this.unbindIdleListeners = () => {
        events.forEach(eventName => {
          window.removeEventListener(eventName, resetOnActivity);
        });
        if (this.idleTimer) {
          clearTimeout(this.idleTimer);
          this.idleTimer = null;
        }
      };
    });
  }

  private reiniciarTemporizadorInactividad(): void {
    if (this.idleTimer) {
      clearTimeout(this.idleTimer);
      this.idleTimer = null;
    }

    if (this.asistenteAbierto) {
      return;
    }

    this.idleTimer = setTimeout(() => {
      if (!this.asistenteAbierto) {
        this.ngZone.run(() => {
          this.asistenteParpadeando = true;
          this.changeDetector.detectChanges();
        });
      }
    }, this.TIEMPO_INACTIVIDAD_MS);
  }

  usarSugerenciaAsistente(pregunta: string): void {
    this.preguntaAsistente = pregunta;
    this.preguntarAlAsistente();
  }

  preguntarAlAsistente(): void {
    const pregunta = this.preguntaAsistente.trim();

    if (!pregunta || this.asistenteProcesando) {
      return;
    }

    this.mensajesAsistente.push({
      id: this.siguienteIdMensajeAsistente++,
      rol: 'usuario',
      contenido: pregunta
    });

    this.preguntaAsistente = '';
    this.asistenteProcesando = true;
    this.changeDetector.detectChanges();

    const contexto = this.construirContextoAsistente();

    const solicitud = {
      pregunta,
      contexto
    };

    this.http
      .post<{ respuesta?: string; message?: string; content?: string }>(
        'http://localhost:8080/api/ia/asistente',
        solicitud
      )
      .subscribe({
        next: (respuesta) => {
          const texto =
            respuesta?.respuesta?.trim() ||
            respuesta?.message?.trim() ||
            respuesta?.content?.trim();

          this.agregarRespuestaAsistente(
            texto || this.generarRespuestaLocalAsistente(pregunta, contexto)
          );
        },
        error: (error) => {
          console.warn(
            'CODEFORGE AGENTE: endpoint IA no disponible, usando ayuda local.',
            error?.status
          );

          // El asistente sigue funcionando aunque el endpoint generativo
          // todavía no esté conectado en el backend.
          this.agregarRespuestaAsistente(
            this.generarRespuestaLocalAsistente(pregunta, contexto)
          );
        }
      });
  }

  private agregarRespuestaAsistente(texto: string): void {
    this.mensajesAsistente.push({
      id: this.siguienteIdMensajeAsistente++,
      rol: 'agente',
      contenido: texto
    });

    this.asistenteProcesando = false;
    this.changeDetector.detectChanges();

    setTimeout(() => {
      const contenedor = document.getElementById('mensajesAsistente');
      if (contenedor) {
        contenedor.scrollTop = contenedor.scrollHeight;
      }
    }, 0);
  }

  private construirContextoAsistente(): ContextoAsistente {
    return {
      proyecto: this.proyecto?.nombre || null,
      modelo: this.modeloActual?.nombre || null,
      totalElementos: this.elementos.length,
      totalRelaciones: this.relaciones.length,
      elementoSeleccionado: this.elementoSeleccionado
        ? {
            nombre: this.elementoSeleccionado.nombre,
            tipo: this.elementoSeleccionado.tipo,
            atributos: (this.elementoSeleccionado.atributos ?? []).map(a => ({
              nombre: a.nombre,
              tipo: a.tipo
            })),
            operaciones: (this.elementoSeleccionado.operaciones ?? []).map(o => ({
              nombre: o.nombre,
              tipoRetorno: o.tipoRetorno,
              parametros: (o.parametros ?? []).map(p => ({
                nombre: p.nombre,
                tipo: p.tipo
              }))
            }))
          }
        : null,
      elementos: this.elementos.slice(0, 30).map(e => ({
        nombre: e.nombre,
        tipo: e.tipo,
        atributos: (e.atributos ?? []).slice(0, 20).map(a => ({
          nombre: a.nombre,
          tipo: a.tipo
        })),
        operaciones: (e.operaciones ?? []).slice(0, 20).map(o => ({
          nombre: o.nombre,
          tipoRetorno: o.tipoRetorno
        }))
      })),
      relaciones: this.relaciones.slice(0, 40).map(r => ({
        tipo: r.tipo,
        origen: this.obtenerNombreElemento(r.origen) || r.origen,
        destino: this.obtenerNombreElemento(r.destino) || r.destino,
        multiplicidadOrigen: r.multiplicidadOrigen,
        multiplicidadDestino: r.multiplicidadDestino
      }))
    };
  }

  private generarRespuestaLocalAsistente(
    pregunta: string,
    contexto: ContextoAsistente
  ): string {
    const q = pregunta
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();

    if (q.includes('crear') && q.includes('clase') || q.includes('agregar') && q.includes('clase')) {
      return 'Para crear una clase, pulsa + Clase en el panel izquierdo o en la barra flotante del lienzo. Luego escribe el nombre, define la visibilidad y pulsa Crear Elemento. La clase aparecerá en el lienzo y también en la lista de elementos.';
    }

    if (q.includes('atributo')) {
      return 'Selecciona una clase en el lienzo. En el inspector de la derecha, entra en ATRIBUTOS y pulsa + Atributo. Allí puedes definir nombre, tipo, visibilidad, multiplicidad, valor inicial, static y derivado.';
    }

    if (q.includes('operacion') || q.includes('metodo') || q.includes('método')) {
      return 'Selecciona una clase y en OPERACIONES (MÉTODOS) pulsa + Operación. Puedes indicar nombre, tipo de retorno, visibilidad, parámetros, static y abstract. Los parámetros también pueden tener dirección IN, OUT o INOUT.';
    }

    if (q.includes('parametro') || q.includes('parámetro')) {
      return 'Los parámetros se agregan desde el formulario de una operación. Escribe el nombre, el tipo y la dirección (IN, OUT o INOUT), y pulsa + Agregar. La firma se actualizará automáticamente en la vista previa UML.';
    }

    if (q.includes('relacion') || q.includes('relación') || q.includes('asociacion') || q.includes('asociación') || q.includes('herencia') || q.includes('composicion') || q.includes('agregacion')) {
      return 'Para crear una relación puedes usar Conectar Clases en el panel izquierdo o + Relación. En el formulario puedes elegir Asociación, Generalización, Realización, Agregación, Composición o Dependencia, además de origen, destino, roles y multiplicidades.';
    }

    if (q.includes('backend') || q.includes('generar') && q.includes('codigo')) {
      return 'Para generar el backend, pulsa Generar Backend en la barra superior. CodeForge utiliza el modelo UML como blueprint y genera la estructura Spring Boot, incluyendo entidades, DTOs, repositorios, servicios y controladores según el modelo.';
    }

    if (q.includes('imagen') || q.includes('gemini') || q.includes('ia')) {
      return 'Para convertir una imagen UML en un modelo, pulsa Analizar imagen UML, selecciona una imagen PNG, JPG, JPEG o WEBP y espera el análisis. La IA devuelve un modelo UML estructurado que CodeForge agrega al diagrama sin borrar lo que ya existe.';
    }

    if (q.includes('xmi')) {
      return 'En INTERCAMBIO puedes usar Importar XMI para cargar un modelo compatible y Exportar XMI para guardar el diagrama en ese formato. CodeForge también mantiene las opciones equivalentes para JSON.';
    }

    if (q.includes('json') && (q.includes('import') || q.includes('export') || q.includes('guardar') || q.includes('abrir'))) {
      return 'En INTERCAMBIO puedes Exportar JSON para guardar el modelo actual o Importar JSON para cargar un modelo UML previamente guardado.';
    }

    if (q.includes('colaborador') || q.includes('colaboracion') || q.includes('colaboración')) {
      return 'El módulo de colaboradores permite invitar usuarios al proyecto y mantener los cambios del modelo sincronizados en tiempo real entre los participantes.';
    }

    if (q.includes('zoom') || q.includes('centrar')) {
      return 'En la barra flotante del lienzo tienes − para alejar, + para acercar, el porcentaje para restablecer el zoom y Centrar para ajustar el diagrama a la pantalla.';
    }

    if (q.includes('que tengo') || q.includes('qué tengo') || q.includes('diagrama') || q.includes('modelo')) {
      return `Actualmente estás trabajando en ${contexto.modelo || 'tu modelo UML'}${contexto.proyecto ? ` del proyecto ${contexto.proyecto}` : ''}. El diagrama contiene ${contexto.totalElementos} elemento(s) y ${contexto.totalRelaciones} relación(es).`;
    }

    if (contexto.elementoSeleccionado) {
      return `Tienes seleccionada la ${contexto.elementoSeleccionado.tipo.toLowerCase()} ${contexto.elementoSeleccionado.nombre}. Desde el inspector puedes modificar sus propiedades, atributos, operaciones y relaciones. También puedo explicarte cualquiera de esas secciones.`;
    }

    return 'Puedo ayudarte a usar CodeForge. Pregúntame, por ejemplo: “¿Cómo creo una clase?”, “¿Cómo agrego un atributo?”, “¿Cómo hago una relación?”, “¿Cómo importo XMI?” o “¿Cómo genero el backend?”';
  }

  // =========================================================
  // IA DESARROLLADORA COLABORATIVA (EQUIPO EN VIVO)
  // =========================================================

  chatColaborativoAbierto = false;
  iaColaborativaProcesando = false;
  // Configuración de Modo de Aprobación de Propuestas IA (Manual vs Automático)
  modoAprobacionIa: 'MANUAL' | 'AUTOMATICO' = 'MANUAL';
  menuAprobacionAbierto = false;
  autoAplicandoPropuesta = false;
  propuestaRevertirSeleccionada: IaPropuesta | null = null;
  revertiendoPropuestaId: number | null = null;
  modoIaSeleccionado: ModoIa = 'GENERAL';
  textoMensajeColaborativo = '';
  generarPropuestaCheck = false;
  mensajesColaborativos: IaChatMensaje[] = [];
  propuestasColaborativas: IaPropuesta[] = [];
  tabColaborativoActivo: 'CHAT' | 'PROPUESTAS' | 'HALLAZGOS' = 'CHAT';
  hallazgosUml: IaUmlFinding[] = [];
  cargandoHallazgos = false;
  filtroEstadoPropuestas: string = 'TODAS';
  propuestaSeleccionadaParaVer: IaPropuesta | null = null;
  modalVerPropuestaAbierto = false;
  aplicandoPropuestaId: number | null = null;
  notificacionColaborativa: string | null = null;
  tipoNotificacionColaborativa: 'success' | 'error' | 'info' = 'info';

  modosDisponibles: Array<{ id: ModoIa; nombre: string; icono: string; descripcion: string }> = [
    { id: 'GENERAL', nombre: 'General', icono: '✦', descripcion: 'Colaborador técnico del equipo' },
    { id: 'MODELADOR_UML', nombre: 'Modelador UML', icono: '📐', descripcion: 'Diagnóstico y estructura UML' },
    { id: 'REQUISITOS', nombre: 'Requisitos', icono: '📋', descripcion: 'Auditoría y reglas de negocio' },
    { id: 'ARQUITECTO', nombre: 'Arquitecto', icono: '🏛️', descripcion: 'Capas y patrones Spring Boot' },
    { id: 'DESARROLLADOR', nombre: 'Desarrollador', icono: '💻', descripcion: 'Entidades, DTOs y lógica' },
    { id: 'REVISOR_PRUEBAS', nombre: 'Pruebas / Revisor', icono: '🧪', descripcion: 'Revisión y pruebas automatizadas' },
    { id: 'ANALISTA_ERRORES', nombre: 'Analista Errores', icono: '🔍', descripcion: 'Detección de bugs e inconsistencias' },
    { id: 'DOCUMENTADOR', nombre: 'Documentador', icono: '📖', descripcion: 'ADRs y notas de cambios' },
    { id: 'IMPACTO', nombre: 'Impacto de Cambios', icono: '⚡', descripcion: 'Análisis de impacto y dependencias' },
    { id: 'DECISIONES', nombre: 'Decisiones', icono: '💡', descripcion: 'Explicación técnica de alternativas' }
  ];

  abrirChatColaborativo(): void {
    this.chatColaborativoAbierto = true;
    if (this.idProyectoActual) {
      this.cargarConversacionColaborativa(this.idProyectoActual);
      this.cargarPropuestasColaborativas(this.idProyectoActual);
      this.cargarDiagnosticoUml();
    }
    this.scrollChatAlFondo();
    this.changeDetector.detectChanges();
  }

  cerrarChatColaborativo(): void {
    this.chatColaborativoAbierto = false;
    this.changeDetector.detectChanges();
  }

  cambiarModoIa(modo: ModoIa): void {
    this.modoIaSeleccionado = modo;
    this.changeDetector.detectChanges();
  }

  toggleMenuAprobacion(event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.menuAprobacionAbierto = !this.menuAprobacionAbierto;
    this.changeDetector.detectChanges();
  }

  cerrarMenuAprobacion(): void {
    if (this.menuAprobacionAbierto) {
      this.menuAprobacionAbierto = false;
      this.changeDetector.detectChanges();
    }
  }

  seleccionarModoAprobacion(modo: 'MANUAL' | 'AUTOMATICO'): void {
    this.modoAprobacionIa = modo;
    this.menuAprobacionAbierto = false;
    try {
      localStorage.setItem('codeforge_modo_aprobacion_ia', modo);
    } catch {}

    if (modo === 'AUTOMATICO') {
      this.generarPropuestaCheck = true;
      this.mostrarAlertaColaborativa('🛡️ Modo automático activado: las propuestas de la IA se aceptarán e implementarán en el diseño de forma inmediata.', 'success');
    } else {
      this.mostrarAlertaColaborativa('✋ Modo manual activado: deberás aprobar o rechazar manualmente cada propuesta antes de aplicarla.', 'info');
    }
    this.changeDetector.detectChanges();
  }

  aprobarYAplicarAutomaticamente(propuesta: IaPropuesta): void {
    if (!this.idProyectoActual || !propuesta || this.aplicandoPropuestaId || this.autoAplicandoPropuesta) {
      return;
    }

    this.autoAplicandoPropuesta = true;
    this.aplicandoPropuestaId = propuesta.idPropuesta;
    this.mostrarAlertaColaborativa(`⚡ [Modo Automático] Aprobando propuesta #${propuesta.idPropuesta}: "${propuesta.titulo}"...`, 'info');
    this.changeDetector.detectChanges();

    // 1. Aprobar en backend
    this.iaColaborativaService.decidirPropuesta(this.idProyectoActual, propuesta.idPropuesta, { decision: 'APROBAR' }).subscribe({
      next: (propAprobada) => {
        propuesta.estado = propAprobada.estado;
        if (propAprobada.versionResultante) propuesta.versionResultante = propAprobada.versionResultante;
        if (propAprobada.nombreAprobador) propuesta.nombreAprobador = propAprobada.nombreAprobador;
        this.sincronizarPropuestaEnEstado(propAprobada);

        // 2. Aplicar inmediatamente al diseño UML
        this.iaColaborativaService.aplicarPropuesta(this.idProyectoActual!, propuesta.idPropuesta).subscribe({
          next: (propAplicada) => {
            this.aplicandoPropuestaId = null;
            this.autoAplicandoPropuesta = false;
            propuesta.estado = propAplicada.estado;
            if (propAplicada.versionResultante) propuesta.versionResultante = propAplicada.versionResultante;
            this.sincronizarPropuestaEnEstado(propAplicada);

            this.mostrarAlertaColaborativa(`✓ [Modo Automático] Propuesta #${propAplicada.idPropuesta} ("${propAplicada.titulo}") aprobada e implementada en el diseño (v${propAplicada.versionResultante || 'nueva'}).`, 'success');

            // Refrescar diagrama UML en vivo, listas y diagnóstico
            this.cargarModeloUML(this.idProyectoActual!);
            this.cargarPropuestasColaborativas(this.idProyectoActual!);
            this.cargarConversacionColaborativa(this.idProyectoActual!);
            this.cargarDiagnosticoUml();
            this.changeDetector.detectChanges();
          },
          error: (errAplicar) => {
            this.aplicandoPropuestaId = null;
            this.autoAplicandoPropuesta = false;
            const msg = errAplicar.error?.message || errAplicar.message || 'Error al aplicar cambios';
            this.mostrarAlertaColaborativa(`Error al aplicar automáticamente la propuesta: ${msg}`, 'error');
            this.changeDetector.detectChanges();
          }
        });
      },
      error: (errAprobar) => {
        this.aplicandoPropuestaId = null;
        this.autoAplicandoPropuesta = false;
        const msg = errAprobar.error?.message || errAprobar.message || 'Error al aprobar';
        this.mostrarAlertaColaborativa(`Error al auto-aprobar propuesta: ${msg}`, 'error');
        this.changeDetector.detectChanges();
      }
    });
  }

  enviarMensajeColaborativo(): void {
    const texto = this.textoMensajeColaborativo.trim();
    if (!texto || this.iaColaborativaProcesando || !this.idProyectoActual) return;

    this.iaColaborativaProcesando = true;
    this.textoMensajeColaborativo = '';

    const req: IaPreguntaRequest = {
      mensaje: texto,
      modoIa: this.modoIaSeleccionado,
      idModelo: this.modeloActual?.idModelo,
      generarPropuesta: this.generarPropuestaCheck
    };

    this.iaColaborativaService.enviarMensaje(this.idProyectoActual, req).subscribe({
      next: (nuevos) => {
        for (const m of nuevos) {
          if (!this.mensajesColaborativos.some(ex => ex.idMensaje === m.idMensaje)) {
            this.mensajesColaborativos.push(m);
          }
        }
        this.iaColaborativaProcesando = false;
        this.cargarPropuestasColaborativas(this.idProyectoActual!);
        this.scrollChatAlFondo();
        this.changeDetector.detectChanges();

        // Si está en modo AUTOMÁTICO, auto-aprobar y aplicar al diseño inmediatamente
        if (this.modoAprobacionIa === 'AUTOMATICO') {
          for (const m of nuevos) {
            if (m.propuestaAsociada && m.propuestaAsociada.estado === 'PENDIENTE') {
              this.aprobarYAplicarAutomaticamente(m.propuestaAsociada);
              break;
            }
          }
          // Respaldo por si la propuesta se asoció por id o fue devuelta en la lista
          setTimeout(() => {
            if (this.modoAprobacionIa === 'AUTOMATICO' && !this.autoAplicandoPropuesta && this.propuestasPendientes.length > 0) {
              const primeraPendiente = this.propuestasPendientes[0];
              if (primeraPendiente && primeraPendiente.estado === 'PENDIENTE') {
                this.aprobarYAplicarAutomaticamente(primeraPendiente);
              }
            }
          }, 350);
        }
      },
      error: (err) => {
        this.iaColaborativaProcesando = false;
        this.mostrarAlertaColaborativa('Error al comunicarse con la IA: ' + (err.error?.message || err.message || 'Error de conexión'), 'error');
        this.changeDetector.detectChanges();
      }
    });
  }

  ejecutarAccionRapidaIa(promptRapido: string, modo: ModoIa = 'MODELADOR_UML', forzarPropuesta = false): void {
    this.modoIaSeleccionado = modo;
    this.textoMensajeColaborativo = promptRapido;
    this.generarPropuestaCheck = forzarPropuesta;
    this.enviarMensajeColaborativo();
  }

  cargarConversacionColaborativa(idProyecto: number): void {
    this.iaColaborativaService.obtenerConversacion(idProyecto).subscribe({
      next: (resp) => {
        this.mensajesColaborativos = resp.mensajes || [];
        this.scrollChatAlFondo();
        this.changeDetector.detectChanges();
      },
      error: (err) => console.warn('No se pudo cargar la conversación colaborativa:', err)
    });
  }

  cargarPropuestasColaborativas(idProyecto: number): void {
    this.iaColaborativaService.listarPropuestas(idProyecto).subscribe({
      next: (props) => {
        this.propuestasColaborativas = props || [];
        this.changeDetector.detectChanges();
      },
      error: (err) => console.warn('No se pudieron cargar propuestas:', err)
    });
  }

  iniciarWebSocketIaColaborativa(idProyecto: number): void {
    this.iaColaborativaService.suscribirChat(idProyecto, (nuevoMensaje) => {
      if (!this.mensajesColaborativos.some(m => m.idMensaje === nuevoMensaje.idMensaje)) {
        this.mensajesColaborativos.push(nuevoMensaje);
        this.scrollChatAlFondo();
        this.changeDetector.detectChanges();
      }
    });

    this.iaColaborativaService.suscribirPropuestas(idProyecto, (propuesta) => {
      this.sincronizarPropuestaEnEstado(propuesta);
    });

    this.iaColaborativaService.suscribirModeloActualizado(idProyecto, (evt) => {
      console.log('IA COLABORATIVA: Modelo actualizado externamente, recargando lienzo...', evt);
      this.cargarModeloUML(idProyecto);
      this.cargarDiagnosticoUml();
      this.mostrarAlertaColaborativa('El modelo UML fue actualizado a la versión ' + (evt?.version || 'nueva'), 'info');
    });
  }

  sincronizarPropuestaEnEstado(propuestaActualizada: IaPropuesta): void {
    if (!propuestaActualizada) return;

    // 1. Sincronizar en la lista de propuestas del tab de propuestas
    const idx = this.propuestasColaborativas.findIndex(p => p.idPropuesta === propuestaActualizada.idPropuesta);
    if (idx >= 0) {
      this.propuestasColaborativas[idx] = { ...this.propuestasColaborativas[idx], ...propuestaActualizada };
    } else {
      this.propuestasColaborativas.unshift(propuestaActualizada);
    }

    // 2. Sincronizar en todos los mensajes del chat que referencien esta propuesta
    if (this.mensajesColaborativos && this.mensajesColaborativos.length > 0) {
      for (const m of this.mensajesColaborativos) {
        if (m.propuestaAsociada && m.propuestaAsociada.idPropuesta === propuestaActualizada.idPropuesta) {
          m.propuestaAsociada = { ...m.propuestaAsociada, ...propuestaActualizada };
        }
      }
    }

    // 3. Sincronizar en la propuesta del modal de detalles/diffs si está abierta
    if (this.propuestaSeleccionadaParaVer && this.propuestaSeleccionadaParaVer.idPropuesta === propuestaActualizada.idPropuesta) {
      this.propuestaSeleccionadaParaVer = { ...this.propuestaSeleccionadaParaVer, ...propuestaActualizada };
    }

    // 4. Forzar actualización inmediata en el template de Angular
    this.changeDetector.markForCheck();
    this.changeDetector.detectChanges();
  }

  cargarDiagnosticoUml(): void {
    if (!this.idProyectoActual) return;
    this.cargandoHallazgos = true;
    this.iaColaborativaService.obtenerDiagnostico(this.idProyectoActual, this.modeloActual?.idModelo).subscribe({
      next: (findings) => {
        this.hallazgosUml = findings || [];
        this.cargandoHallazgos = false;
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        console.warn('Error al obtener diagnóstico UML:', err);
        this.cargandoHallazgos = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  solicitarCorreccionHallazgo(h: IaUmlFinding): void {
    this.tabColaborativoActivo = 'CHAT';
    this.modoIaSeleccionado = 'MODELADOR_UML';
    this.generarPropuestaCheck = true;
    this.textoMensajeColaborativo = `Genera una propuesta formal para corregir el problema en '${h.elementoAfectado}' (${h.tipoProblema}): ${h.descripcion}. Corrección sugerida: ${h.propuestaSolucion || 'Ajustar estructura UML'}.`;
    this.enviarMensajeColaborativo();
  }

  aprobarPropuesta(propuesta: IaPropuesta): void {
    if (!this.idProyectoActual) return;
    this.iaColaborativaService.decidirPropuesta(this.idProyectoActual, propuesta.idPropuesta, { decision: 'APROBAR' }).subscribe({
      next: (p) => {
        // Actualizar directamente la instancia recibida y sincronizar todo el estado de la vista de inmediato
        propuesta.estado = p.estado;
        if (p.versionResultante) propuesta.versionResultante = p.versionResultante;
        if (p.nombreAprobador) propuesta.nombreAprobador = p.nombreAprobador;
        this.sincronizarPropuestaEnEstado(p);

        this.mostrarAlertaColaborativa(`Propuesta #${p.idPropuesta} aprobada con éxito. Ya puedes aplicarla al diagrama.`, 'success');
        this.cargarPropuestasColaborativas(this.idProyectoActual!);
        this.cargarConversacionColaborativa(this.idProyectoActual!);
      },
      error: (err) => this.mostrarAlertaColaborativa(err.error?.message || 'Error al aprobar propuesta', 'error')
    });
  }

  rechazarPropuesta(propuesta: IaPropuesta): void {
    if (!this.idProyectoActual) return;
    const comentario = prompt('Motivo del rechazo (opcional):') || undefined;
    this.iaColaborativaService.decidirPropuesta(this.idProyectoActual, propuesta.idPropuesta, { decision: 'RECHAZAR', comentario }).subscribe({
      next: (p) => {
        // Actualizar directamente la instancia recibida y sincronizar todo el estado de la vista de inmediato
        propuesta.estado = p.estado;
        if (p.versionResultante) propuesta.versionResultante = p.versionResultante;
        this.sincronizarPropuestaEnEstado(p);

        this.mostrarAlertaColaborativa(`Propuesta #${p.idPropuesta} rechazada.`, 'info');
        this.cargarPropuestasColaborativas(this.idProyectoActual!);
        this.cargarConversacionColaborativa(this.idProyectoActual!);
      },
      error: (err) => this.mostrarAlertaColaborativa(err.error?.message || 'Error al rechazar propuesta', 'error')
    });
  }

  aplicarPropuestaDefinitiva(propuesta: IaPropuesta): void {
    if (!this.idProyectoActual || this.aplicandoPropuestaId) return;

    if (!confirm(`¿Confirmas aplicar la propuesta #${propuesta.idPropuesta}: "${propuesta.titulo}"?\nSe incrementará la versión del modelo y se actualizará el diagrama de todo el equipo.`)) {
      return;
    }

    this.aplicandoPropuestaId = propuesta.idPropuesta;
    this.changeDetector.detectChanges();

    this.iaColaborativaService.aplicarPropuesta(this.idProyectoActual, propuesta.idPropuesta).subscribe({
      next: (p) => {
        this.aplicandoPropuestaId = null;
        propuesta.estado = p.estado;
        if (p.versionResultante) propuesta.versionResultante = p.versionResultante;
        this.sincronizarPropuestaEnEstado(p);

        this.mostrarAlertaColaborativa(`¡Propuesta #${p.idPropuesta} aplicada! Nueva versión del modelo: ${p.versionResultante}`, 'success');
        this.cargarPropuestasColaborativas(this.idProyectoActual!);
        this.cargarConversacionColaborativa(this.idProyectoActual!);
        this.cargarModeloUML(this.idProyectoActual!);
        this.cargarDiagnosticoUml();
        this.modalVerPropuestaAbierto = false;
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.aplicandoPropuestaId = null;
        const msg = err.error?.message || err.message || 'Error al aplicar propuesta';
        this.mostrarAlertaColaborativa(msg, 'error');
        this.changeDetector.detectChanges();
      }
    });
  }

  iniciarReversionPropuesta(propuesta: IaPropuesta, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    if (this.propuestaRevertirSeleccionada?.idPropuesta === propuesta.idPropuesta) {
      this.propuestaRevertirSeleccionada = null;
    } else {
      this.propuestaRevertirSeleccionada = propuesta;
    }
    this.changeDetector.detectChanges();
  }

  cancelarReversionPropuesta(event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.propuestaRevertirSeleccionada = null;
    this.changeDetector.detectChanges();
  }

  confirmarReversionPropuesta(propuesta: IaPropuesta, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    if (!this.idProyectoActual || this.revertiendoPropuestaId) return;

    this.revertiendoPropuestaId = propuesta.idPropuesta;
    this.changeDetector.detectChanges();

    this.iaColaborativaService.revertirPropuesta(this.idProyectoActual, propuesta.idPropuesta).subscribe({
      next: (propRevertida) => {
        this.revertiendoPropuestaId = null;
        this.propuestaRevertirSeleccionada = null;
        propuesta.estado = propRevertida.estado;
        if (propRevertida.versionResultante) propuesta.versionResultante = propRevertida.versionResultante;
        this.sincronizarPropuestaEnEstado(propRevertida);

        this.mostrarAlertaColaborativa(`⏪ Propuesta #${propRevertida.idPropuesta} revertida con éxito. El diagrama UML volvió a la versión v${propRevertida.versionOrigen || 'previa'}.`, 'success');

        this.cargarModeloUML(this.idProyectoActual!);
        this.cargarPropuestasColaborativas(this.idProyectoActual!);
        this.cargarConversacionColaborativa(this.idProyectoActual!);
        this.cargarDiagnosticoUml();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.revertiendoPropuestaId = null;
        this.propuestaRevertirSeleccionada = null;
        const msg = err.error?.message || err.message || 'Error al revertir propuesta';
        this.mostrarAlertaColaborativa(`Error al volver atrás: ${msg}`, 'error');
        this.changeDetector.detectChanges();
      }
    });
  }

  verDetallePropuesta(propuesta: IaPropuesta): void {
    this.propuestaSeleccionadaParaVer = propuesta;
    this.modalVerPropuestaAbierto = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalVerPropuesta(): void {
    this.modalVerPropuestaAbierto = false;
    this.propuestaSeleccionadaParaVer = null;
    this.changeDetector.detectChanges();
  }

  obtenerOperacionesParseadas(operacionesJson: string): any[] {
    try {
      return JSON.parse(operacionesJson) || [];
    } catch {
      return [];
    }
  }

  formatearDetalleOperacion(op: any): string {
    if (!op) return '';
    const tipo = op.tipo || '';
    if (tipo === 'CREAR_CLASE' || tipo === 'CREAR_ELEMENTO') {
      const elem = op.elemento || {};
      const atrs = (elem.atributos || []).map((a: any) => `${a.nombre}: ${a.tipo}`).join(', ');
      return `Crear ${elem.tipo || 'CLASE'} '${elem.nombre}' ${atrs ? '(' + atrs + ')' : ''}`;
    }
    if (tipo === 'AGREGAR_ATRIBUTO') {
      return `Agregar atributo '${op.atributo?.nombre}: ${op.atributo?.tipo}' en clase '${op.clase}'`;
    }
    if (tipo === 'AGREGAR_OPERACION') {
      return `Agregar operación '${op.operacion?.nombre}(): ${op.operacion?.tipoRetorno}' en clase '${op.clase}'`;
    }
    if (tipo === 'CREAR_RELACION') {
      const r = op.relacion || {};
      return `Crear relación ${r.tipo || 'ASOCIACION'} entre '${r.origen}' (${r.multiplicidadOrigen || '1'}) y '${r.destino}' (${r.multiplicidadDestino || '1'})`;
    }
    if (tipo === 'MODIFICAR_MULTIPLICIDAD') {
      return `Ajustar multiplicidad entre '${op.origen}' y '${op.destino}' a ${op.multiplicidadDestino || '1'}`;
    }
    if (tipo === 'ELIMINAR_CLASE') {
      return `Eliminar clase '${op.nombre}'`;
    }
    if (tipo === 'ELIMINAR_RELACION') {
      return `Eliminar relación entre '${op.origen}' y '${op.destino}'`;
    }
    return JSON.stringify(op);
  }

  get propuestasFiltradas(): IaPropuesta[] {
    if (this.filtroEstadoPropuestas === 'TODAS') return this.propuestasColaborativas;
    return this.propuestasColaborativas.filter(p => p.estado === this.filtroEstadoPropuestas);
  }

  get propuestasPendientes(): IaPropuesta[] {
    return this.propuestasColaborativas.filter(p => p.estado === 'PENDIENTE');
  }

  private scrollChatAlFondo(): void {
    setTimeout(() => {
      const container = document.getElementById('contenedorChatColaborativo');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 60);
  }

  private mostrarAlertaColaborativa(mensaje: string, tipo: 'success' | 'error' | 'info' = 'info'): void {
    this.notificacionColaborativa = mensaje;
    this.tipoNotificacionColaborativa = tipo;
    setTimeout(() => {
      if (this.notificacionColaborativa === mensaje) {
        this.notificacionColaborativa = null;
        this.changeDetector.detectChanges();
      }
    }, 6000);
    this.changeDetector.detectChanges();
  }

  // =========================================================
  // GENERAR UML DESDE IMAGEN CON IA
  // =========================================================

  iaProcesando = false;
  iaArchivoSeleccionado: File | null = null;

  // =========================================================
  // GENERAR UML DESDE AUDIO / VOZ
  // =========================================================

  audioGrabando = false;
  audioProcesando = false;
  audioSegundosGrabacion = 0;

  private mediaRecorder: MediaRecorder | null = null;
  private audioStream: MediaStream | null = null;
  private audioChunks: Blob[] = [];
  private audioMimeType = 'audio/webm';
  private audioTimer: ReturnType<typeof setInterval> | null = null;

  private readonly endpointsAudioUml = [
    'http://localhost:8080/api/ia/uml/audio',
    'http://localhost:8080/api/ia/uml/voz'
  ];

  generarUmlDesdeImagen(event: Event): void {
    if (!this.modeloActual) {
      alert('No hay un modelo UML cargado.');
      return;
    }

    if (this.iaProcesando) {
      return;
    }

    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];

    if (!archivo) {
      return;
    }

    const tiposPermitidos = [
      'image/png',
      'image/jpeg',
      'image/jpg',
      'image/webp'
    ];

    if (!tiposPermitidos.includes(archivo.type)) {
      alert('Selecciona una imagen PNG, JPG, JPEG o WEBP.');
      input.value = '';
      return;
    }

    this.iaArchivoSeleccionado = archivo;
    this.iaProcesando = true;
    this.changeDetector.detectChanges();

    const formData = new FormData();
    formData.append('imagen', archivo);

    console.log(
      'CODEFORGE IA: enviando imagen al backend:',
      archivo.name
    );

    this.http
      .post<ContenidoUML>(
        'http://localhost:8080/api/ia/uml/imagen',
        formData
      )
      .subscribe({
        next: (contenido) => {
          console.log(
            'CODEFORGE IA: contenido UML recibido:',
            contenido
          );

          const contenidoNormalizado =
            this.normalizarContenidoGeneradoPorIA(contenido);

          const elementos = contenidoNormalizado.elementos ?? [];
          const relaciones = contenidoNormalizado.relaciones ?? [];

          if (elementos.length === 0) {
            this.iaProcesando = false;
            this.iaArchivoSeleccionado = null;
            this.changeDetector.detectChanges();
            alert(
              'La IA no detectó clases o elementos UML en la imagen.'
            );
            input.value = '';
            return;
          }

          const confirmar = confirm(
            `La IA detectó ${elementos.length} elemento(s) UML y ${relaciones.length} relación(es).\n\n` +
            'Se agregarán al modelo actual sin eliminar lo que ya tienes.\n\n' +
            '¿Deseas agregarlos al lienzo?'
          );

          if (!confirmar) {
            this.iaProcesando = false;
            this.iaArchivoSeleccionado = null;
            this.changeDetector.detectChanges();
            input.value = '';
            return;
          }

          this.aplicarContenidoIaAlModelo(
            contenidoNormalizado
          );

          input.value = '';
        },
        error: (error) => {
          console.error(
            'CODEFORGE IA: error generando UML:',
            error
          );

          this.iaProcesando = false;
          this.iaArchivoSeleccionado = null;
          this.changeDetector.detectChanges();

          if (error.status === 401) {
            alert('Tu sesión expiró. Inicia sesión nuevamente.');
          } else if (error.status === 403) {
            alert('No tienes permiso para utilizar la IA.');
          } else if (error.status === 413) {
            alert('La imagen es demasiado grande para procesarla.');
          } else {
            alert(
              error.error?.message ??
              error.error?.error ??
              'No se pudo generar el UML mediante IA.'
            );
          }

          input.value = '';
        }
      });
  }

  // =========================================================
  // CREAR UML DESDE AUDIO / VOZ
  // =========================================================

  async iniciarGrabacionUmlPorAudio(): Promise<void> {
    if (!this.modeloActual) {
      alert('No hay un modelo UML cargado.');
      return;
    }

    if (this.iaProcesando || this.audioProcesando || this.audioGrabando) {
      return;
    }

    if (!navigator.mediaDevices?.getUserMedia) {
      alert('Tu navegador no permite acceder al micrófono desde esta página.');
      return;
    }

    try {
      this.audioStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true
        }
      });

      this.audioChunks = [];
      this.audioSegundosGrabacion = 0;

      const tiposPreferidos = [
        'audio/webm;codecs=opus',
        'audio/webm',
        'audio/ogg;codecs=opus',
        'audio/mp4'
      ];

      this.audioMimeType =
        tiposPreferidos.find(tipo => MediaRecorder.isTypeSupported(tipo)) ||
        'audio/webm';

      this.mediaRecorder = new MediaRecorder(this.audioStream, {
        mimeType: this.audioMimeType
      });

      this.mediaRecorder.ondataavailable = (evento: BlobEvent) => {
        if (evento.data && evento.data.size > 0) {
          this.audioChunks.push(evento.data);
        }
      };

      this.mediaRecorder.onerror = () => {
        this.limpiarGrabacionAudio();
        alert('Ocurrió un error al grabar el audio.');
      };

      this.mediaRecorder.onstop = () => {
        void this.procesarAudioGrabado();
      };

      this.mediaRecorder.start(250);
      this.audioGrabando = true;
      this.iniciarContadorAudio();
      this.changeDetector.detectChanges();
    } catch (error) {
      console.error('CODEFORGE IA AUDIO: no se pudo abrir el micrófono:', error);
      this.limpiarGrabacionAudio();

      if (error instanceof DOMException && error.name === 'NotAllowedError') {
        alert('Debes permitir el acceso al micrófono para crear UML mediante voz.');
      } else {
        alert('No se pudo acceder al micrófono.');
      }
    }
  }

  detenerGrabacionUmlPorAudio(): void {
    if (!this.mediaRecorder || !this.audioGrabando) {
      return;
    }

    this.audioGrabando = false;
    this.detenerContadorAudio();

    if (this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
    }

    this.audioStream?.getTracks().forEach(track => track.stop());
    this.audioStream = null;
    this.changeDetector.detectChanges();
  }

  private iniciarContadorAudio(): void {
    this.detenerContadorAudio();

    this.audioTimer = setInterval(() => {
      this.audioSegundosGrabacion++;

      // Evitar grabaciones accidentalmente demasiado largas.
      if (this.audioSegundosGrabacion >= 120) {
        this.detenerGrabacionUmlPorAudio();
      }

      this.changeDetector.detectChanges();
    }, 1000);
  }

  private detenerContadorAudio(): void {
    if (this.audioTimer) {
      clearInterval(this.audioTimer);
      this.audioTimer = null;
    }
  }

  private async procesarAudioGrabado(): Promise<void> {
    const chunks = [...this.audioChunks];
    this.audioChunks = [];

    if (chunks.length === 0) {
      this.limpiarGrabacionAudio();
      return;
    }

    const blob = new Blob(chunks, {
      type: this.audioMimeType || 'audio/webm'
    });

    if (blob.size < 1000) {
      this.limpiarGrabacionAudio();
      alert('La grabación es demasiado corta. Habla durante unos segundos e inténtalo nuevamente.');
      return;
    }

    this.audioProcesando = true;
    this.changeDetector.detectChanges();

    const extension = this.obtenerExtensionAudio(this.audioMimeType);
    const formData = new FormData();
    formData.append(
      'audio',
      blob,
      `codeforge-voz-${Date.now()}.${extension}`
    );

    // Enviar contexto de las clases y relaciones actuales en el lienzo para que la IA reconozca nombres existentes
    if (this.elementos.length > 0 || this.relaciones.length > 0) {
      const contextoActual = {
        elementos: this.elementos.map(elem => ({
          nombre: elem.nombre,
          tipo: elem.tipo,
          atributos: (elem.atributos ?? []).map(a => ({ nombre: a.nombre, tipo: a.tipo })),
          operaciones: (elem.operaciones ?? []).map(o => ({ nombre: o.nombre, tipoRetorno: o.tipoRetorno }))
        })),
        relaciones: this.relaciones.map(rel => {
          const nomOrigen = this.elementos.find(e => e.id === rel.origen)?.nombre || rel.origen;
          const nomDestino = this.elementos.find(e => e.id === rel.destino)?.nombre || rel.destino;
          return {
            tipo: rel.tipo,
            origen: nomOrigen,
            destino: nomDestino,
            claseAsociacion: rel.rolDestino || null
          };
        })
      };
      formData.append('contexto', JSON.stringify(contextoActual));
    }

    console.log(
      'CODEFORGE IA AUDIO: enviando grabación al backend:',
      this.audioMimeType,
      `${Math.round(blob.size / 1024)} KB`
    );

    this.enviarAudioUmlAlBackend(formData, 0);
  }

  private enviarAudioUmlAlBackend(
    formData: FormData,
    indiceEndpoint: number
  ): void {
    const endpoint = this.endpointsAudioUml[indiceEndpoint];

    if (!endpoint) {
      this.audioProcesando = false;
      this.changeDetector.detectChanges();
      alert('No se encontró un endpoint de IA para procesar el audio.');
      return;
    }

    this.http
      .post<any>(endpoint, formData)
      .subscribe({
        next: (respuesta) => {
          console.log('CODEFORGE IA AUDIO: respuesta recibida:', respuesta);

          const contenidoBruto = this.extraerContenidoUmlDeRespuestaIA(respuesta);
          const contenidoNormalizado =
            this.normalizarContenidoGeneradoPorIA(contenidoBruto);

          const elementos = contenidoNormalizado.elementos ?? [];
          const relaciones = contenidoNormalizado.relaciones ?? [];
          const eliminaciones = contenidoNormalizado.eliminaciones;

          const numElementos = elementos.length;
          const numRelaciones = relaciones.length;
          const numClasesEliminar = eliminaciones?.clases?.length ?? 0;
          const numRelacionesEliminar = eliminaciones?.relaciones?.length ?? 0;
          const numAtributosEliminar = eliminaciones?.atributos?.length ?? 0;
          const numOperacionesEliminar = eliminaciones?.operaciones?.length ?? 0;

          const totalAcciones = numElementos + numRelaciones + numClasesEliminar +
            numRelacionesEliminar + numAtributosEliminar + numOperacionesEliminar;

          if (totalAcciones === 0) {
            this.audioProcesando = false;
            this.changeDetector.detectChanges();
            alert(
              'La IA no detectó elementos para crear ni para eliminar en la instrucción de voz.'
            );
            return;
          }

          const detalles: string[] = [];
          if (numElementos > 0) detalles.push(`• Crear ${numElementos} elemento(s) UML`);
          if (numRelaciones > 0) detalles.push(`• Crear ${numRelaciones} relación(es) UML`);
          if (numClasesEliminar > 0) detalles.push(`• Eliminar clase(s): ${eliminaciones!.clases!.join(', ')}`);
          if (numRelacionesEliminar > 0) {
            const relsDesc = eliminaciones!.relaciones!.map(r => `${r.origen} ── ${r.destino}`).join(', ');
            detalles.push(`• Eliminar relación(es): ${relsDesc}`);
          }
          if (numAtributosEliminar > 0) {
            const atrsDesc = eliminaciones!.atributos!.map(a => `${a.clase ? a.clase + '.' : ''}${a.nombre}`).join(', ');
            detalles.push(`• Eliminar atributo(s): ${atrsDesc}`);
          }
          if (numOperacionesEliminar > 0) {
            const opsDesc = eliminaciones!.operaciones!.map(o => `${o.clase ? o.clase + '.' : ''}${o.nombre}()`).join(', ');
            detalles.push(`• Eliminar método(s): ${opsDesc}`);
          }

          const confirmar = confirm(
            `La IA identificó las siguientes acciones a partir de tu voz:\n\n` +
            detalles.join('\n') +
            `\n\n¿Deseas aplicar estos cambios al modelo?`
          );

          if (!confirmar) {
            this.audioProcesando = false;
            this.changeDetector.detectChanges();
            return;
          }

          this.aplicarContenidoIaAlModelo(contenidoNormalizado, 'audio');
        },
        error: (error) => {
          console.error(
            'CODEFORGE IA AUDIO: error en endpoint:',
            endpoint,
            error
          );

          // Compatibilidad con ambos nombres de endpoint usados en CodeForge.
          if (
            (error.status === 404 || error.status === 405) &&
            indiceEndpoint + 1 < this.endpointsAudioUml.length
          ) {
            this.enviarAudioUmlAlBackend(formData, indiceEndpoint + 1);
            return;
          }

          this.audioProcesando = false;
          this.changeDetector.detectChanges();

          if (error.status === 401) {
            alert('Tu sesión expiró. Inicia sesión nuevamente.');
          } else if (error.status === 403) {
            alert('No tienes permiso para utilizar la IA por voz.');
          } else if (error.status === 413) {
            alert('La grabación es demasiado grande para procesarla.');
          } else {
            alert(
              error.error?.message ??
              error.error?.error ??
              'No se pudo generar el UML mediante voz.'
            );
          }
        }
      });
  }

  private extraerContenidoUmlDeRespuestaIA(respuesta: any): ContenidoUML {
    if (typeof respuesta === 'string') {
      try {
        let texto = respuesta.trim();
        if (texto.startsWith('```json')) {
          texto = texto.substring(7);
        } else if (texto.startsWith('```')) {
          texto = texto.substring(3);
        }
        if (texto.endsWith('```')) {
          texto = texto.substring(0, texto.length - 3);
        }
        respuesta = JSON.parse(texto.trim());
      } catch (e) {
        console.warn('CODEFORGE IA: error parseando respuesta como JSON:', e);
      }
    }

    if (respuesta && typeof respuesta === 'object') {
      if (respuesta.elementos || respuesta.relaciones || respuesta.eliminaciones) {
        return respuesta as ContenidoUML;
      }
      if (respuesta.contenidoJson) {
        return this.extraerContenidoUmlDeRespuestaIA(respuesta.contenidoJson);
      }
      if (respuesta.contenido) {
        return this.extraerContenidoUmlDeRespuestaIA(respuesta.contenido);
      }
      if (respuesta.uml) {
        return this.extraerContenidoUmlDeRespuestaIA(respuesta.uml);
      }
      if (respuesta.modelo?.contenidoJson) {
        return this.extraerContenidoUmlDeRespuestaIA(respuesta.modelo.contenidoJson);
      }
    }

    return {};
  }

  private obtenerExtensionAudio(mimeType: string): string {
    const mime = mimeType.toLowerCase();

    if (mime.includes('ogg')) {
      return 'ogg';
    }

    if (mime.includes('mp4')) {
      return 'mp4';
    }

    if (mime.includes('wav')) {
      return 'wav';
    }

    return 'webm';
  }

  private limpiarGrabacionAudio(): void {
    this.detenerContadorAudio();
    this.audioGrabando = false;
    this.audioStream?.getTracks().forEach(track => track.stop());
    this.audioStream = null;
    this.mediaRecorder = null;
    this.audioChunks = [];
    this.audioSegundosGrabacion = 0;
    this.changeDetector.detectChanges();
  }

  private normalizarContenidoGeneradoPorIA(
    contenido: ContenidoUML | null | undefined
  ): ContenidoUML {
    const elementosOriginales =
      Array.isArray(contenido?.elementos)
        ? contenido!.elementos!
        : [];

    const relacionesOriginales =
      Array.isArray(contenido?.relaciones)
        ? contenido!.relaciones!
        : [];

    const eliminacionesOriginales = contenido?.eliminaciones;
    const eliminaciones: EliminacionesUML = {
      clases: Array.isArray(eliminacionesOriginales?.clases)
        ? eliminacionesOriginales!.clases!
            .map(c => {
              if (typeof c === 'string') return c.trim();
              if (c && typeof c === 'object') return String((c as any).nombre ?? (c as any).clase ?? (c as any).id ?? '').trim();
              return String(c ?? '').trim();
            })
            .filter(c => !!c)
        : [],
      relaciones: Array.isArray(eliminacionesOriginales?.relaciones)
        ? eliminacionesOriginales!.relaciones!
            .map(r => {
              const orig = String(r?.origen ?? (r as any)?.claseOrigen ?? (r as any)?.from ?? (r as any)?.source ?? '').trim();
              const dest = String(r?.destino ?? (r as any)?.claseDestino ?? (r as any)?.to ?? (r as any)?.target ?? '').trim();
              const tipo = r?.tipo ? String(r.tipo).trim() : null;
              return { origen: orig, destino: dest, tipo };
            })
            .filter(r => !!r.origen && !!r.destino)
        : [],
      atributos: Array.isArray(eliminacionesOriginales?.atributos)
        ? eliminacionesOriginales!.atributos!
            .map(a => {
              const clase = String(a?.clase ?? (a as any)?.elemento ?? (a as any)?.className ?? '').trim();
              const nombre = String(a?.nombre ?? (a as any)?.atributo ?? (a as any)?.name ?? '').trim();
              return { clase, nombre };
            })
            .filter(a => !!a.nombre)
        : [],
      operaciones: Array.isArray(eliminacionesOriginales?.operaciones)
        ? eliminacionesOriginales!.operaciones!
            .map(o => {
              const clase = String(o?.clase ?? (o as any)?.elemento ?? (o as any)?.className ?? '').trim();
              const nombre = String(o?.nombre ?? (o as any)?.operacion ?? (o as any)?.metodo ?? (o as any)?.name ?? '').trim();
              return { clase, nombre };
            })
            .filter(o => !!o.nombre)
        : []
    };

    const elementos = elementosOriginales
      .filter(elemento => !!elemento && !!String(elemento.nombre ?? '').trim())
      .map((elemento, index) => ({
        ...elemento,
        id: elemento.id?.trim() || this.generarIdTemporalIA(`elemento-${index}`),
        tipo: this.normalizarTipoElementoIA(elemento.tipo),
        nombre: String(elemento.nombre).trim(),
        visibilidad: elemento.visibilidad || 'PUBLIC',
        abstracto: elemento.abstracto ?? false,
        finalElemento: elemento.finalElemento ?? false,
        posicion: elemento.posicion ?? {
          x: 80 + (index % 3) * 320,
          y: 80 + Math.floor(index / 3) * 260
        },
        tamaño: elemento.tamaño ?? {
          ancho: 260,
          alto: 180
        },
        atributos: (elemento.atributos ?? []).map((atributo, atributoIndex) => ({
          ...atributo,
          id: atributo.id?.trim() || this.generarIdTemporalIA(`atributo-${index}-${atributoIndex}`),
          nombre: String(atributo.nombre ?? '').trim(),
          tipo: String(atributo.tipo ?? 'String').trim() || 'String',
          visibilidad: atributo.visibilidad || 'PRIVATE',
          multiplicidad: atributo.multiplicidad || '1',
          derivado: atributo.derivado ?? false,
          estatico: atributo.estatico ?? false,
          valorInicial: atributo.valorInicial ?? null
        })).filter(atributo => !!atributo.nombre),
        operaciones: (elemento.operaciones ?? []).map((operacion, operacionIndex) => ({
          ...operacion,
          id: operacion.id?.trim() || this.generarIdTemporalIA(`operacion-${index}-${operacionIndex}`),
          nombre: String(operacion.nombre ?? '').trim(),
          tipoRetorno: String(operacion.tipoRetorno ?? 'void').trim() || 'void',
          visibilidad: operacion.visibilidad || 'PUBLIC',
          estatica: operacion.estatica ?? false,
          abstracta: operacion.abstracta ?? false,
          parametros: (operacion.parametros ?? []).map((parametro, parametroIndex) => ({
            ...parametro,
            id: parametro.id?.trim() || this.generarIdTemporalIA(`parametro-${index}-${operacionIndex}-${parametroIndex}`),
            nombre: String(parametro.nombre ?? '').trim(),
            tipo: String(parametro.tipo ?? 'String').trim() || 'String',
            direccion: parametro.direccion || 'IN',
            valorInicial: parametro.valorInicial ?? null
          })).filter(parametro => !!parametro.nombre)
        })).filter(operacion => !!operacion.nombre)
      }));

    const relaciones = relacionesOriginales
      .filter(relacion => {
        if (!relacion || !relacion.origen || !relacion.destino) return false;
        return true;
      })
      .map((relacion, index) => {
        let origen = String(relacion.origen).trim();
        let destino = String(relacion.destino).trim();

        const elemO = elementos.find(
          e => e.id === origen || e.nombre?.trim().toLowerCase() === origen.toLowerCase()
        );
        if (elemO) origen = elemO.id;

        const elemD = elementos.find(
          e => e.id === destino || e.nombre?.trim().toLowerCase() === destino.toLowerCase()
        );
        if (elemD) destino = elemD.id;

        const esMismo = origen === destino;
        let tipo = this.normalizarTipoRelacionIA(relacion.tipo);
        if (esMismo) {
          tipo = 'RECURSIVA';
        }

        return {
          ...relacion,
          id: relacion.id?.trim() || this.generarIdTemporalIA(`relacion-${index}`),
          tipo,
          origen,
          destino,
          rolOrigen: relacion.rolOrigen ?? null,
          rolDestino: relacion.rolDestino ?? null,
          multiplicidadOrigen: relacion.multiplicidadOrigen ?? (esMismo ? '1' : null),
          multiplicidadDestino: relacion.multiplicidadDestino ?? (esMismo ? '0..*' : null)
        };
      });

    return {
      versionUML: contenido?.versionUML || '2.5',
      elementos,
      relaciones,
      eliminaciones
    };
  }

  private normalizarTipoElementoIA(tipo?: string): string {
    const tipoNormalizado = (tipo ?? 'CLASE').trim().toUpperCase();

    if (
      tipoNormalizado === 'CLASE' ||
      tipoNormalizado === 'INTERFACE' ||
      tipoNormalizado === 'ENUM'
    ) {
      return tipoNormalizado;
    }

    return 'CLASE';
  }

  private normalizarTipoRelacionIA(tipo?: string): string {
    const tipoNormalizado = (tipo ?? 'ASOCIACION').trim().toUpperCase();

    const tiposPermitidos = [
      'ASOCIACION',
      'GENERALIZACION',
      'REALIZACION',
      'AGREGACION',
      'COMPOSICION',
      'DEPENDENCIA',
      'CLASE_ASOCIACION',
      'ASOCIACION_CLASE',
      'RECURSIVA'
    ];

    return tiposPermitidos.includes(tipoNormalizado)
      ? tipoNormalizado
      : 'ASOCIACION';
  }

  private generarIdTemporalIA(prefijo: string): string {
    try {
      return `${prefijo}-${crypto.randomUUID()}`;
    } catch {
      return `${prefijo}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
    }
  }

  private aplicarContenidoIaAlModelo(
    contenido: ContenidoUML,
    origen: 'imagen' | 'audio' = 'imagen'
  ): void {
    if (!this.modeloActual) {
      this.iaProcesando = false;
      this.audioProcesando = false;
      return;
    }

    const idModelo = this.modeloActual.idModelo;
    const elementos = contenido.elementos ?? [];
    const relaciones = contenido.relaciones ?? [];
    const eliminaciones = contenido.eliminaciones;
    const mapaIds = new Map<string, string>();

    const tareasEliminacion = this.prepararTareasEliminacionIA(idModelo, eliminaciones);

    const ejecutarEliminaciones$ = tareasEliminacion.length > 0
      ? from(tareasEliminacion).pipe(
          concatMap(tarea => tarea()),
          toArray()
        )
      : of([]);

    const ejecutarElementos$ = elementos.length > 0
      ? from(elementos).pipe(
          concatMap(elemento => this.guardarElementoGeneradoPorIA(elemento, mapaIds)),
          toArray()
        )
      : of([]);

    const ejecutarRelaciones$ = relaciones.length > 0
      ? from(relaciones).pipe(
          concatMap(relacion => this.guardarRelacionGeneradaPorIA(relacion, mapaIds)),
          toArray()
        )
      : of([]);

    ejecutarEliminaciones$
      .pipe(
        concatMap(() => ejecutarElementos$),
        concatMap(() => ejecutarRelaciones$),
        finalize(() => {
          this.actualizarContenidoModeloLocal();
          this.iaProcesando = false;
          this.iaArchivoSeleccionado = null;
          this.audioProcesando = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        })
      )
      .subscribe({
        next: () => {
          console.log(
            'CODEFORGE IA: cambios aplicados correctamente al modelo.'
          );
          this.changeDetector.detectChanges();
        },
        error: (error) => {
          console.error(
            'CODEFORGE IA: error aplicando UML:',
            error
          );
          alert(
            error.error?.message ??
            'Ocurrió un error al aplicar las modificaciones al modelo.'
          );
        }
      });
  }

  private normalizarNombreParaBusqueda(texto?: string): string {
    if (!texto) return '';
    let t = String(texto).trim().toLowerCase();
    t = t.replace(/^la clase\s+/, '')
         .replace(/^el elemento\s+/, '')
         .replace(/^clase\s+/, '')
         .replace(/^elemento\s+/, '')
         .replace(/^el atributo\s+/, '')
         .replace(/^el campo\s+/, '')
         .replace(/^atributo\s+/, '')
         .replace(/^campo\s+/, '')
         .replace(/^el método\s+/, '')
         .replace(/^el metodo\s+/, '')
         .replace(/^la operación\s+/, '')
         .replace(/^la operacion\s+/, '')
         .replace(/^la función\s+/, '')
         .replace(/^la funcion\s+/, '')
         .replace(/^método\s+/, '')
         .replace(/^metodo\s+/, '')
         .replace(/^operación\s+/, '')
         .replace(/^operacion\s+/, '')
         .replace(/^función\s+/, '')
         .replace(/^funcion\s+/, '')
         .replace(/\(\)$/, '')
         .trim();
    return t;
  }

  private prepararTareasEliminacionIA(
    idModelo: number,
    eliminaciones?: EliminacionesUML
  ): Array<() => Observable<any>> {
    if (!eliminaciones) return [];

    const tareas: Array<() => Observable<any>> = [];

    // 1. Eliminar atributos indicados por voz
    for (const atr of (eliminaciones.atributos ?? [])) {
      tareas.push(() => {
        const nomAtr = this.normalizarNombreParaBusqueda(atr.nombre);
        const nomClase = this.normalizarNombreParaBusqueda(atr.clase);

        let elem = this.elementos.find(
          e => e.id === atr.clase ||
               e.nombre.trim().toLowerCase() === atr.clase.trim().toLowerCase() ||
               (nomClase && this.normalizarNombreParaBusqueda(e.nombre) === nomClase)
        );

        if (!elem) {
          elem = this.elementos.find(e =>
            (e.atributos ?? []).some(a => this.normalizarNombreParaBusqueda(a.nombre) === nomAtr)
          );
        }

        if (!elem || !elem.atributos) {
          console.warn('CODEFORGE IA: no se encontró clase para eliminar atributo:', atr);
          return of(null);
        }

        const atrEncontrado = elem.atributos.find(
          a => a.id === atr.nombre ||
               a.nombre.trim().toLowerCase() === atr.nombre.trim().toLowerCase() ||
               this.normalizarNombreParaBusqueda(a.nombre) === nomAtr
        );
        if (!atrEncontrado) {
          console.warn('CODEFORGE IA: no se encontró atributo en clase:', atr);
          return of(null);
        }

        const elemTarget = elem;
        return this.http
          .delete(`http://localhost:8080/api/modelos/${idModelo}/elementos/${elemTarget.id}/atributos/${atrEncontrado.id}`)
          .pipe(
            map(() => {
              elemTarget.atributos = (elemTarget.atributos ?? []).filter(a => a.id !== atrEncontrado.id);
              if (this.elementoSeleccionado?.id === elemTarget.id) {
                this.elementoSeleccionado = { ...elemTarget };
              }
              return null;
            }),
            catchError(err => {
              console.warn('CODEFORGE IA: error en delete de atributo:', atr, err);
              return of(null);
            })
          );
      });
    }

    // 2. Eliminar métodos u operaciones indicadas por voz
    for (const op of (eliminaciones.operaciones ?? [])) {
      tareas.push(() => {
        const nomOp = this.normalizarNombreParaBusqueda(op.nombre);
        const nomClase = this.normalizarNombreParaBusqueda(op.clase);

        let elem = this.elementos.find(
          e => e.id === op.clase ||
               e.nombre.trim().toLowerCase() === op.clase.trim().toLowerCase() ||
               (nomClase && this.normalizarNombreParaBusqueda(e.nombre) === nomClase)
        );

        if (!elem) {
          elem = this.elementos.find(e =>
            (e.operaciones ?? []).some(o => this.normalizarNombreParaBusqueda(o.nombre) === nomOp)
          );
        }

        if (!elem || !elem.operaciones) {
          console.warn('CODEFORGE IA: no se encontró clase para eliminar operación:', op);
          return of(null);
        }

        const opEncontrada = elem.operaciones.find(
          o => o.id === op.nombre ||
               o.nombre.trim().toLowerCase() === op.nombre.trim().toLowerCase() ||
               this.normalizarNombreParaBusqueda(o.nombre) === nomOp
        );
        if (!opEncontrada) {
          console.warn('CODEFORGE IA: no se encontró operación en clase:', op);
          return of(null);
        }

        const elemTarget = elem;
        return this.http
          .delete(`http://localhost:8080/api/modelos/${idModelo}/elementos/${elemTarget.id}/operaciones/${opEncontrada.id}`)
          .pipe(
            map(() => {
              elemTarget.operaciones = (elemTarget.operaciones ?? []).filter(o => o.id !== opEncontrada.id);
              if (this.elementoSeleccionado?.id === elemTarget.id) {
                this.elementoSeleccionado = { ...elemTarget };
              }
              return null;
            }),
            catchError(err => {
              console.warn('CODEFORGE IA: error en delete de operación:', op, err);
              return of(null);
            })
          );
      });
    }

    // 3. Eliminar relaciones indicadas por voz
    for (const rel of (eliminaciones.relaciones ?? [])) {
      tareas.push(() => {
        const nomO = this.normalizarNombreParaBusqueda(rel.origen);
        const nomD = this.normalizarNombreParaBusqueda(rel.destino);

        const elemOrigen = this.elementos.find(
          e => e.id === rel.origen ||
               e.nombre.trim().toLowerCase() === rel.origen.trim().toLowerCase() ||
               this.normalizarNombreParaBusqueda(e.nombre) === nomO
        );
        const elemDestino = this.elementos.find(
          e => e.id === rel.destino ||
               e.nombre.trim().toLowerCase() === rel.destino.trim().toLowerCase() ||
               this.normalizarNombreParaBusqueda(e.nombre) === nomD
        );

        let relEncontrada: RelacionUML | undefined;

        if (elemOrigen && elemDestino) {
          relEncontrada = this.relaciones.find(
            r =>
              ((r.origen === elemOrigen.id && r.destino === elemDestino.id) ||
               (r.origen === elemDestino.id && r.destino === elemOrigen.id)) &&
              (!rel.tipo || r.tipo.toUpperCase() === rel.tipo.toUpperCase())
          ) || this.relaciones.find(
            r =>
              (r.origen === elemOrigen.id && r.destino === elemDestino.id) ||
              (r.origen === elemDestino.id && r.destino === elemOrigen.id)
          );
        }

        if (!relEncontrada) {
          relEncontrada = this.relaciones.find(
            r =>
              r.id === rel.origen ||
              r.id === rel.destino ||
              (r.rolDestino && this.normalizarNombreParaBusqueda(r.rolDestino) === nomO) ||
              (r.rolDestino && this.normalizarNombreParaBusqueda(r.rolDestino) === nomD)
          );
        }

        if (!relEncontrada) {
          console.warn('CODEFORGE IA: no se encontró relación a eliminar:', rel);
          return of(null);
        }

        const idRel = relEncontrada.id;
        return this.http
          .delete(`http://localhost:8080/api/modelos/${idModelo}/relaciones/${idRel}`)
          .pipe(
            map(() => {
              this.relaciones = this.relaciones.filter(r => r.id !== idRel);
              if (this.relacionSeleccionada?.id === idRel) {
                this.relacionSeleccionada = null;
              }
              return null;
            }),
            catchError(err => {
              console.warn('CODEFORGE IA: error en delete de relación:', rel, err);
              return of(null);
            })
          );
      });
    }

    // 4. Eliminar clases / elementos indicados por voz
    for (const nombreClase of (eliminaciones.clases ?? [])) {
      tareas.push(() => {
        const nomLimpio = this.normalizarNombreParaBusqueda(nombreClase);
        const elem = this.elementos.find(
          e => e.id === nombreClase ||
               e.nombre.trim().toLowerCase() === nombreClase.trim().toLowerCase() ||
               this.normalizarNombreParaBusqueda(e.nombre) === nomLimpio
        );
        if (!elem) {
          console.warn('CODEFORGE IA: no se encontró clase a eliminar:', nombreClase);
          return of(null);
        }

        return this.http
          .delete(`http://localhost:8080/api/modelos/${idModelo}/elementos/${elem.id}`)
          .pipe(
            map(() => {
              this.elementos = this.elementos.filter(e => e.id !== elem.id);
              this.relaciones = this.relaciones.filter(
                r => r.origen !== elem.id && r.destino !== elem.id && r.rolDestino !== elem.nombre
              );
              if (this.elementoSeleccionado?.id === elem.id) {
                this.elementoSeleccionado = null;
              }
              return null;
            }),
            catchError(err => {
              console.warn('CODEFORGE IA: error en delete de clase:', nombreClase, err);
              return of(null);
            })
          );
      });
    }

    return tareas;
  }

  private guardarElementoGeneradoPorIA(
    elemento: ElementoUML,
    mapaIds: Map<string, string>
  ) {
    if (!this.modeloActual) {
      return of(null);
    }

    const idModelo = this.modeloActual.idModelo;

    const existente = this.elementos.find(
      e =>
        e.nombre.trim().toLowerCase() ===
        elemento.nombre.trim().toLowerCase()
    );

    if (existente) {
      mapaIds.set(elemento.id, existente.id);

      return this.guardarAtributosYOperacionesIA(
        elemento,
        existente.id
      );
    }

    const posicion = elemento.posicion ??
      this.obtenerNuevaPosicion();

    const tamaño = elemento.tamaño ?? {
      ancho: 260,
      alto: 180
    };

    const dto = {
      tipo: this.normalizarTipoElementoIA(elemento.tipo),
      nombre: elemento.nombre.trim(),
      visibilidad: elemento.visibilidad || 'PUBLIC',
      estereotipo: elemento.estereotipo ?? null,
      abstracto: elemento.abstracto ?? false,
      finalElemento: elemento.finalElemento ?? false,
      posicionX: posicion.x,
      posicionY: posicion.y,
      ancho: tamaño.ancho,
      alto: tamaño.alto
    };

    return this.http
      .post<ModeloUML>(
        `http://localhost:8080/api/modelos/${idModelo}/elementos`,
        dto
      )
      .pipe(
        concatMap(modelo => {
          this.modeloActual = modelo;
          this.elementos = modelo.contenidoJson?.elementos ?? [];
          this.relaciones = modelo.contenidoJson?.relaciones ?? [];

          const creado = this.elementos.find(
            e =>
              e.nombre.trim().toLowerCase() ===
              elemento.nombre.trim().toLowerCase()
          );

          if (!creado) {
            throw new Error(
              `No se pudo localizar el elemento creado: ${elemento.nombre}`
            );
          }

          mapaIds.set(elemento.id, creado.id);

          return this.guardarAtributosYOperacionesIA(
            elemento,
            creado.id
          );
        })
      );
  }

  private guardarAtributosYOperacionesIA(
    elemento: ElementoUML,
    idElemento: string
  ) {
    const atributos = elemento.atributos ?? [];
    const operaciones = elemento.operaciones ?? [];

    return from(atributos)
      .pipe(
        concatMap(atributo => {
          const actual = this.elementos
            .find(e => e.id === idElemento)
            ?.atributos
            ?.find(
              a =>
                a.nombre.trim().toLowerCase() ===
                atributo.nombre.trim().toLowerCase()
            );

          if (actual) {
            return of(null);
          }

          const dto = {
            nombre: atributo.nombre.trim(),
            tipo: atributo.tipo?.trim() || 'String',
            visibilidad: atributo.visibilidad || 'PRIVATE',
            multiplicidad: atributo.multiplicidad || '1',
            derivado: atributo.derivado ?? false,
            estatico: atributo.estatico ?? false,
            valorInicial: atributo.valorInicial ?? null
          };

          return this.http
            .post<AtributoUML>(
              `http://localhost:8080/api/modelos/${this.modeloActual!.idModelo}/elementos/${idElemento}/atributos`,
              dto
            )
            .pipe(
              map(atributoGuardado => {
                const elem = this.elementos.find(
                  e => e.id === idElemento
                );

                if (elem) {
                  elem.atributos = [
                    ...(elem.atributos ?? []),
                    atributoGuardado
                  ];
                }

                return atributoGuardado;
              })
            );
        }),
        toArray(),
        concatMap(() => from(operaciones)),
        concatMap(operacion =>
          this.guardarOperacionGeneradaPorIA(
            operacion,
            idElemento
          )
        ),
        toArray(),
        map(() => null)
      );
  }

  private guardarOperacionGeneradaPorIA(
    operacion: OperacionUML,
    idElemento: string
  ) {
    const elem = this.elementos.find(
      e => e.id === idElemento
    );

    const existente = elem?.operaciones?.find(
      o =>
        o.nombre.trim().toLowerCase() ===
        operacion.nombre.trim().toLowerCase()
    );

    if (existente) {
      return of(null);
    }

    const dto = {
      nombre: operacion.nombre.trim(),
      tipoRetorno: operacion.tipoRetorno?.trim() || 'void',
      visibilidad: operacion.visibilidad || 'PUBLIC',
      estatica: operacion.estatica ?? false,
      abstracta: operacion.abstracta ?? false
    };

    return this.http
      .post<OperacionUML>(
        `http://localhost:8080/api/modelos/${this.modeloActual!.idModelo}/elementos/${idElemento}/operaciones`,
        dto
      )
      .pipe(
        concatMap(operacionGuardada => {
          const parametros = operacion.parametros ?? [];

          return from(parametros)
            .pipe(
              concatMap(parametro =>
                this.http.post<ParametroUML>(
                  `http://localhost:8080/api/modelos/${this.modeloActual!.idModelo}/elementos/${idElemento}/operaciones/${operacionGuardada.id}/parametros`,
                  {
                    nombre: parametro.nombre.trim(),
                    tipo: parametro.tipo?.trim() || 'String',
                    direccion: parametro.direccion || 'IN',
                    valorInicial: parametro.valorInicial ?? null
                  }
                )
              ),
              toArray(),
              map(parametrosGuardados => {
                const elemActual = this.elementos.find(
                  e => e.id === idElemento
                );

                if (elemActual) {
                  elemActual.operaciones = [
                    ...(elemActual.operaciones ?? []),
                    {
                      ...operacionGuardada,
                      parametros: parametrosGuardados
                    }
                  ];
                }

                return operacionGuardada;
              })
            );
        })
      );
  }

  private guardarRelacionGeneradaPorIA(
    relacion: RelacionUML,
    mapaIds: Map<string, string>
  ) {
    if (!this.modeloActual) {
      return of(null);
    }

    const origen = this.resolverIdElementoIA(
      relacion.origen,
      mapaIds
    );
    const destino = this.resolverIdElementoIA(
      relacion.destino,
      mapaIds
    );

    const esMismo = origen === destino;
    const tipoNormalizado = this.normalizarTipoRelacionIA(relacion.tipo);
    const tipoFinal = esMismo ? 'RECURSIVA' : tipoNormalizado;

    if (!origen || !destino) {
      console.warn(
        'CODEFORGE IA: relación ignorada porque no se encontraron sus elementos:',
        relacion
      );
      return of(null);
    }

    const existe = this.relaciones.some(
      r =>
        r.tipo === tipoFinal &&
        r.origen === origen &&
        r.destino === destino
    );

    if (existe) {
      return of(null);
    }

    const dto = {
      tipo: tipoFinal,
      origen,
      destino,
      rolOrigen: relacion.rolOrigen?.trim() || null,
      rolDestino: relacion.rolDestino?.trim() || null,
      multiplicidadOrigen: relacion.multiplicidadOrigen || (esMismo ? '1' : null),
      multiplicidadDestino: relacion.multiplicidadDestino || (esMismo ? '0..*' : null)
    };

    return this.http
      .post<RelacionUML>(
        `http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/relaciones`,
        dto
      )
      .pipe(
        map(relacionGuardada => {
          this.relaciones = [
            ...this.relaciones,
            relacionGuardada
          ];
          return relacionGuardada;
        })
      );
  }

  private resolverIdElementoIA(
    referencia: string,
    mapaIds: Map<string, string>
  ): string | null {
    const referenciaNormalizada = String(
      referencia ?? ''
    ).trim();

    if (!referenciaNormalizada) {
      return null;
    }

    const idMapeado = mapaIds.get(
      referenciaNormalizada
    );

    if (idMapeado) {
      return idMapeado;
    }

    const elemento = this.elementos.find(
      e =>
        e.id === referenciaNormalizada ||
        e.nombre.trim().toLowerCase() ===
          referenciaNormalizada.toLowerCase()
    );

    return elemento?.id ?? null;
  }

  private actualizarContenidoModeloLocal(): void {
    if (!this.modeloActual) {
      return;
    }

    this.modeloActual = {
      ...this.modeloActual,
      contenidoJson: {
        versionUML:
          this.modeloActual.contenidoJson?.versionUML || '2.5',
        elementos: this.elementos,
        relaciones: this.relaciones
      }
    };
  }

  // =========================================================
  // GENERAR BACKEND
// =========================================================

generarBackend(): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  if (this.generandoBackend) {
    return;
  }

  const idModelo =
    this.modeloActual.idModelo;

  this.generandoBackend = true;

  this.http
    .post(
      `http://localhost:8080/api/generador/modelos/${idModelo}/backend`,
      null,
      {
        observe: 'response',
        responseType: 'blob'
      }
    )
    .pipe(
      finalize(() => {

        this.generandoBackend = false;

        this.changeDetector.detectChanges();
      })
    )
    .subscribe({

      next: (response) => {

        if (!response.body) {

          alert(
            'El servidor no devolvió el archivo ZIP.'
          );

          return;
        }

        const blob =
          response.body;

        const url =
          window.URL.createObjectURL(
            blob
          );

        const enlace =
          document.createElement(
            'a'
          );

        enlace.href =
          url;

        enlace.download =
          'codeforge-generated-backend.zip';

        document.body.appendChild(
          enlace
        );

        enlace.click();

        enlace.remove();

        window.URL.revokeObjectURL(
          url
        );

        console.log(
          'CODEFORGE: backend generado y descargado correctamente.'
        );
      },

      error: (error) => {

        console.error(
          'CODEFORGE: error generando backend:',
          error
        );

        alert(
          'No se pudo generar el backend.'
        );
      }

    });
}
  // =========================================================
// EXPORTAR JSON
// =========================================================

exportarJson(): void {

  if (!this.modeloActual) {
    alert('No hay un modelo UML cargado.');
    return;
  }

  const idModelo =
    this.modeloActual.idModelo;

  console.log(
    'INTERCAMBIO: exportando modelo',
    idModelo
  );

  this.intercambioService
    .exportarJson(idModelo)
    .subscribe({

      next: (response) => {

        if (!response.body) {
          alert(
            'El servidor no devolvió ningún archivo.'
          );
          return;
        }

        const blob =
          response.body;

        const url =
          window.URL.createObjectURL(blob);

        const enlace =
          document.createElement('a');

        enlace.href = url;

        enlace.download =
          `modelo-${idModelo}.json`;

        document.body.appendChild(enlace);

        enlace.click();

        document.body.removeChild(enlace);

        window.URL.revokeObjectURL(url);

        console.log(
          'INTERCAMBIO: JSON exportado correctamente'
        );
      },

      error: (error) => {

        console.error(
          'INTERCAMBIO: error exportando JSON',
          error
        );

        alert(
          'No se pudo exportar el modelo.'
        );
      }

    });
}
  @ViewChild('umlCanvas', { static: false })
  private umlCanvas?: ElementRef<HTMLDivElement>;

  // Datos principales
  proyecto: Proyecto | null = null;
  colaboradores: Colaborador[] = [];
  usuarioEncontrado: UsuarioBusqueda | null = null;
  modeloActual: ModeloUML | null = null;
  elementos: ElementoUML[] = [];
  relaciones: RelacionUML[] = [];
  elementoSeleccionado: ElementoUML | null = null;
  relacionSeleccionada: RelacionUML | null = null;

  // JointJS
  private graph?: dia.Graph;
  private paper?: dia.Paper;
  private resizeObserver?: ResizeObserver;
  zoomLevel = 1.0;

  // Panning
  private isPanning = false;
  private panStartX = 0;
  private panStartY = 0;
  private panOriginTx = 0;
  private panOriginTy = 0;
  private unbindGlobalListeners?: () => void;

  // Polling para colaboración en tiempo real
  private pollingTimer?: any;
  sincronizadoEnVivo = false;
  private sincTimeout?: any;

  // Cursores Colaborativos en Tiempo Real (WebSocket STOMP)
  idProyectoActual: number | null = null;
  miUsuarioId: number = 0;
  miNombreUsuario: string = '';
  miColor: string = '#3B82F6';
  cursoresRemotos = new Map<number, CursorRemoto>();
  private ultimoEnvioCursor = 0;
  private readonly throttleCursorMs = 35; // ~28 mensajes/segundo
  private canvasMouseMoveListener?: (evt: MouseEvent) => void;
  private canvasMouseLeaveListener?: () => void;
  private beforeUnloadListener?: () => void;

  get listaCursoresRemotos(): CursorRemoto[] {
    return Array.from(this.cursoresRemotos.values());
  }

  // Estados
  cargando = true;
  cargandoColaboradores = false;
  cargandoModelo = false;
  buscandoUsuario = false;
  enviandoInvitacion = false;
  guardandoPosicion = false;
  generandoBackend = false;
  guardandoElemento = false;
  guardandoAtributo = false;
  guardandoOperacion = false;
  guardandoRelacion = false;
  eliminando = false;

  error = '';
  errorColaboradores = '';
  errorModelo = '';
  mensajeColaborador = '';
  mensajeInvitacion = '';
  usernameBusqueda = '';

  // Modales
  mostrarColaboradores = false;
  mostrarInvitar = false;
  mostrarModalElemento = false;
  mostrarModalAtributo = false;
  mostrarModalOperacion = false;
  mostrarModalRelacion = false;

  mostrarModalEditarAtributo = false;
  mostrarModalEditarOperacion = false;

  // Modo conexión interactiva en canvas
  modoConexion = false;
  origenConexion: ElementoUML | null = null;

  // Formulario Nuevo Elemento
  nuevoElemento = {
    tipo: 'CLASE',
    nombre: '',
    visibilidad: 'PUBLIC',
    estereotipo: '',
    abstracto: false
  };

  // Formulario Nuevo / Editar Atributo
  nuevoAtributo = {
    nombre: '',
    tipo: 'String',
    visibilidad: 'PRIVATE',
    multiplicidad: '1',
    valorInicial: '',
    derivado: false,
    estatico: false
  };

  idAtributoEditando = '';
  atributoEditando = {
    nombre: '',
    tipo: 'String',
    visibilidad: 'PRIVATE',
    multiplicidad: '1',
    valorInicial: '',
    derivado: false,
    estatico: false
  };

  // Formulario Nueva / Editar Operación
  nuevaOperacion = {
    nombre: '',
    tipoRetorno: 'void',
    visibilidad: 'PUBLIC',
    estatica: false,
    abstracta: false,
    parametros: [] as ParametroUML[]
  };

  nuevoParametroOperacion = {
    nombre: '',
    tipo: 'String',
    direccion: 'IN',
    valorInicial: ''
  };

  idOperacionEditando = '';
  operacionEditando = {
    nombre: '',
    tipoRetorno: 'void',
    visibilidad: 'PUBLIC',
    estatica: false,
    abstracta: false,
    parametros: [] as ParametroUML[]
  };

  guardandoParametroOperacion = false;

  // Formulario Nueva Relación
  nuevaRelacion = {
    tipo: 'ASOCIACION',
    origen: '',
    destino: '',
    rolOrigen: '',
    rolDestino: '',
    multiplicidadOrigen: '1',
    multiplicidadDestino: '1'
  };

  // =========================================================
  // CICLO DE VIDA
  // =========================================================

  ngOnInit(): void {
    const idProyecto = Number(this.route.snapshot.paramMap.get('idProyecto'));

    if (!idProyecto || Number.isNaN(idProyecto)) {
      this.error = 'El identificador del proyecto no es válido';
      this.cargando = false;
      this.changeDetector.detectChanges();
      return;
    }

    const usuarioActual = this.auth.obtenerUsuario();
    if (usuarioActual) {
      this.miUsuarioId = usuarioActual.idUsuario ?? 0;
      this.miNombreUsuario = (usuarioActual.nombre ? `${usuarioActual.nombre} ${usuarioActual.apellido || ''}` : usuarioActual.username || `Usuario ${this.miUsuarioId}`).trim();
      this.miColor = this.colaboracionService.obtenerColorUsuario(this.miUsuarioId);
    }
    this.idProyectoActual = idProyecto;

    this.cargarProyecto(idProyecto);
    this.cargarColaboradores(idProyecto);
    this.cargarModeloUML(idProyecto);

    // Iniciar Auto-actualización / Polling para Colaboradores en Tiempo Real
    this.iniciarPollingEnVivo(idProyecto);

    // Iniciar WebSocket STOMP para Cursores Colaborativos en Tiempo Real
    this.iniciarWebSocketColaboracion(idProyecto);

    // Iniciar datos y WebSocket de IA Colaborativa de Equipo
    this.cargarConversacionColaborativa(idProyecto);
    this.cargarPropuestasColaborativas(idProyecto);
    this.cargarDiagnosticoUml();
    this.iniciarWebSocketIaColaborativa(idProyecto);

    // Cargar modo de aprobación de IA preferido
    try {
      const modoGuardado = localStorage.getItem('codeforge_modo_aprobacion_ia');
      if (modoGuardado === 'AUTOMATICO' || modoGuardado === 'MANUAL') {
        this.modoAprobacionIa = modoGuardado;
        if (this.modoAprobacionIa === 'AUTOMATICO') {
          this.generarPropuestaCheck = true;
        }
      }
    } catch {}

    // Iniciar detector de inactividad de 1 minuto para el Asistente IA
    this.iniciarDetectorInactividad();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.inicializarJointJS();

      if (this.umlCanvas?.nativeElement) {
        this.resizeObserver = new ResizeObserver(() => {
          this.ajustarTamañoPaper();
        });
        this.resizeObserver.observe(this.umlCanvas.nativeElement);
      }
    }, 50);

    // Escuchadores globales para Panning (Arrastrar fondo del lienzo)
    const onMouseMove = (evt: MouseEvent) => {
      if (!this.isPanning || !this.paper) return;
      const dx = evt.clientX - this.panStartX;
      const dy = evt.clientY - this.panStartY;
      this.paper.translate(this.panOriginTx + dx, this.panOriginTy + dy);
    };

    const onMouseUp = () => {
      this.isPanning = false;
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);

    // Escuchar movimiento del mouse sobre el canvas para enviar cursor
    this.canvasMouseMoveListener = (evt: MouseEvent) => {
      this.capturarYEnviarCursor(evt);
    };

    this.canvasMouseLeaveListener = () => {
      // Opcional: cuando el mouse sale del canvas
    };

    const canvasElem = this.umlCanvas?.nativeElement;
    if (canvasElem) {
      canvasElem.addEventListener('mousemove', this.canvasMouseMoveListener);
      canvasElem.addEventListener('mouseleave', this.canvasMouseLeaveListener);
    }

    this.beforeUnloadListener = () => {
      if (this.idProyectoActual && this.miUsuarioId) {
        this.colaboracionService.enviarSalida(this.idProyectoActual, this.miUsuarioId);
      }
    };
    window.addEventListener('beforeunload', this.beforeUnloadListener);

    const onDocClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (this.menuAprobacionAbierto && !target.closest('.colab-approval-dropdown-container')) {
        this.menuAprobacionAbierto = false;
        this.changeDetector.detectChanges();
      }
      if (this.propuestaRevertirSeleccionada && !target.closest('.rollback-container')) {
        this.propuestaRevertirSeleccionada = null;
        this.changeDetector.detectChanges();
      }
    };
    window.addEventListener('click', onDocClick);

    this.unbindGlobalListeners = () => {
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
      window.removeEventListener('click', onDocClick);
    };
  }

  ngOnDestroy(): void {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
    this.unbindIdleListeners?.();
    if (this.idleTimer) {
      clearTimeout(this.idleTimer);
      this.idleTimer = null;
    }
    this.detenerContadorAudio();
    this.mediaRecorder?.stop();
    this.audioStream?.getTracks().forEach(track => track.stop());
    clearTimeout(this.sincTimeout);
    this.resizeObserver?.disconnect();
    this.unbindGlobalListeners?.();
    this.paper?.remove();

    // Desconectar WebSocket y notificar salida
    if (this.idProyectoActual && this.miUsuarioId) {
      this.colaboracionService.enviarSalida(this.idProyectoActual, this.miUsuarioId);
    }
    this.colaboracionService.desconectar();

    const canvasElem = this.umlCanvas?.nativeElement;
    if (canvasElem && this.canvasMouseMoveListener) {
      canvasElem.removeEventListener('mousemove', this.canvasMouseMoveListener);
    }
    if (canvasElem && this.canvasMouseLeaveListener) {
      canvasElem.removeEventListener('mouseleave', this.canvasMouseLeaveListener);
    }
    if (this.beforeUnloadListener) {
      window.removeEventListener('beforeunload', this.beforeUnloadListener);
    }
  }

  // =========================================================
  // CURSORES EN TIEMPO REAL (WEBSOCKET / STOMP)
  // =========================================================

  private iniciarWebSocketColaboracion(idProyecto: number): void {
    this.colaboracionService.conectar(idProyecto, (mensaje: CursorMessage) => {
      this.procesarCursorRemoto(mensaje);
    });
  }

  private capturarYEnviarCursor(evt: MouseEvent): void {
    const ahora = performance.now();
    if (ahora - this.ultimoEnvioCursor < this.throttleCursorMs) {
      return;
    }
    this.ultimoEnvioCursor = ahora;

    if (!this.umlCanvas?.nativeElement || !this.idProyectoActual) {
      return;
    }

    const rect = this.umlCanvas.nativeElement.getBoundingClientRect();
    const x = Math.round(evt.clientX - rect.left);
    const y = Math.round(evt.clientY - rect.top);

    if (x >= 0 && y >= 0 && x <= rect.width && y <= rect.height) {
      this.colaboracionService.enviarCursor({
        proyectoId: this.idProyectoActual,
        usuarioId: this.miUsuarioId,
        x,
        y,
        color: this.miColor,
        nombre: this.miNombreUsuario,
        tipo: 'MOVE'
      });
    }
  }

  private procesarCursorRemoto(mensaje: CursorMessage): void {
    if (!mensaje || !mensaje.usuarioId || mensaje.usuarioId === this.miUsuarioId) {
      // Ignoramos nuestro propio cursor
      return;
    }

    if (mensaje.tipo === 'LEAVE') {
      this.cursoresRemotos.delete(mensaje.usuarioId);
      this.changeDetector.detectChanges();
      return;
    }

    const colaborador = this.colaboradores.find(c => c.idUsuario === mensaje.usuarioId);
    const nombre = mensaje.nombre || (colaborador ? `${colaborador.nombre} ${colaborador.apellido || ''}`.trim() : `Usuario ${mensaje.usuarioId}`);
    const color = mensaje.color || this.colaboracionService.obtenerColorUsuario(mensaje.usuarioId);

    this.cursoresRemotos.set(mensaje.usuarioId, {
      usuarioId: mensaje.usuarioId,
      x: mensaje.x,
      y: mensaje.y,
      color,
      nombre,
      ultimoMovimiento: Date.now()
    });

    this.changeDetector.detectChanges();
  }

  // =========================================================
  // COLABORACIÓN EN TIEMPO REAL (AUTO-ACTUALIZACIÓN)
  // =========================================================

  private iniciarPollingEnVivo(idProyecto: number): void {
    this.pollingTimer = setInterval(() => {
      this.sincronizarEnVivo(idProyecto);
    }, 2500);
  }

  private sincronizarEnVivo(idProyecto: number): void {
    // Si el usuario está interactuando con modales o guardando, no interrumpir
    if (
      this.guardandoElemento ||
      this.guardandoAtributo ||
      this.guardandoOperacion ||
      this.guardandoRelacion ||
      this.guardandoPosicion ||
      this.eliminando ||
      this.mostrarModalElemento ||
      this.mostrarModalAtributo ||
      this.mostrarModalOperacion ||
      this.mostrarModalRelacion ||
      this.mostrarModalEditarAtributo ||
      this.mostrarModalEditarOperacion ||
      this.iaProcesando ||
      this.audioGrabando ||
      this.audioProcesando ||
      this.asistenteAbierto ||
      this.isPanning
    ) {
      return;
    }

    this.http
      .get<ModeloUML[]>(`http://localhost:8080/api/modelos/proyecto/${idProyecto}`)
      .subscribe({
        next: (modelos) => {
          if (!modelos || modelos.length === 0) return;
          const nuevoModelo = modelos[0];

          // Comprobar si hubo cambios respecto al modelo actual
          const jsonActual = JSON.stringify(this.modeloActual?.contenidoJson ?? {});
          const jsonNuevo = JSON.stringify(nuevoModelo.contenidoJson ?? {});

          if (jsonActual !== jsonNuevo) {
            this.modeloActual = nuevoModelo;
            this.elementos = nuevoModelo.contenidoJson?.elementos ?? [];
            this.relaciones = nuevoModelo.contenidoJson?.relaciones ?? [];

            // Mantener selección si el elemento aún existe
            if (this.elementoSeleccionado) {
              const actual = this.elementos.find(e => e.id === this.elementoSeleccionado!.id);
              this.elementoSeleccionado = actual ?? null;
            }

            if (this.relacionSeleccionada) {
              const actual = this.relaciones.find(r => r.id === this.relacionSeleccionada!.id);
              this.relacionSeleccionada = actual ?? null;
            }

            this.sincronizarConJointJS();
            this.notificarSincronizacion();
            this.changeDetector.detectChanges();
          }
        },
        error: () => {
          // Fallo silencioso en polling de fondo
        }
      });
  }

  private notificarSincronizacion(): void {
    this.sincronizadoEnVivo = true;
    clearTimeout(this.sincTimeout);
    this.sincTimeout = setTimeout(() => {
      this.sincronizadoEnVivo = false;
      this.changeDetector.detectChanges();
    }, 2000);
  }

  // =========================================================
  // CARGA DE DATOS DESDE BACKEND
  // =========================================================

  private cargarProyecto(idProyecto: number): void {
    this.cargando = true;
    this.error = '';

    this.proyectoService
      .obtenerProyecto(idProyecto)
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (proyecto: Proyecto) => {
          this.proyecto = proyecto;
          this.changeDetector.detectChanges();
          this.garantizarPaperYSincronizar();
        },
        error: (error: any) => {
          console.error('EDITOR: error cargando proyecto:', error);
          if (error.status === 401) {
            this.error = 'Tu sesión expiró. Inicia sesión de nuevo.';
          } else if (error.status === 403) {
            this.error = 'No tienes permiso para ver este proyecto.';
          } else if (error.status === 404) {
            this.error = 'El proyecto no existe o fue eliminado.';
          } else {
            this.error = 'Ocurrió un error al cargar el proyecto.';
          }
        }
      });
  }

  private cargarColaboradores(idProyecto: number): void {
    this.cargandoColaboradores = true;
    this.errorColaboradores = '';

    this.proyectoService
      .obtenerColaboradores(idProyecto)
      .pipe(
        finalize(() => {
          this.cargandoColaboradores = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (colaboradores) => {
          this.colaboradores = colaboradores;
          this.changeDetector.detectChanges();
        },
        error: (error) => {
          console.error('EDITOR: error cargando colaboradores:', error);
          this.errorColaboradores = 'No se pudieron cargar los colaboradores.';
        }
      });
  }

  private cargarModeloUML(idProyecto: number): void {
    this.cargandoModelo = true;
    this.errorModelo = '';

    this.http
      .get<ModeloUML[]>(`http://localhost:8080/api/modelos/proyecto/${idProyecto}`)
      .pipe(
        finalize(() => {
          this.cargandoModelo = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (modelos) => {
          if (modelos && modelos.length > 0) {
            this.modeloActual = modelos[0];
            this.elementos = this.modeloActual.contenidoJson?.elementos ?? [];
            this.relaciones = this.modeloActual.contenidoJson?.relaciones ?? [];
            this.changeDetector.detectChanges();
            this.garantizarPaperYSincronizar();
          } else {
            this.crearModeloPorDefecto(idProyecto);
          }
        },
        error: (error) => {
          console.error('EDITOR UML: error cargando modelos:', error);
          this.errorModelo = 'No se pudo cargar el modelo UML.';
        }
      });
  }

  private crearModeloPorDefecto(idProyecto: number): void {
    const body = {
      idProyecto,
      nombre: 'Diagrama de Clases Principal',
      descripcion: 'Modelo UML generado automáticamente',
      version: '1.0'
    };

    this.http
      .post<ModeloUML>('http://localhost:8080/api/modelos', body)
      .subscribe({
        next: (modelo) => {
          this.modeloActual = modelo;
          this.elementos = modelo.contenidoJson?.elementos ?? [];
          this.relaciones = modelo.contenidoJson?.relaciones ?? [];
          this.changeDetector.detectChanges();
          this.garantizarPaperYSincronizar();
        },
        error: (error) => {
          console.error('EDITOR UML: error creando modelo inicial:', error);
          this.errorModelo = 'No se pudo inicializar el modelo UML.';
        }
      });
  }

  private garantizarPaperYSincronizar(): void {
    setTimeout(() => {
      if (!this.paper) {
        this.inicializarJointJS();
      } else {
        this.sincronizarConJointJS();
        setTimeout(() => this.centrarDiagrama(), 100);
      }
    }, 50);
  }

  // =========================================================
  // JOINTJS: RENDERIZADO Y EVENTOS
  // =========================================================

  private inicializarJointJS(): void {
    if (!this.umlCanvas?.nativeElement) {
      return;
    }

    const contenedor = this.umlCanvas.nativeElement;
    const ancho = contenedor.clientWidth || 1200;
    const alto = contenedor.clientHeight || 800;

    this.graph = new dia.Graph({}, { cellNamespace });

    this.paper = new dia.Paper({
      el: contenedor,
      model: this.graph,
      width: ancho,
      height: alto,
      gridSize: 10,
      drawGrid: {
        name: 'dot',
        args: {
          color: '#334155',
          scaleFactor: 2
        }
      },
      background: {
        color: '#090d16'
      },
      cellViewNamespace: cellNamespace,
      interactive: {
        linkMove: true,
        elementMove: true
      },
      snapLinks: { radius: 25 },
      linkPinning: false,
      markAvailable: true,
      defaultLink: () => new shapes.standard.Link({
        attrs: {
          line: {
            stroke: '#8b5cf6',
            strokeWidth: 2,
            strokeDasharray: '4 4',
            targetMarker: { type: 'path', d: 'M 10 -5 0 0 10 5 Z', fill: '#8b5cf6' }
          }
        }
      }),
      validateConnection: (cellViewS, magnetS, cellViewT) => {
        return cellViewT.model.isElement();
      }
    });

    // EVENTO STARUML: Jalar relación interactiva arrastrando desde un elemento a otro (o a sí mismo)
    this.paper.on('link:connect', (linkView: dia.LinkView) => {
      const link = linkView.model;
      const source = link.source();
      const target = link.target();
      const sourceId = String(source.id);
      const targetId = String(target.id);

      link.remove();

      if (sourceId && targetId) {
        const esMismo = sourceId === targetId;
        this.nuevaRelacion = {
          tipo: esMismo ? 'RECURSIVA' : 'ASOCIACION',
          origen: sourceId,
          destino: targetId,
          rolOrigen: '',
          rolDestino: '',
          multiplicidadOrigen: '1',
          multiplicidadDestino: esMismo ? '0..*' : '1'
        };
        this.mostrarModalRelacion = true;
        this.changeDetector.detectChanges();
      }
    });

    // Evento: Zoom con rueda del mouse
    contenedor.addEventListener('wheel', (evt: WheelEvent) => {
      evt.preventDefault();
      const delta = evt.deltaY < 0 ? 0.1 : -0.1;
      this.aplicarZoom(delta);
    }, { passive: false });

    // Evento: Panning (Arrastrar el fondo del lienzo)
    this.paper.on('blank:pointerdown', (evt: any) => {
      this.isPanning = true;
      this.panStartX = evt.clientX ?? evt.originalEvent?.clientX ?? 0;
      this.panStartY = evt.clientY ?? evt.originalEvent?.clientY ?? 0;
      const current = this.paper!.translate();
      this.panOriginTx = current.tx;
      this.panOriginTy = current.ty;
    });

    // Evento: Click en elemento UML (incluyendo clase de asociación)
    this.paper.on('element:pointerclick', (elementView) => {
      const idElemento = String(elementView.model.id);

      if (idElemento.startsWith('assoc-class-')) {
        const idRel = idElemento.replace('assoc-class-', '');
        this.seleccionarRelacion(idRel);
        return;
      }

      if (this.modoConexion) {
        this.procesarClickConexion(idElemento);
      } else {
        this.seleccionarElemento(idElemento);
      }
    });

    // Evento: Click en enlace / relación
    this.paper.on('link:pointerclick', (linkView) => {
      const idRelacion = String(linkView.model.id);
      const idReal = idRelacion.startsWith('assoc-conn-') ? idRelacion.replace('assoc-conn-', '') : idRelacion;
      this.seleccionarRelacion(idReal);
    });

    // Evento: Click en el fondo del lienzo (deseleccionar)
    this.paper.on('blank:pointerclick', () => {
      if (this.modoConexion && this.origenConexion) {
        this.cancelarModoConexion();
      }
      this.elementoSeleccionado = null;
      this.relacionSeleccionada = null;
      this.changeDetector.detectChanges();
    });

    // Evento: Movimiento de elemento terminado (guardar posición)
    this.paper.on('element:pointerup', (elementView) => {
      this.guardarPosicionElemento(elementView.model as dia.Element);
    });

    this.sincronizarConJointJS();
    setTimeout(() => this.centrarDiagrama(), 120);
  }

  private ajustarTamañoPaper(): void {
    if (!this.paper || !this.umlCanvas?.nativeElement) {
      return;
    }
    const contenedor = this.umlCanvas.nativeElement;
    const ancho = contenedor.clientWidth || 1200;
    const alto = contenedor.clientHeight || 800;
    this.paper.setDimensions(ancho, alto);
  }

  private sincronizarConJointJS(): void {
    if (!this.graph) {
      return;
    }

    this.graph.clear();
    this.renderizarElementos();
    this.renderizarRelaciones();
  }

  private renderizarElementos(): void {
    if (!this.graph) {
      return;
    }

    const cells: dia.Element[] = [];

    this.elementos.forEach((elemento, index) => {
      const cell = this.crearCeldaUML(elemento, index);
      cells.push(cell);
    });

    if (cells.length > 0) {
      this.graph.addCells(cells);
    }
  }

  private crearCeldaUML(elemento: ElementoUML, index: number): dia.Element {
    const lineasAtributos = (elemento.atributos ?? [])
      .map((a) => {
        const vis = this.simboloVisibilidad(a.visibilidad);
        const mult = a.multiplicidad && a.multiplicidad !== '1' ? ` [${a.multiplicidad}]` : '';
        const init = a.valorInicial ? ` = ${a.valorInicial}` : '';
        const der = a.derivado ? '/' : '';
        return `${vis} ${der}${a.nombre}: ${a.tipo}${mult}${init}`;
      })
      .join('\n');

    const lineasOperaciones = (elemento.operaciones ?? [])
      .map((o) => {
        const vis = this.simboloVisibilidad(o.visibilidad);
        const params = (o.parametros ?? []).map(p => `${p.nombre}: ${p.tipo}`).join(', ');
        const ret = o.tipoRetorno ? `: ${o.tipoRetorno}` : ': void';
        return `${vis} ${o.nombre}(${params})${ret}`;
      })
      .join('\n');

    const totalAtributos = elemento.atributos?.length ?? 0;
    const totalOperaciones = elemento.operaciones?.length ?? 0;

    const alturaAtributos = Math.max(34, totalAtributos * 20 + 14);
    const alturaOperaciones = Math.max(34, totalOperaciones * 20 + 14);
    const alto = 38 + alturaAtributos + alturaOperaciones + 12;
    const ancho = Math.max(260, elemento.tamaño?.ancho ?? 260);

    // Colores según tipo
    let colorBorde = '#8b5cf6';
    let colorHeader = '#1e1b4b';
    let textoEstereotipo = '';

    if (elemento.tipo === 'INTERFACE') {
      colorBorde = '#38bdf8';
      colorHeader = '#082f49';
      textoEstereotipo = '«interface»';
    } else if (elemento.tipo === 'ENUM') {
      colorBorde = '#34d399';
      colorHeader = '#064e3b';
      textoEstereotipo = '«enumeration»';
    } else if (elemento.estereotipo === 'association') {
      colorBorde = '#06b6d4';
      colorHeader = '#164e63';
      textoEstereotipo = '«association»';
    } else if (elemento.estereotipo) {
      textoEstereotipo = `«${elemento.estereotipo}»`;
    }

    const posX = elemento.posicion?.x ?? (80 + (index % 3) * 320);
    const posY = elemento.posicion?.y ?? (80 + Math.floor(index / 3) * 260);

    const cell = new UMLClass({
      id: elemento.id,
      position: {
        x: posX,
        y: posY
      },
      size: {
        width: ancho,
        height: alto
      },
      attrs: {
        body: {
          stroke: colorBorde,
          fill: '#0f172a'
        },
        header: {
          fill: colorHeader
        },
        headerLine: {
          y1: 38,
          y2: 38
        },
        attributesLine: {
          y1: 38 + alturaAtributos,
          y2: 38 + alturaAtributos
        },
        className: {
          text: elemento.nombre,
          fontStyle: elemento.abstracto ? 'italic' : 'normal'
        },
        stereotype: {
          text: textoEstereotipo
        },
        connectHandle: {
          fill: colorBorde
        },
        attributesText: {
          text: lineasAtributos || '(sin atributos)',
          fill: lineasAtributos ? '#e2e8f0' : '#64748b',
          y: 48
        },
        operationsText: {
          text: lineasOperaciones || '(sin operaciones)',
          fill: lineasOperaciones ? '#cbd5e1' : '#64748b',
          y: 48 + alturaAtributos
        }
      }
    });

    return cell;
  }

  private renderizarRelaciones(): void {
    if (!this.graph) {
      return;
    }

    const links: dia.Link[] = [];
    const extraCells: dia.Element[] = [];

    for (const relacion of this.relaciones) {
      let origen = this.graph.getCell(relacion.origen);
      let destino = this.graph.getCell(relacion.destino);

      // Resiliencia: si origen o destino fueron referenciados por nombre en vez de UUID, resolver al ID de la celda
      if (!origen && relacion.origen) {
        const origenNombre = String(relacion.origen).trim().toLowerCase();
        const elemO = this.elementos.find(
          e => e.id === relacion.origen || e.nombre?.trim().toLowerCase() === origenNombre
        );
        if (elemO) {
          origen = this.graph.getCell(elemO.id);
          relacion.origen = elemO.id;
        }
      }

      if (!destino && relacion.destino) {
        const destinoNombre = String(relacion.destino).trim().toLowerCase();
        const elemD = this.elementos.find(
          e => e.id === relacion.destino || e.nombre?.trim().toLowerCase() === destinoNombre
        );
        if (elemD) {
          destino = this.graph.getCell(elemD.id);
          relacion.destino = elemD.id;
        }
      }

      if (!origen || !destino) {
        continue;
      }

      const esClaseAsociacion =
        (relacion.tipo ?? '').toUpperCase() === 'CLASE_ASOCIACION' ||
        (relacion.tipo ?? '').toUpperCase() === 'ASOCIACION_CLASE';

      const esRecursiva =
        relacion.origen === relacion.destino ||
        (relacion.tipo ?? '').toUpperCase() === 'RECURSIVA';

      let link: dia.Link;

      if (esRecursiva) {
        const elem = origen as dia.Element;
        const bbox = elem.getBBox();
        const loopOffset = 55;
        const topY = bbox.y + Math.min(28, bbox.height * 0.25);
        const bottomY = bbox.y + Math.max(bbox.height - 28, bbox.height * 0.75);
        const loopX = bbox.x + bbox.width + loopOffset;

        link = new shapes.standard.Link({
          id: relacion.id,
          source: {
            id: relacion.origen,
            anchor: {
              name: 'right',
              args: { dy: -Math.max(14, bbox.height * 0.25) }
            },
            connectionPoint: { name: 'boundary' }
          },
          target: {
            id: relacion.destino,
            anchor: {
              name: 'right',
              args: { dy: Math.max(14, bbox.height * 0.25) }
            },
            connectionPoint: { name: 'boundary' }
          },
          router: {
            name: 'orthogonal'
          },
          vertices: [
            { x: loopX, y: topY },
            { x: loopX, y: bottomY }
          ],
          attrs: {
            line: {
              stroke: this.colorRelacion(relacion.tipo || 'RECURSIVA'),
              strokeWidth: 1.75,
              strokeDasharray: this.dashRelacion(relacion.tipo),
              sourceMarker: this.sourceMarkerRelacion(relacion.tipo),
              targetMarker: this.targetMarkerRelacion(relacion.tipo || 'RECURSIVA')
            }
          }
        });

        // Al mover la clase, desplazar los vértices del lazo para que acompañen al elemento
        elem.on('change:position', () => {
          const currentBBox = elem.getBBox();
          const curTopY = currentBBox.y + Math.min(28, currentBBox.height * 0.25);
          const curBottomY = currentBBox.y + Math.max(currentBBox.height - 28, currentBBox.height * 0.75);
          const curLoopX = currentBBox.x + currentBBox.width + loopOffset;
          link.vertices([
            { x: curLoopX, y: curTopY },
            { x: curLoopX, y: curBottomY }
          ]);
        });
      } else {
        link = new shapes.standard.Link({
          id: relacion.id,
          source: { id: relacion.origen },
          target: { id: relacion.destino },
          attrs: {
            line: {
              stroke: this.colorRelacion(relacion.tipo),
              strokeWidth: 1.75,
              strokeDasharray: esClaseAsociacion ? '0' : this.dashRelacion(relacion.tipo),
              sourceMarker: this.sourceMarkerRelacion(relacion.tipo),
              targetMarker: this.targetMarkerRelacion(relacion.tipo)
            }
          }
        });
      }

      // Etiquetas de multiplicidad / roles
      const labels: any[] = [];

      if (relacion.multiplicidadOrigen || (!esClaseAsociacion && relacion.rolOrigen)) {
        const textoOrigen = [relacion.multiplicidadOrigen, !esClaseAsociacion ? relacion.rolOrigen : null].filter(Boolean).join(' ');
        if (textoOrigen) {
          labels.push({
            position: { distance: esRecursiva ? 0.15 : 0.18, offset: esRecursiva ? -14 : 14 },
            attrs: {
              text: {
                text: textoOrigen,
                fill: '#94a3b8',
                fontSize: 10,
                fontFamily: 'Inter, sans-serif'
              },
              rect: { fill: '#0f172a', rx: 3, ry: 3 }
            }
          });
        }
      }

      if (relacion.multiplicidadDestino || (!esClaseAsociacion && relacion.rolDestino)) {
        const textoDestino = [relacion.multiplicidadDestino, !esClaseAsociacion ? relacion.rolDestino : null].filter(Boolean).join(' ');
        if (textoDestino) {
          labels.push({
            position: { distance: esRecursiva ? 0.85 : 0.82, offset: 14 },
            attrs: {
              text: {
                text: textoDestino,
                fill: '#94a3b8',
                fontSize: 10,
                fontFamily: 'Inter, sans-serif'
              },
              rect: { fill: '#0f172a', rx: 3, ry: 3 }
            }
          });
        }
      }

      if (labels.length > 0) {
        link.labels(labels);
      }

      links.push(link);

      // Si es Clase de Asociación, conectar la clase intermedia con línea punteada
      if (esClaseAsociacion) {
        const elemO = origen as dia.Element;
        const elemD = destino as dia.Element;
        const bboxO = elemO.getBBox();
        const bboxD = elemD.getBBox();
        const midX = (bboxO.x + bboxD.x) / 2;
        const midY = (bboxO.y + bboxD.y) / 2;

        const nombreOrigen = this.elementos.find(e => e.id === relacion.origen)?.nombre || 'A';
        const nombreDestino = this.elementos.find(e => e.id === relacion.destino)?.nombre || 'B';
        const nombreIntermedio = relacion.rolDestino?.trim() || relacion.rolOrigen?.trim() || `${nombreOrigen}_${nombreDestino}`;

        // 1. Buscar si la clase intermedia existe como elemento real en this.elementos
        const claseIntermediaExistente = this.elementos.find(
          e => e.id !== relacion.origen && e.id !== relacion.destino &&
               (e.nombre.trim().toLowerCase() === nombreIntermedio.trim().toLowerCase() ||
                (e.estereotipo === 'association' && (e.id === relacion.rolDestino || e.nombre.toLowerCase() === nombreIntermedio.toLowerCase())))
        ) || this.elementos.find(
          e => e.id !== relacion.origen && e.id !== relacion.destino && e.estereotipo === 'association'
        );

        let intermediateId: string | null = null;

        if (claseIntermediaExistente) {
          intermediateId = String(claseIntermediaExistente.id);
        } else if (this.graph.getCell(`assoc-class-${relacion.id}`)) {
          intermediateId = `assoc-class-${relacion.id}`;
        }

        if (intermediateId && this.graph.getCell(intermediateId)) {
          const linkConector = new shapes.standard.Link({
            id: `assoc-conn-${relacion.id}`,
            source: { id: intermediateId },
            target: { id: relacion.id },
            attrs: {
              line: {
                stroke: '#06b6d4',
                strokeWidth: 1.5,
                strokeDasharray: '5 5',
                targetMarker: null
              }
            }
          });
          links.push(linkConector);
        }
      }
    }

    if (extraCells.length > 0) {
      this.graph.addCells(extraCells);
    }

    if (links.length > 0) {
      this.graph.addCells(links);
    }
  }

  // =========================================================
  // ESTILOS UML DE RELACIONES
  // =========================================================

  private colorRelacion(tipo: string): string {
    switch ((tipo ?? '').toUpperCase()) {
      case 'GENERALIZACION':
        return '#f59e0b';
      case 'REALIZACION':
        return '#a78bfa';
      case 'AGREGACION':
        return '#38bdf8';
      case 'COMPOSICION':
        return '#f43f5e';
      case 'DEPENDENCIA':
        return '#94a3b8';
      case 'CLASE_ASOCIACION':
      case 'ASOCIACION_CLASE':
        return '#06b6d4';
      case 'RECURSIVA':
        return '#10b981';
      case 'ASOCIACION':
      default:
        return '#cbd5e1';
    }
  }

  private dashRelacion(tipo: string): string {
    const t = (tipo ?? '').toUpperCase();
    if (t === 'REALIZACION' || t === 'DEPENDENCIA') {
      return '5 5';
    }
    return '0';
  }

  private sourceMarkerRelacion(tipo: string): any {
    const t = (tipo ?? '').toUpperCase();
    if (t === 'AGREGACION') {
      return {
        type: 'path',
        d: 'M 0 0 L 10 -6 L 20 0 L 10 6 Z',
        fill: '#0f172a',
        stroke: '#38bdf8',
        strokeWidth: 1.5
      };
    }
    if (t === 'COMPOSICION') {
      return {
        type: 'path',
        d: 'M 0 0 L 10 -6 L 20 0 L 10 6 Z',
        fill: '#f43f5e',
        stroke: '#f43f5e',
        strokeWidth: 1.5
      };
    }
    return null;
  }

  private targetMarkerRelacion(tipo: string): any {
    const t = (tipo ?? '').toUpperCase();
    if (t === 'GENERALIZACION' || t === 'REALIZACION') {
      return {
        type: 'path',
        d: 'M 14 -8 L 0 0 L 14 8 Z',
        fill: '#0f172a',
        stroke: this.colorRelacion(tipo),
        strokeWidth: 1.5
      };
    }
    if (t === 'DEPENDENCIA') {
      return {
        type: 'path',
        d: 'M 10 -5 L 0 0 L 10 5',
        fill: 'none',
        stroke: '#94a3b8',
        strokeWidth: 1.5
      };
    }
    if (t === 'ASOCIACION' || t === 'CLASE_ASOCIACION' || t === 'ASOCIACION_CLASE' || t === 'RECURSIVA') {
      return {
        type: 'path',
        d: 'M 10 -5 L 0 0 L 10 5 Z',
        fill: this.colorRelacion(tipo),
        stroke: this.colorRelacion(tipo)
      };
    }
    return null;
  }

  simboloVisibilidad(visibilidad?: string): string {
    switch ((visibilidad ?? '').toUpperCase()) {
      case 'PRIVATE':
        return '-';
      case 'PROTECTED':
        return '#';
      case 'PACKAGE':
        return '~';
      case 'PUBLIC':
      default:
        return '+';
    }
  }

  // =========================================================
  // SELECCIÓN Y GUARDADO DE POSICIÓN
  // =========================================================

  seleccionarElemento(idElemento: string): void {
    const elem = this.elementos.find(e => e.id === idElemento);
    this.elementoSeleccionado = elem ? { ...elem } : null;
    this.relacionSeleccionada = null;
    this.changeDetector.detectChanges();
  }

  seleccionarRelacion(idRelacion: string): void {
    const rel = this.relaciones.find(r => r.id === idRelacion);
    this.relacionSeleccionada = rel ? { ...rel } : null;
    this.elementoSeleccionado = null;
    this.changeDetector.detectChanges();
  }

  private guardarPosicionElemento(cell: dia.Element): void {
    if (!this.modeloActual) {
      return;
    }

    const idElemento = String(cell.id);
    if (idElemento.startsWith('assoc-class-')) {
      return;
    }
    const pos = cell.position();

    this.guardandoPosicion = true;

    this.http
      .put(`http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/elementos/${idElemento}`, {
        posicionX: pos.x,
        posicionY: pos.y
      })
      .pipe(
        finalize(() => {
          this.guardandoPosicion = false;
        })
      )
      .subscribe({
        next: () => {
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem) {
            elem.posicion = { x: pos.x, y: pos.y };
          }
          if (this.elementoSeleccionado && this.elementoSeleccionado.id === idElemento) {
            this.elementoSeleccionado.posicion = { x: pos.x, y: pos.y };
          }
        },
        error: (err) => {
          console.error('EDITOR UML: error guardando posición:', err);
        }
      });
  }

  // =========================================================
  // GESTIÓN Y EDICIÓN DE ELEMENTOS (CLASE / INTERFAZ / ENUM)
  // =========================================================

  abrirModalElemento(tipo: string = 'CLASE'): void {
    this.nuevoElemento = {
      tipo,
      nombre: '',
      visibilidad: 'PUBLIC',
      estereotipo: tipo === 'INTERFACE' ? 'interface' : tipo === 'ENUM' ? 'enumeration' : '',
      abstracto: false
    };
    this.mostrarModalElemento = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalElemento(): void {
    if (this.guardandoElemento) return;
    this.mostrarModalElemento = false;
  }

  confirmarCrearElemento(): void {
    if (!this.modeloActual) {
      alert('No hay un modelo UML cargado.');
      return;
    }

    const nombre = this.nuevoElemento.nombre.trim();
    if (!nombre) {
      alert('El nombre es obligatorio.');
      return;
    }

    const pos = this.obtenerNuevaPosicion();

    const dto = {
      tipo: this.nuevoElemento.tipo,
      nombre,
      visibilidad: this.nuevoElemento.visibilidad,
      estereotipo: this.nuevoElemento.estereotipo.trim() || null,
      abstracto: this.nuevoElemento.abstracto,
      finalElemento: false,
      posicionX: pos.x,
      posicionY: pos.y,
      ancho: 260,
      alto: 180
    };

    this.guardandoElemento = true;

    this.http
      .post<ModeloUML>(`http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/elementos`, dto)
      .pipe(
        finalize(() => {
          this.guardandoElemento = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (modelo) => {
          this.modeloActual = modelo;
          this.elementos = modelo.contenidoJson?.elementos ?? [];
          this.relaciones = modelo.contenidoJson?.relaciones ?? [];
          this.mostrarModalElemento = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();

          const recienCreado = this.elementos.find(e => e.nombre === nombre);
          if (recienCreado) {
            this.seleccionarElemento(recienCreado.id);
          }
        },
        error: (error) => {
          console.error('EDITOR UML: error creando elemento:', error);
          alert(error.error?.message ?? 'No se pudo crear el elemento.');
        }
      });
  }

  // AUTO-GUARDADO: Guardar edición de elemento desde el panel de propiedades
  guardarEdicionElemento(): void {
    if (!this.modeloActual || !this.elementoSeleccionado) return;

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;

    const dto = {
      nombre: this.elementoSeleccionado.nombre.trim(),
      visibilidad: this.elementoSeleccionado.visibilidad,
      estereotipo: this.elementoSeleccionado.estereotipo?.trim() || null,
      abstracto: this.elementoSeleccionado.abstracto ?? false,
      finalElemento: this.elementoSeleccionado.finalElemento ?? false
    };

    this.guardandoElemento = true;

    this.http
      .put(`http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}`, dto)
      .pipe(
        finalize(() => {
          this.guardandoElemento = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          const index = this.elementos.findIndex(e => e.id === idElemento);
          if (index !== -1) {
            this.elementos[index] = {
              ...this.elementos[index],
              nombre: dto.nombre,
              visibilidad: dto.visibilidad,
              estereotipo: dto.estereotipo,
              abstracto: dto.abstracto,
              finalElemento: dto.finalElemento
            };
          }
          this.sincronizarConJointJS();
        },
        error: (err) => {
          console.error('EDITOR UML: error editando elemento:', err);
          alert(err.error?.message ?? 'No se pudo guardar la modificación del elemento.');
        }
      });
  }

  eliminarElemento(idElemento: string): void {
    if (!this.modeloActual) return;
    if (!confirm('¿Estás seguro de eliminar este elemento UML y sus relaciones asociadas?')) return;

    this.eliminando = true;

    this.http
      .delete(`http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/elementos/${idElemento}`)
      .pipe(
        finalize(() => {
          this.eliminando = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.elementos = this.elementos.filter(e => e.id !== idElemento);
          this.relaciones = this.relaciones.filter(r => r.origen !== idElemento && r.destino !== idElemento);
          if (this.elementoSeleccionado?.id === idElemento) {
            this.elementoSeleccionado = null;
          }
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error eliminando elemento:', error);
          alert(error.error?.message ?? 'No se pudo eliminar el elemento.');
        }
      });
  }

  // =========================================================
  // GESTIÓN Y EDICIÓN DE ATRIBUTOS
  // =========================================================

  abrirModalAtributo(): void {
    if (!this.elementoSeleccionado) {
      alert('Primero selecciona una clase o elemento UML.');
      return;
    }

    this.nuevoAtributo = {
      nombre: '',
      tipo: 'String',
      visibilidad: 'PRIVATE',
      multiplicidad: '1',
      valorInicial: '',
      derivado: false,
      estatico: false
    };
    this.mostrarModalAtributo = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalAtributo(): void {
    if (this.guardandoAtributo) return;
    this.mostrarModalAtributo = false;
  }

  confirmarAgregarAtributo(): void {
    if (!this.modeloActual || !this.elementoSeleccionado) return;

    const nombre = this.nuevoAtributo.nombre.trim();
    const tipo = this.nuevoAtributo.tipo.trim();

    if (!nombre) {
      alert('El nombre del atributo es obligatorio.');
      return;
    }
    if (!tipo) {
      alert('El tipo del atributo es obligatorio.');
      return;
    }

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;

    const dto = {
      nombre,
      tipo,
      visibilidad: this.nuevoAtributo.visibilidad,
      multiplicidad: this.nuevoAtributo.multiplicidad,
      derivado: this.nuevoAtributo.derivado,
      estatico: this.nuevoAtributo.estatico,
      valorInicial: this.nuevoAtributo.valorInicial.trim() || null
    };

    this.guardandoAtributo = true;

    this.http
      .post<AtributoUML>(`http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/atributos`, dto)
      .pipe(
        finalize(() => {
          this.guardandoAtributo = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (atributo) => {
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem) {
            elem.atributos = [...(elem.atributos ?? []), atributo];
            this.elementoSeleccionado = { ...elem };
          }
          this.mostrarModalAtributo = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error creando atributo:', error);
          alert(error.error?.message ?? 'No se pudo agregar el atributo.');
        }
      });
  }

  abrirModalEditarAtributo(atr: AtributoUML): void {
    this.idAtributoEditando = atr.id;
    this.atributoEditando = {
      nombre: atr.nombre,
      tipo: atr.tipo,
      visibilidad: atr.visibilidad || 'PRIVATE',
      multiplicidad: atr.multiplicidad || '1',
      valorInicial: atr.valorInicial || '',
      derivado: atr.derivado ?? false,
      estatico: atr.estatico ?? false
    };
    this.mostrarModalEditarAtributo = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalEditarAtributo(): void {
    if (this.guardandoAtributo) return;
    this.mostrarModalEditarAtributo = false;
  }

  confirmarEditarAtributo(): void {
    if (!this.modeloActual || !this.elementoSeleccionado || !this.idAtributoEditando) return;

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;
    const idAtributo = this.idAtributoEditando;

    const dto = {
      nombre: this.atributoEditando.nombre.trim(),
      tipo: this.atributoEditando.tipo.trim(),
      visibilidad: this.atributoEditando.visibilidad,
      multiplicidad: this.atributoEditando.multiplicidad,
      derivado: this.atributoEditando.derivado,
      estatico: this.atributoEditando.estatico,
      valorInicial: this.atributoEditando.valorInicial.trim() || null
    };

    this.guardandoAtributo = true;

    this.http
      .put(`http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/atributos/${idAtributo}`, dto)
      .pipe(
        finalize(() => {
          this.guardandoAtributo = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (atrActualizado: any) => {
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem && elem.atributos) {
            const index = elem.atributos.findIndex(a => a.id === idAtributo);
            if (index !== -1) {
              elem.atributos[index] = { ...elem.atributos[index], ...dto, id: idAtributo };
              this.elementoSeleccionado = { ...elem };
            }
          }
          this.mostrarModalEditarAtributo = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (err) => {
          console.error('EDITOR UML: error editando atributo:', err);
          alert(err.error?.message ?? 'No se pudo editar el atributo.');
        }
      });
  }

  eliminarAtributo(idAtributo: string): void {
    if (!this.modeloActual || !this.elementoSeleccionado) return;
    if (!confirm('¿Eliminar este atributo?')) return;

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;

    this.http
      .delete(`http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/atributos/${idAtributo}`)
      .subscribe({
        next: () => {
          if (this.elementoSeleccionado) {
            this.elementoSeleccionado.atributos = (this.elementoSeleccionado.atributos ?? []).filter(a => a.id !== idAtributo);
          }
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem) {
            elem.atributos = (elem.atributos ?? []).filter(a => a.id !== idAtributo);
          }
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error eliminando atributo:', error);
          alert(error.error?.message ?? 'No se pudo eliminar el atributo.');
        }
      });
  }

  // =========================================================
  // GESTIÓN Y EDICIÓN DE OPERACIONES (MÉTODOS)
  // =========================================================

  abrirModalOperacion(): void {
    if (!this.elementoSeleccionado) {
      alert('Primero selecciona una clase o elemento UML.');
      return;
    }

    this.nuevaOperacion = {
      nombre: '',
      tipoRetorno: 'void',
      visibilidad: 'PUBLIC',
      estatica: false,
      abstracta: false,
      parametros: []
    };

    this.nuevoParametroOperacion = {
      nombre: '',
      tipo: 'String',
      direccion: 'IN',
      valorInicial: ''
    };

    this.mostrarModalOperacion = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalOperacion(): void {
    if (this.guardandoOperacion) return;
    this.mostrarModalOperacion = false;
  }

  agregarParametroOperacionNueva(): void {
    const nombre = this.nuevoParametroOperacion.nombre.trim();
    const tipo = this.nuevoParametroOperacion.tipo.trim();

    if (!nombre) {
      alert('El nombre del parámetro es obligatorio.');
      return;
    }

    if (!tipo) {
      alert('El tipo del parámetro es obligatorio.');
      return;
    }

    const existe = this.nuevaOperacion.parametros.some(
      parametro => parametro.nombre.trim().toLowerCase() === nombre.toLowerCase()
    );

    if (existe) {
      alert(`Ya existe un parámetro con el nombre '${nombre}'.`);
      return;
    }

    this.nuevaOperacion.parametros.push({
      id: `tmp-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      nombre,
      tipo,
      direccion: this.nuevoParametroOperacion.direccion,
      valorInicial: this.nuevoParametroOperacion.valorInicial.trim() || null
    });

    this.nuevoParametroOperacion = {
      nombre: '',
      tipo: 'String',
      direccion: 'IN',
      valorInicial: ''
    };

    this.changeDetector.detectChanges();
  }

  eliminarParametroOperacionNueva(idParametro: string): void {
    this.nuevaOperacion.parametros = this.nuevaOperacion.parametros.filter(
      parametro => parametro.id !== idParametro
    );
  }

  private agregarParametrosOperacionCreada(
    idModelo: number,
    idElemento: string,
    operacion: OperacionUML,
    parametros: ParametroUML[]
  ) {
    if (parametros.length === 0) {
      return of(operacion);
    }

    return from(parametros).pipe(
      concatMap(parametro =>
        this.http.post<ParametroUML>(
          `http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/operaciones/${operacion.id}/parametros`,
          {
            nombre: parametro.nombre,
            tipo: parametro.tipo,
            direccion: parametro.direccion || 'IN',
            valorInicial: parametro.valorInicial ?? null
          }
        )
      ),
      toArray(),
      map(parametrosGuardados => ({
        ...operacion,
        parametros: parametrosGuardados
      }))
    );
  }

  confirmarAgregarOperacion(): void {
    if (!this.modeloActual || !this.elementoSeleccionado) return;

    const nombre = this.nuevaOperacion.nombre.trim();
    if (!nombre) {
      alert('El nombre de la operación es obligatorio.');
      return;
    }

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;

    const dto = {
      nombre,
      tipoRetorno: this.nuevaOperacion.tipoRetorno.trim() || 'void',
      visibilidad: this.nuevaOperacion.visibilidad,
      estatica: this.nuevaOperacion.estatica,
      abstracta: this.nuevaOperacion.abstracta
    };

    const parametros = [...this.nuevaOperacion.parametros];

    this.guardandoOperacion = true;

    this.http
      .post<OperacionUML>(
        `http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/operaciones`,
        dto
      )
      .pipe(
        concatMap(operacion =>
          this.agregarParametrosOperacionCreada(
            idModelo,
            idElemento,
            operacion,
            parametros
          )
        ),
        finalize(() => {
          this.guardandoOperacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (operacionCompleta) => {
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem) {
            elem.operaciones = [
              ...(elem.operaciones ?? []),
              operacionCompleta
            ];
            this.elementoSeleccionado = { ...elem };
          }
          this.mostrarModalOperacion = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error agregando operación:', error);
          alert(error.error?.message ?? 'No se pudo agregar la operación o sus parámetros.');
        }
      });
  }

  abrirModalEditarOperacion(op: OperacionUML): void {
    this.idOperacionEditando = op.id;
    this.operacionEditando = {
      nombre: op.nombre,
      tipoRetorno: op.tipoRetorno || 'void',
      visibilidad: op.visibilidad || 'PUBLIC',
      estatica: op.estatica ?? false,
      abstracta: op.abstracta ?? false,
      parametros: [...(op.parametros ?? [])]
    };

    this.nuevoParametroOperacion = {
      nombre: '',
      tipo: 'String',
      direccion: 'IN',
      valorInicial: ''
    };

    this.mostrarModalEditarOperacion = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalEditarOperacion(): void {
    if (this.guardandoOperacion || this.guardandoParametroOperacion) return;
    this.mostrarModalEditarOperacion = false;
  }

  agregarParametroOperacionEditando(): void {
    if (!this.modeloActual || !this.elementoSeleccionado || !this.idOperacionEditando) return;

    const nombre = this.nuevoParametroOperacion.nombre.trim();
    const tipo = this.nuevoParametroOperacion.tipo.trim();

    if (!nombre) {
      alert('El nombre del parámetro es obligatorio.');
      return;
    }

    if (!tipo) {
      alert('El tipo del parámetro es obligatorio.');
      return;
    }

    const existe = this.operacionEditando.parametros.some(
      parametro => parametro.nombre.trim().toLowerCase() === nombre.toLowerCase()
    );

    if (existe) {
      alert(`Ya existe un parámetro con el nombre '${nombre}'.`);
      return;
    }

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;
    const idOperacion = this.idOperacionEditando;

    const dto = {
      nombre,
      tipo,
      direccion: this.nuevoParametroOperacion.direccion,
      valorInicial: this.nuevoParametroOperacion.valorInicial.trim() || null
    };

    this.guardandoParametroOperacion = true;

    this.http
      .post<ParametroUML>(
        `http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/operaciones/${idOperacion}/parametros`,
        dto
      )
      .pipe(
        finalize(() => {
          this.guardandoParametroOperacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (parametro) => {
          this.operacionEditando.parametros = [
            ...this.operacionEditando.parametros,
            parametro
          ];
          this.actualizarOperacionLocal(idElemento, idOperacion, {
            parametros: this.operacionEditando.parametros
          });
          this.nuevoParametroOperacion = {
            nombre: '',
            tipo: 'String',
            direccion: 'IN',
            valorInicial: ''
          };
        },
        error: (error) => {
          console.error('EDITOR UML: error agregando parámetro:', error);
          alert(error.error?.message ?? 'No se pudo agregar el parámetro.');
        }
      });
  }

  editarParametroOperacion(parametro: ParametroUML): void {
    if (!this.modeloActual || !this.elementoSeleccionado || !this.idOperacionEditando) return;

    const dto = {
      nombre: parametro.nombre.trim(),
      tipo: parametro.tipo.trim(),
      direccion: parametro.direccion || 'IN',
      valorInicial: parametro.valorInicial ?? null
    };

    this.http
      .put<ParametroUML>(
        `http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/elementos/${this.elementoSeleccionado.id}/operaciones/${this.idOperacionEditando}/parametros/${parametro.id}`,
        dto
      )
      .subscribe({
        next: (parametroActualizado) => {
          this.operacionEditando.parametros = this.operacionEditando.parametros.map(p =>
            p.id === parametro.id ? parametroActualizado : p
          );
          this.actualizarOperacionLocal(
            this.elementoSeleccionado!.id,
            this.idOperacionEditando,
            { parametros: this.operacionEditando.parametros }
          );
          this.changeDetector.detectChanges();
        },
        error: (error) => {
          console.error('EDITOR UML: error editando parámetro:', error);
          alert(error.error?.message ?? 'No se pudo editar el parámetro.');
        }
      });
  }

  eliminarParametroOperacionEditando(parametro: ParametroUML): void {
    if (!this.modeloActual || !this.elementoSeleccionado || !this.idOperacionEditando) return;
    if (!confirm(`¿Eliminar el parámetro '${parametro.nombre}'?`)) return;

    this.http
      .delete(
        `http://localhost:8080/api/modelos/${this.modeloActual.idModelo}/elementos/${this.elementoSeleccionado.id}/operaciones/${this.idOperacionEditando}/parametros/${parametro.id}`
      )
      .subscribe({
        next: () => {
          this.operacionEditando.parametros = this.operacionEditando.parametros.filter(p => p.id !== parametro.id);
          this.actualizarOperacionLocal(
            this.elementoSeleccionado!.id,
            this.idOperacionEditando,
            { parametros: this.operacionEditando.parametros }
          );
          this.changeDetector.detectChanges();
        },
        error: (error) => {
          console.error('EDITOR UML: error eliminando parámetro:', error);
          alert(error.error?.message ?? 'No se pudo eliminar el parámetro.');
        }
      });
  }

  private actualizarOperacionLocal(
    idElemento: string,
    idOperacion: string,
    cambios: Partial<OperacionUML>
  ): void {
    const elem = this.elementos.find(e => e.id === idElemento);
    if (!elem || !elem.operaciones) return;

    const index = elem.operaciones.findIndex(o => o.id === idOperacion);
    if (index === -1) return;

    elem.operaciones[index] = {
      ...elem.operaciones[index],
      ...cambios
    };

    this.elementoSeleccionado = { ...elem };
  }

  formatearParametrosOperacion(op: { parametros?: ParametroUML[] }): string {
    return (op.parametros ?? [])
      .map(parametro => `${parametro.nombre}: ${parametro.tipo}`)
      .join(', ');
  }

  confirmarEditarOperacion(): void {
    if (!this.modeloActual || !this.elementoSeleccionado || !this.idOperacionEditando) return;

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;
    const idOperacion = this.idOperacionEditando;

    const dto = {
      nombre: this.operacionEditando.nombre.trim(),
      tipoRetorno: this.operacionEditando.tipoRetorno.trim() || 'void',
      visibilidad: this.operacionEditando.visibilidad,
      estatica: this.operacionEditando.estatica,
      abstracta: this.operacionEditando.abstracta
    };

    this.guardandoOperacion = true;

    this.http
      .put(
        `http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/operaciones/${idOperacion}`,
        dto
      )
      .pipe(
        finalize(() => {
          this.guardandoOperacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.actualizarOperacionLocal(idElemento, idOperacion, {
            ...dto,
            parametros: this.operacionEditando.parametros
          });
          this.mostrarModalEditarOperacion = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (err) => {
          console.error('EDITOR UML: error editando operación:', err);
          alert(err.error?.message ?? 'No se pudo editar la operación.');
        }
      });
  }

  eliminarOperacion(idOperacion: string): void {
    if (!this.modeloActual || !this.elementoSeleccionado) return;
    if (!confirm('¿Eliminar esta operación?')) return;

    const idModelo = this.modeloActual.idModelo;
    const idElemento = this.elementoSeleccionado.id;

    this.http
      .delete(`http://localhost:8080/api/modelos/${idModelo}/elementos/${idElemento}/operaciones/${idOperacion}`)
      .subscribe({
        next: () => {
          if (this.elementoSeleccionado) {
            this.elementoSeleccionado.operaciones = (this.elementoSeleccionado.operaciones ?? []).filter(o => o.id !== idOperacion);
          }
          const elem = this.elementos.find(e => e.id === idElemento);
          if (elem) {
            elem.operaciones = (elem.operaciones ?? []).filter(o => o.id !== idOperacion);
          }
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error eliminando operación:', error);
          alert(error.error?.message ?? 'No se pudo eliminar la operación.');
        }
      });
  }

  // =========================================================
  // GESTIÓN Y EDICIÓN DE RELACIONES
  // =========================================================

  sugerirNombreClaseAsociacion(): void {
    if (this.nuevaRelacion.tipo === 'CLASE_ASOCIACION') {
      const elemOrigen = this.elementos.find(e => e.id === this.nuevaRelacion.origen);
      const elemDestino = this.elementos.find(e => e.id === this.nuevaRelacion.destino);
      if (elemOrigen && elemDestino) {
        if (!this.nuevaRelacion.rolDestino || this.nuevaRelacion.rolDestino.includes('_')) {
          this.nuevaRelacion.rolDestino = `${elemOrigen.nombre}_${elemDestino.nombre}`;
        }
      }
    }
  }

  seleccionarTipoRecursiva(): void {
    this.nuevaRelacion.tipo = 'RECURSIVA';
    if (this.nuevaRelacion.origen) {
      this.nuevaRelacion.destino = this.nuevaRelacion.origen;
    }
    if (!this.nuevaRelacion.multiplicidadOrigen) {
      this.nuevaRelacion.multiplicidadOrigen = '1';
    }
    if (!this.nuevaRelacion.multiplicidadDestino) {
      this.nuevaRelacion.multiplicidadDestino = '0..*';
    }
  }

  alCambiarOrigenRelacion(): void {
    this.sugerirNombreClaseAsociacion();
    if (this.nuevaRelacion.tipo === 'RECURSIVA') {
      this.nuevaRelacion.destino = this.nuevaRelacion.origen;
    }
  }

  abrirModalRelacion(origenId?: string): void {
    if (this.elementos.length < 1) {
      alert('Debes tener al menos 1 elemento UML para crear una relación.');
      return;
    }

    const defaultOrigen = origenId || this.elementoSeleccionado?.id || this.elementos[0]?.id || '';
    const defaultDestino = this.elementos.find(e => e.id !== defaultOrigen)?.id || defaultOrigen;

    this.nuevaRelacion = {
      tipo: 'ASOCIACION',
      origen: defaultOrigen,
      destino: defaultDestino,
      rolOrigen: '',
      rolDestino: '',
      multiplicidadOrigen: '1',
      multiplicidadDestino: '1'
    };

    this.mostrarModalRelacion = true;
    this.changeDetector.detectChanges();
  }

  cerrarModalRelacion(): void {
    if (this.guardandoRelacion) return;
    this.mostrarModalRelacion = false;
  }

  confirmarAgregarRelacion(): void {
    if (!this.modeloActual) return;

    if (!this.nuevaRelacion.origen || !this.nuevaRelacion.destino) {
      alert('Debes seleccionar el elemento de origen y de destino.');
      return;
    }

    if (this.nuevaRelacion.origen === this.nuevaRelacion.destino) {
      this.nuevaRelacion.tipo = 'RECURSIVA';
    }

    const idModelo = this.modeloActual.idModelo;
    const esClaseAsociacion = this.nuevaRelacion.tipo === 'CLASE_ASOCIACION';

    const dto = {
      tipo: this.nuevaRelacion.tipo,
      origen: this.nuevaRelacion.origen,
      destino: this.nuevaRelacion.destino,
      rolOrigen: this.nuevaRelacion.rolOrigen.trim() || null,
      rolDestino: this.nuevaRelacion.rolDestino.trim() || null,
      multiplicidadOrigen: this.nuevaRelacion.multiplicidadOrigen || null,
      multiplicidadDestino: this.nuevaRelacion.multiplicidadDestino || null
    };

    this.guardandoRelacion = true;

    // Si es CLASE_ASOCIACION, primero creamos la clase intermedia como una CLASE real en el modelo
    if (esClaseAsociacion) {
      const elemOrigen = this.elementos.find(e => e.id === this.nuevaRelacion.origen);
      const elemDestino = this.elementos.find(e => e.id === this.nuevaRelacion.destino);
      const posOX = elemOrigen?.posicion?.x ?? 100;
      const posOY = elemOrigen?.posicion?.y ?? 100;
      const posDX = elemDestino?.posicion?.x ?? 400;
      const posDY = elemDestino?.posicion?.y ?? 100;
      const midX = Math.round((posOX + posDX) / 2);
      const midY = Math.round((posOY + posDY) / 2) - 150;

      const nombreClaseAsociacion =
        this.nuevaRelacion.rolDestino?.trim() ||
        `${elemOrigen?.nombre || 'ClaseA'}_${elemDestino?.nombre || 'ClaseB'}`;

      dto.rolDestino = nombreClaseAsociacion;

      const dtoElemento = {
        tipo: 'CLASE',
        nombre: nombreClaseAsociacion,
        visibilidad: 'PUBLIC',
        estereotipo: 'association',
        abstracto: false,
        posicionX: midX,
        posicionY: midY > 40 ? midY : (posOY + posDY) / 2 + 160
      };

      this.http
        .post<ElementoUML>(`http://localhost:8080/api/modelos/${idModelo}/elementos`, dtoElemento)
        .subscribe({
          next: (nuevaClaseCreada) => {
            nuevaClaseCreada.atributos = [];
            nuevaClaseCreada.operaciones = [];
            this.elementos = [...this.elementos, nuevaClaseCreada];

            this.http
              .post<RelacionUML>(`http://localhost:8080/api/modelos/${idModelo}/relaciones`, dto)
              .pipe(
                finalize(() => {
                  this.guardandoRelacion = false;
                  this.changeDetector.detectChanges();
                })
              )
              .subscribe({
                next: (relacion) => {
                  this.relaciones = [...this.relaciones, relacion];
                  this.mostrarModalRelacion = false;
                  this.changeDetector.detectChanges();
                  this.sincronizarConJointJS();
                  // Seleccionar la clase creada para que el usuario pueda agregarle atributos y métodos de inmediato
                  this.seleccionarElemento(nuevaClaseCreada.id);
                },
                error: (error) => {
                  console.error('EDITOR UML: error creando relación de clase de asociación:', error);
                  alert(error.error?.message ?? 'No se pudo crear la relación.');
                }
              });
          },
          error: (err) => {
            this.guardandoRelacion = false;
            this.changeDetector.detectChanges();
            console.error('EDITOR UML: error creando clase intermedia de asociación:', err);
            alert(err.error?.message ?? 'No se pudo crear la clase intermedia de la asociación.');
          }
        });

      return;
    }

    this.http
      .post<RelacionUML>(`http://localhost:8080/api/modelos/${idModelo}/relaciones`, dto)
      .pipe(
        finalize(() => {
          this.guardandoRelacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (relacion) => {
          this.relaciones = [...this.relaciones, relacion];
          this.mostrarModalRelacion = false;
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
          this.seleccionarRelacion(relacion.id);
        },
        error: (error) => {
          console.error('EDITOR UML: error creando relación:', error);
          alert(error.error?.message ?? 'No se pudo crear la relación.');
        }
      });
  }

  // AUTO-GUARDADO: Guardar edición de relación desde el panel de propiedades
  guardarEdicionRelacion(): void {
    if (!this.modeloActual || !this.relacionSeleccionada) return;

    const idModelo = this.modeloActual.idModelo;
    const idRelacion = this.relacionSeleccionada.id;

    const dto = {
      tipo: this.relacionSeleccionada.tipo,
      origen: this.relacionSeleccionada.origen,
      destino: this.relacionSeleccionada.destino,
      rolOrigen: this.relacionSeleccionada.rolOrigen?.trim() || null,
      rolDestino: this.relacionSeleccionada.rolDestino?.trim() || null,
      multiplicidadOrigen: this.relacionSeleccionada.multiplicidadOrigen || null,
      multiplicidadDestino: this.relacionSeleccionada.multiplicidadDestino || null
    };

    this.guardandoRelacion = true;

    this.http
      .put(`http://localhost:8080/api/modelos/${idModelo}/relaciones/${idRelacion}`, dto)
      .pipe(
        finalize(() => {
          this.guardandoRelacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          const index = this.relaciones.findIndex(r => r.id === idRelacion);
          if (index !== -1) {
            this.relaciones[index] = { ...this.relaciones[index], ...dto };
          }
          this.sincronizarConJointJS();
        },
        error: (err) => {
          console.error('EDITOR UML: error editando relación:', err);
          alert(err.error?.message ?? 'No se pudo guardar la modificación de la relación.');
        }
      });
  }

  eliminarRelacion(idRelacion: string): void {
    if (!this.modeloActual) return;
    if (!confirm('¿Eliminar esta relación?')) return;

    const idModelo = this.modeloActual.idModelo;

    this.http
      .delete(`http://localhost:8080/api/modelos/${idModelo}/relaciones/${idRelacion}`)
      .subscribe({
        next: () => {
          this.relaciones = this.relaciones.filter(r => r.id !== idRelacion);
          if (this.relacionSeleccionada?.id === idRelacion) {
            this.relacionSeleccionada = null;
          }
          this.changeDetector.detectChanges();
          this.sincronizarConJointJS();
        },
        error: (error) => {
          console.error('EDITOR UML: error eliminando relación:', error);
          alert(error.error?.message ?? 'No se pudo eliminar la relación.');
        }
      });
  }

  // =========================================================
  // MODO CONEXIÓN INTERACTIVO
  // =========================================================

  activarModoConexion(): void {
    if (this.elementos.length < 2) {
      alert('Necesitas al menos 2 elementos UML para conectarlos.');
      return;
    }
    this.modoConexion = true;
    this.origenConexion = this.elementoSeleccionado ?? null;
    this.changeDetector.detectChanges();
  }

  cancelarModoConexion(): void {
    this.modoConexion = false;
    this.origenConexion = null;
    this.changeDetector.detectChanges();
  }

  private procesarClickConexion(idElemento: string): void {
    const elemento = this.elementos.find(e => e.id === idElemento);
    if (!elemento) return;

    if (!this.origenConexion) {
      this.origenConexion = elemento;
      this.changeDetector.detectChanges();
    } else {
      const origenId = this.origenConexion.id;
      const destinoId = elemento.id;
      const esMismo = origenId === destinoId;
      this.cancelarModoConexion();

      this.nuevaRelacion = {
        tipo: esMismo ? 'RECURSIVA' : 'ASOCIACION',
        origen: origenId,
        destino: destinoId,
        rolOrigen: '',
        rolDestino: '',
        multiplicidadOrigen: '1',
        multiplicidadDestino: esMismo ? '0..*' : '1'
      };
      this.mostrarModalRelacion = true;
      this.changeDetector.detectChanges();
    }
  }

  // =========================================================
  // ZOOM Y PAN
  // =========================================================

  aplicarZoom(delta: number): void {
    if (!this.paper) return;
    const oldZoom = this.zoomLevel;
    const newZoom = Math.min(2.5, Math.max(0.3, Math.round((oldZoom + delta) * 100) / 100));
    this.zoomLevel = newZoom;
    this.paper.scale(newZoom, newZoom);
    this.changeDetector.detectChanges();
  }

  zoomIn(): void {
    this.aplicarZoom(0.15);
  }

  zoomOut(): void {
    this.aplicarZoom(-0.15);
  }

  centrarDiagrama(): void {
    if (!this.paper || !this.graph || !this.umlCanvas?.nativeElement) return;
    const cells = this.graph.getElements();
    if (cells.length === 0) {
      this.paper.scale(1.0, 1.0);
      this.paper.translate(0, 0);
      this.zoomLevel = 1.0;
      this.changeDetector.detectChanges();
      return;
    }
    const bbox = this.graph.getBBox();
    if (!bbox) return;
    const contenedor = this.umlCanvas.nativeElement;
    const anchoContenedor = contenedor.clientWidth || 1000;
    const altoContenedor = contenedor.clientHeight || 700;

    // Calcular escala adecuada para visualizar todo cómodamente
    const padding = 80;
    const escalaX = (anchoContenedor - padding * 2) / Math.max(bbox.width, 100);
    const escalaY = (altoContenedor - padding * 2) / Math.max(bbox.height, 100);
    const fitScale = Math.min(1.0, Math.max(0.5, Math.min(escalaX, escalaY)));

    this.zoomLevel = Math.round(fitScale * 100) / 100;
    this.paper.scale(this.zoomLevel, this.zoomLevel);

    const centroDiagramaX = bbox.x + bbox.width / 2;
    const centroDiagramaY = bbox.y + bbox.height / 2;

    const tx = (anchoContenedor / 2) - (centroDiagramaX * this.zoomLevel);
    const ty = (altoContenedor / 2) - (centroDiagramaY * this.zoomLevel);

    this.paper.translate(tx, ty);
    this.changeDetector.detectChanges();
  }

  zoomReset(): void {
    this.centrarDiagrama();
  }

  obtenerPorcentajeZoom(): number {
    return Math.round(this.zoomLevel * 100);
  }

  obtenerNombreElemento(id: string): string {
    const elem = this.elementos.find(e => e.id === id);
    return elem ? elem.nombre : id;
  }

  hayElementos(): boolean {
    return this.elementos.length > 0;
  }

  private obtenerNuevaPosicion(): { x: number; y: number } {
    const cantidad = this.elementos.length;
    return {
      x: 80 + (cantidad % 3) * 320,
      y: 80 + Math.floor(cantidad / 3) * 260
    };
  }

  // =========================================================
  // COLABORADORES
  // =========================================================

  abrirColaboradores(): void {
    this.mostrarColaboradores = true;
    this.mensajeColaborador = '';
  }

  cerrarColaboradores(): void {
    this.mostrarColaboradores = false;
  }

  abrirInvitar(): void {
    this.mostrarInvitar = true;
    this.usuarioEncontrado = null;
    this.usernameBusqueda = '';
    this.mensajeInvitacion = '';
  }

  cerrarInvitar(): void {
    this.mostrarInvitar = false;
    this.usuarioEncontrado = null;
    this.usernameBusqueda = '';
    this.mensajeInvitacion = '';
  }

  buscarUsuario(): void {
    const username = this.usernameBusqueda.trim();
    if (!username) {
      this.mensajeInvitacion = 'Escribe el username que deseas buscar.';
      return;
    }

    this.buscandoUsuario = true;
    this.mensajeInvitacion = '';
    this.usuarioEncontrado = null;

    this.invitacionService
      .buscarUsuario(username)
      .pipe(
        finalize(() => {
          this.buscandoUsuario = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: (usuario: UsuarioBusqueda) => {
          this.usuarioEncontrado = usuario;
        },
        error: (error: any) => {
          if (error.status === 404) {
            this.mensajeInvitacion = 'No se encontró ningún usuario con ese username.';
          } else {
            this.mensajeInvitacion = 'Error al buscar el usuario.';
          }
        }
      });
  }

  enviarInvitacion(): void {
    if (!this.proyecto || !this.usuarioEncontrado) return;

    const emisor = this.auth.obtenerUsuario();
    if (!emisor) {
      this.mensajeInvitacion = 'No hay una sesión de usuario válida.';
      return;
    }

    this.enviandoInvitacion = true;
    this.mensajeInvitacion = '';

    this.invitacionService
      .crear({
        idProyecto: this.proyecto.idProyecto,
        idEmisor: emisor.idUsuario,
        idReceptor: this.usuarioEncontrado.idUsuario,
        rolPropuesto: 'COLABORADOR'
      })
      .pipe(
        finalize(() => {
          this.enviandoInvitacion = false;
          this.changeDetector.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.mensajeInvitacion = 'Invitación enviada correctamente.';
          this.usuarioEncontrado = null;
          this.usernameBusqueda = '';
        },
        error: (error: any) => {
          this.mensajeInvitacion = error.error?.message || 'No se pudo enviar la invitación.';
        }
      });
  }

  volverDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
  // =========================================================
// ABRIR IMPORTADOR JSON
// =========================================================

abrirImportadorJson(
  input: HTMLInputElement
): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  input.click();
}


// =========================================================
// IMPORTAR JSON
// =========================================================

importarJson(
  event: Event
): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  const input =
    event.target as HTMLInputElement;

  const archivo =
    input.files?.[0];

  if (!archivo) {
    return;
  }

  if (
    !archivo.name
      .toLowerCase()
      .endsWith('.json')
  ) {

    alert(
      'Selecciona un archivo JSON válido.'
    );

    input.value = '';

    return;
  }

  console.log(
    'INTERCAMBIO: importando archivo:',
    archivo.name
  );

  const lector =
    new FileReader();

  lector.onload = () => {

    try {

      const contenido =
        JSON.parse(
          String(
            lector.result
          )
        );

      console.log(
        'INTERCAMBIO: JSON leído:',
        contenido
      );

      this.intercambioService
        .importarJson(
          this.modeloActual!.idModelo,
          contenido
        )
        .subscribe({

          next: (resultado) => {

            console.log(
              'INTERCAMBIO: JSON importado correctamente:',
              resultado
            );

            this.elementos =
  (resultado.elementos ?? []) as ElementoUML[];

this.relaciones =
  (resultado.relaciones ?? []) as RelacionUML[];

            this.elementoSeleccionado =
              null;

            this.relacionSeleccionada =
              null;

            setTimeout(() => {

              this.inicializarJointJS();

              this.changeDetector.detectChanges();

            }, 0);

            alert(
              'Modelo importado correctamente.'
            );
          },

          error: (error) => {

            console.error(
              'INTERCAMBIO: error importando JSON:',
              error
            );

            alert(
              error.error?.message ??
              'No se pudo importar el modelo.'
            );
          }

        });

    }
    catch (error) {

      console.error(
        'INTERCAMBIO: JSON inválido:',
        error
      );

      alert(
        'El archivo seleccionado no contiene un JSON válido.'
      );

    }
    finally {

      input.value = '';

    }

  };

  lector.readAsText(
    archivo
  );
}
exportarXmi(): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  const idModelo =
    this.modeloActual.idModelo;

  console.log(
    'INTERCAMBIO: exportando XMI del modelo',
    idModelo
  );

  this.intercambioService
    .exportarXmi(idModelo)
    .subscribe({

      next: (response) => {

        if (!response.body) {

          alert(
            'El servidor no devolvió ningún archivo XMI.'
          );

          return;
        }

        const blob =
          response.body;

        const url =
          window.URL.createObjectURL(
            blob
          );

        const enlace =
          document.createElement('a');

        enlace.href =
          url;

        enlace.download =
          `modelo-${idModelo}.xmi`;

        document.body.appendChild(
          enlace
        );

        enlace.click();

        document.body.removeChild(
          enlace
        );

        window.URL.revokeObjectURL(
          url
        );

        console.log(
          'INTERCAMBIO: XMI exportado correctamente'
        );
      },

      error: (error) => {

        console.error(
          'INTERCAMBIO: error exportando XMI:',
          error
        );

        alert(
          'No se pudo exportar el modelo como XMI.'
        );
      }

    });
}
abrirImportadorXmi(
  input: HTMLInputElement
): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  input.click();
}
importarXmi(
  event: Event
): void {

  if (!this.modeloActual) {

    alert(
      'No hay un modelo UML cargado.'
    );

    return;
  }

  const input =
    event.target as HTMLInputElement;

  const archivo =
    input.files?.[0];

  if (!archivo) {
    return;
  }

  const nombre =
    archivo.name.toLowerCase();

  if (
    !nombre.endsWith('.xmi') &&
    !nombre.endsWith('.xml')
  ) {

    alert(
      'Selecciona un archivo XMI/XML válido.'
    );

    input.value = '';

    return;
  }

  console.log(
    'INTERCAMBIO: importando XMI:',
    archivo.name
  );

  this.intercambioService
    .importarXmi(
      this.modeloActual.idModelo,
      archivo
    )
    .subscribe({

      next: (resultado) => {

        console.log(
          'INTERCAMBIO: XMI importado correctamente:',
          resultado
        );

        this.elementos =
  (resultado.elementos ?? []) as any[];

this.relaciones =
  (resultado.relaciones ?? []) as any[];

        this.elementoSeleccionado =
          null;

        this.relacionSeleccionada =
          null;

        setTimeout(() => {

          this.inicializarJointJS();

          this.changeDetector.detectChanges();

        }, 0);

        alert(
          'Modelo XMI importado correctamente.'
        );
      },

      error: (error) => {

        console.error(
          'INTERCAMBIO: error importando XMI:',
          error
        );

        console.error(
          'Respuesta del servidor:',
          error.error
        );

        alert(
          'No se pudo importar el archivo XMI.'
        );
      }

    });

  input.value = '';
}
}