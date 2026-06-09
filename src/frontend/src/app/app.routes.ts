import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { selectTenantGuard } from './core/auth/select-tenant.guard';
import { LandingComponent } from './features/landing/landing.component';
import { LoginComponent } from './features/login/login.component';
import { SelectTenantComponent } from './features/select-tenant/select-tenant.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'select-tenant',
    component: SelectTenantComponent,
    canActivate: [selectTenantGuard],
  },
  {
    path: 'app',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'agenda', pathMatch: 'full' },
      {
        path: 'agenda',
        loadChildren: () => import('./features/agenda/agenda.routes').then(m => m.AGENDA_ROUTES),
      },
      {
        path: 'patients',
        loadChildren: () => import('./features/patients/patients.routes').then(m => m.PATIENTS_ROUTES),
      },
      {
        path: 'clinical',
        loadChildren: () => import('./features/clinical/clinical.routes').then(m => m.CLINICAL_ROUTES),
      },
      {
        path: 'reports',
        loadChildren: () => import('./features/reports/reports.routes').then(m => m.REPORTS_ROUTES),
      },
      {
        path: 'settings',
        loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES),
      },
      {
        path: 'unauthorized',
        loadComponent: () => import('./features/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
