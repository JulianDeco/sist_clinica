import { Routes } from '@angular/router';

export const CLINICAL_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./clinical-placeholder.component').then(m => m.ClinicalPlaceholderComponent) },
];
