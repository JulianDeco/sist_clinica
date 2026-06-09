import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

/** Shell principal de la aplicación autenticada. */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, MatSidenavModule, NavbarComponent, SidebarComponent],
  template: `
    <app-navbar />
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav mode="side" opened>
        <app-sidebar />
      </mat-sidenav>
      <mat-sidenav-content class="main-content">
        <router-outlet />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: calc(100vh - 64px); margin-top: 64px; }
    .main-content { padding: 24px; }
  `],
})
export class MainLayoutComponent {}
