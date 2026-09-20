import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { Auth } from '../../auth/services/auth';

import {
  CrearProyectoRequest,
  ProyectoService
} from '../../core/services/proyecto';

import { InvitacionService } from '../../core/services/invitacion';

import { Invitacion } from '../../auth/models/invitacion.model';

import { Proyecto } from './proyecto.model';


@Component({
  selector: 'app-dashboard',

  imports: [
    FormsModule
  ],

  templateUrl: './dashboard.html',

  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private readonly auth =
    inject(Auth);

  private readonly proyectoService =
    inject(ProyectoService);

  private readonly invitacionService =
    inject(InvitacionService);

  private readonly router =
    inject(Router);

  private readonly changeDetector =
    inject(ChangeDetectorRef);


  // =========================================================
  // USUARIO
  // =========================================================

  usuario =
    this.auth.obtenerUsuario();


  // =========================================================
  // PROYECTOS
  // =========================================================

  proyectos: Proyecto[] = [];


  // =========================================================
  // INVITACIONES
  // =========================================================

  invitaciones: Invitacion[] = [];

  mostrarBuzon = false;

  cargandoInvitaciones = false;

  procesandoInvitacion = false;

  mensajeInvitacion = '';


  // =========================================================
  // ESTADOS
  // =========================================================

  cargando = false;

  error = '';

  mostrarFormulario = false;

  creandoProyecto = false;

  errorCreacion = '';


  // =========================================================
  // NUEVO PROYECTO
  // =========================================================

  nuevoProyecto: CrearProyectoRequest = {
    nombre: '',
    descripcion: ''
  };


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarProyectos();

    this.cargarInvitaciones();

  }


  // =========================================================
  // CARGAR PROYECTOS
  // =========================================================

  cargarProyectos(): void {

    this.cargando = true;

    this.error = '';


    this.proyectoService
      .obtenerMisProyectos()

      .pipe(

        finalize(() => {

          this.cargando = false;

          console.log(
            'Carga de proyectos finalizada'
          );

          this.changeDetector.markForCheck();

        })

      )

      .subscribe({

        next: (proyectos) => {

          console.log(
            'Proyectos recibidos:',
            proyectos
          );

          this.proyectos = proyectos;

          this.changeDetector.markForCheck();

        },


        error: (error) => {

          console.error(
            'Error cargando proyectos:',
            error
          );


          if (error.status === 401) {

            this.auth.logout();

            return;

          }


          this.error =
            'No se pudieron cargar los proyectos';

          this.changeDetector.markForCheck();

        }

      });

  }


  // =========================================================
  // CARGAR INVITACIONES
  // =========================================================

  cargarInvitaciones(): void {

    const idUsuario =
      this.usuario?.idUsuario;


    if (!idUsuario) {

      console.warn(
        'No se encontró idUsuario para cargar invitaciones'
      );

      return;

    }


    this.cargandoInvitaciones = true;


    this.invitacionService

      .obtenerRecibidas(idUsuario)

      .pipe(

        finalize(() => {

          this.cargandoInvitaciones = false;

          this.changeDetector.markForCheck();

        })

      )

      .subscribe({

        next: (invitaciones) => {

          console.log(
            'Invitaciones recibidas:',
            invitaciones
          );


          this.invitaciones =
            invitaciones.filter(
              invitacion =>
                invitacion.estado === 'PENDIENTE'
            );


          this.changeDetector.markForCheck();

        },


        error: (error) => {

          console.error(
            'Error cargando invitaciones:',
            error
          );


          if (error.status === 401) {

            this.auth.logout();

            return;

          }


          this.invitaciones = [];

          this.changeDetector.markForCheck();

        }

      });

  }


  // =========================================================
  // BUZÓN
  // =========================================================

  toggleBuzon(): void {

    this.mostrarBuzon =
      !this.mostrarBuzon;


    if (this.mostrarBuzon) {

      this.cargarInvitaciones();

    }


    this.changeDetector.markForCheck();

  }


  // =========================================================
  // ACEPTAR INVITACIÓN
  // =========================================================

  aceptarInvitacion(
    invitacion: Invitacion
  ): void {

    if (this.procesandoInvitacion) {

      return;

    }


    this.procesandoInvitacion = true;

    this.mensajeInvitacion = '';


    this.invitacionService

      .aceptar(
        invitacion.idInvitacion
      )

      .pipe(

        finalize(() => {

          this.procesandoInvitacion = false;

          this.changeDetector.markForCheck();

        })

      )

      .subscribe({

        next: (respuesta) => {

          console.log(
            'Invitación aceptada:',
            respuesta
          );


          this.mensajeInvitacion =
            'Invitación aceptada correctamente';


          this.cargarInvitaciones();

          this.cargarProyectos();

          this.changeDetector.markForCheck();

        },


        error: (error) => {

          console.error(
            'Error aceptando invitación:',
            error
          );


          if (error.status === 401) {

            this.auth.logout();

            return;

          }


          if (error.status === 403) {

            this.mensajeInvitacion =
              'No tienes permiso para aceptar esta invitación';

          }
          else if (error.status === 404) {

            this.mensajeInvitacion =
              'La invitación ya no existe';

          }
          else {

            this.mensajeInvitacion =
              'No se pudo aceptar la invitación';

          }


          this.changeDetector.markForCheck();

        }

      });

  }


  // =========================================================
  // RECHAZAR INVITACIÓN
  // =========================================================

  rechazarInvitacion(
    invitacion: Invitacion
  ): void {

    if (this.procesandoInvitacion) {

      return;

    }


    this.procesandoInvitacion = true;

    this.mensajeInvitacion = '';


    this.invitacionService

      .rechazar(
        invitacion.idInvitacion
      )

      .pipe(

        finalize(() => {

          this.procesandoInvitacion = false;

          this.changeDetector.markForCheck();

        })

      )

      .subscribe({

        next: (respuesta) => {

          console.log(
            'Invitación rechazada:',
            respuesta
          );


          this.mensajeInvitacion =
            'Invitación rechazada correctamente';


          this.cargarInvitaciones();

          this.changeDetector.markForCheck();

        },


        error: (error) => {

          console.error(
            'Error rechazando invitación:',
            error
          );


          if (error.status === 401) {

            this.auth.logout();

            return;

          }


          if (error.status === 403) {

            this.mensajeInvitacion =
              'No tienes permiso para rechazar esta invitación';

          }
          else if (error.status === 404) {

            this.mensajeInvitacion =
              'La invitación ya no existe';

          }
          else {

            this.mensajeInvitacion =
              'No se pudo rechazar la invitación';

          }


          this.changeDetector.markForCheck();

        }

      });

  }


  // =========================================================
  // CREAR PROYECTO
  // =========================================================

  abrirFormulario(): void {

    this.errorCreacion = '';

    this.nuevoProyecto = {
      nombre: '',
      descripcion: ''
    };

    this.mostrarFormulario = true;

    this.changeDetector.markForCheck();

  }


  cerrarFormulario(): void {

    if (this.creandoProyecto) {

      return;

    }

    this.mostrarFormulario = false;

    this.changeDetector.markForCheck();

  }


  crearProyecto(): void {

    this.errorCreacion = '';


    const nombre =
      this.nuevoProyecto.nombre.trim();


    const descripcion =
      this.nuevoProyecto.descripcion.trim();


    if (!nombre) {

      this.errorCreacion =
        'El nombre del proyecto es obligatorio';

      this.changeDetector.markForCheck();

      return;

    }


    this.creandoProyecto = true;

    this.changeDetector.markForCheck();


    this.proyectoService

      .crearProyecto({

        nombre,
        descripcion

      })

      .subscribe({

        next: (proyecto) => {

          console.log(
            'Proyecto creado:',
            proyecto
          );


          this.creandoProyecto = false;

          this.mostrarFormulario = false;

          this.changeDetector.markForCheck();


          this.router.navigate([
            '/editor',
            proyecto.idProyecto
          ]);

        },


        error: (error) => {

          console.error(
            'Error creando proyecto:',
            error
          );


          this.creandoProyecto = false;


          if (error.status === 401) {

            this.auth.logout();

            return;

          }


          if (error.status === 400) {

            this.errorCreacion =
              'Los datos del proyecto no son válidos';

          }
          else if (error.status === 409) {

            this.errorCreacion =
              'Ya existe un proyecto con ese nombre';

          }
          else {

            this.errorCreacion =
              'No se pudo crear el proyecto';

          }


          this.changeDetector.markForCheck();

        }

      });

  }


  // =========================================================
  // ABRIR PROYECTO
  // =========================================================

  abrirProyecto(
    proyecto: Proyecto
  ): void {

    this.router.navigate([
      '/editor',
      proyecto.idProyecto
    ]);

  }


  // =========================================================
  // CERRAR SESIÓN
  // =========================================================

  cerrarSesion(): void {

    this.auth.logout();

  }


  // =========================================================
  // INICIO
  // =========================================================

  irInicio(): void {

    this.router.navigate([
      '/'
    ]);

  }

}