import { Routes } from '@angular/router';

export const AGENDA_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./agenda-placeholder.component').then(m => m.AgendaPlaceholderComponent) },
];
