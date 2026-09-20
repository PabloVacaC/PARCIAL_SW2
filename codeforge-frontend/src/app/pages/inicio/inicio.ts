import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.html',
  styleUrl: './inicio.css'
})
export class Inicio {

  constructor(private router: Router) {}

  irInicio(): void {
    this.router.navigate(['/']);
  }

  irLogin(): void {
    this.router.navigate(['/login']);
  }

  irRegistro(): void {
    this.router.navigate(['/registro']);
  }
}