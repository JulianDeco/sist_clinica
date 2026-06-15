import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

/** Panel lateral de navegación con ítems por feature. */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule],
  template: `
    <mat-nav-list>
      @for (item of navItems; track item.route) {
        <a mat-list-item [routerLink]="item.route" routerLinkActive="active-link">
          <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
          <span matListItemTitle>{{ item.label }}</span>
        </a>
      }
    </mat-nav-list>
  `,
  styles: [
    `
      :host {
        display: block;
        width: 240px;
        height: 100%;
        padding-top: 64px;
      }
      .active-link {
        background: rgba(0, 0, 0, 0.06);
      }
    `,
  ],
})
export class SidebarComponent {
  /** Ítems de navegación del panel lateral, uno por feature. */
  readonly navItems: NavItem[] = [
    { label: 'Agenda', icon: 'calendar_today', route: '/app/agenda' },
    { label: 'Pacientes', icon: 'people', route: '/app/patients' },
    { label: 'Consultas', icon: 'medical_services', route: '/app/clinical' },
    { label: 'Reportes', icon: 'bar_chart', route: '/app/reports' },
    { label: 'Configuración', icon: 'settings', route: '/app/settings' },
  ];
}
