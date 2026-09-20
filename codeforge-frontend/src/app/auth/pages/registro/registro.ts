import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-registro',
  imports: [FormsModule],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class Registro {

  private readonly auth = inject(Auth);
  private readonly router = inject(Router);


  nombre = '';
  apellido = '';
  username = '';
  correo = '';
  password = '';
  confirmPassword = '';

  error = '';
  exito = '';
  cargando = false;


  registrar(): void {

    this.error = '';
    this.exito = '';


    if (
      !this.nombre ||
      !this.apellido ||
      !this.username ||
      !this.correo ||
      !this.password ||
      !this.confirmPassword
    ) {

      this.error =
        'Complete todos los campos';

      return;
    }


    if (this.password.length < 6) {

      this.error =
        'La contraseña debe tener al menos 6 caracteres';

      return;
    }


    if (this.password !== this.confirmPassword) {

      this.error =
        'Las contraseñas no coinciden';

      return;
    }


    this.cargando = true;


    this.auth.registrar({

      nombre: this.nombre.trim(),

      apellido: this.apellido.trim(),

      username: this.username.trim(),

      correo: this.correo.trim(),

      password: this.password

    }).subscribe({

      next: () => {

        this.cargando = false;

        this.exito =
          'Cuenta creada correctamente. Redirigiendo...';


        setTimeout(() => {

          this.router.navigate(['/login']);

        }, 1200);

      },


      error: (error) => {

        this.cargando = false;


        if (error.status === 409) {

          this.error =
            'El usuario o correo ya está registrado';

        }
        else if (error.status === 400) {

          this.error =
            'Los datos enviados no son válidos';

        }
        else {

          this.error =
            'No se pudo crear la cuenta';

        }

      }

    });

  }


  irLogin(): void {

    this.router.navigate(['/login']);

  }


  volverInicio(): void {

    this.router.navigate(['/']);

  }

}