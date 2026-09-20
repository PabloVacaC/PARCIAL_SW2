import {
  ChangeDetectorRef,
  Component,
  inject
} from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { HttpErrorResponse } from '@angular/common/http';

import { finalize } from 'rxjs';

import { Auth } from '../../services/auth';


@Component({
  selector: 'app-login',

  imports: [
    FormsModule
  ],

  templateUrl: './login.html',

  styleUrl: './login.css'
})
export class Login {

  private readonly auth =
    inject(Auth);

  private readonly router =
    inject(Router);

  private readonly changeDetector =
    inject(ChangeDetectorRef);


  // =========================================================
  // CAMPOS
  // =========================================================

  username = '';

  password = '';

  error = '';

  cargando = false;


  // =========================================================
  // LOGIN
  // =========================================================

  iniciarSesion(): void {

    // Limpiar error anterior
    this.error = '';

    this.changeDetector.detectChanges();


    // Validación básica
    if (
      !this.username.trim() ||
      !this.password
    ) {

      this.error =
        'Ingrese usuario y contraseña';

      this.changeDetector.detectChanges();

      return;

    }


    // Activar carga
    this.cargando = true;

    this.changeDetector.detectChanges();


    this.auth
      .login({
        username: this.username.trim(),
        password: this.password
      })

      .pipe(

        /*
         * Se ejecuta SIEMPRE:
         *
         * - login correcto
         * - 401
         * - 403
         * - error de conexión
         */
        finalize(() => {

          this.cargando = false;

          console.log(
            'Finalizó proceso de login'
          );

          this.changeDetector.detectChanges();

        })

      )

      .subscribe({

        // ===================================================
        // LOGIN CORRECTO
        // ===================================================

        next: () => {

          this.cargando = false;

          this.changeDetector.detectChanges();


          console.log(
            'Login correcto'
          );


          this.router.navigate([
            '/dashboard'
          ]);

        },


        // ===================================================
        // ERROR
        // ===================================================

        error: (
          error: HttpErrorResponse
        ) => {

          console.error(
            'Error de login:',
            error
          );


          // Asegurarnos de detener
          // el estado de carga
          this.cargando = false;


          if (error.status === 401) {

            this.error =
              'Usuario o contraseña incorrectos';

          }

          else if (error.status === 403) {

            this.error =
              'Usuario o contraseña incorrectos';

          }

          else if (error.status === 0) {

            this.error =
              'No se pudo conectar con el servidor';

          }

          else {

            this.error =
              'Ocurrió un error al iniciar sesión';

          }


          console.log(
            'Mensaje mostrado:',
            this.error
          );


          this.changeDetector.detectChanges();

        }

      });

  }


  // =========================================================
  // REGISTRO
  // =========================================================

  irRegistro(): void {

    this.router.navigate([
      '/registro'
    ]);

  }


  // =========================================================
  // VOLVER AL INICIO
  // =========================================================

  volverInicio(): void {

    this.router.navigate([
      '/'
    ]);

  }

}