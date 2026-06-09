import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/** Pantalla de acceso denegado por rol. */
@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="wrapper">
      <mat-icon class="icon">lock</mat-icon>
      <h2>Sin permiso</h2>
      <p>No tenés acceso a esta sección con tu rol actual.</p>
      <a mat-raised-button color="primary" routerLink="/app/agenda">Volver a inicio</a>
    </div>
  `,
  styles: [`
    .wrapper {
      display: flex; flex-direction: column; align-items: center;
      justify-content: center; min-height: 60vh; text-align: center; padding: 24px;
    }
    .icon { font-size: 4rem; height: 4rem; width: 4rem; color: #d32f2f; margin-bottom: 16px; }
  `],
})
export class UnauthorizedComponent {}
