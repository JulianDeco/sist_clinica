import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/** Componente raíz — solo renderiza el router outlet. */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class AppComponent {}
