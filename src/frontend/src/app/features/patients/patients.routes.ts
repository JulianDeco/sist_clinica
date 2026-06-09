import { Routes } from '@angular/router';

export const PATIENTS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./patients-placeholder.component').then(m => m.PatientsPlaceholderComponent) },
];
