import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./settings-placeholder.component').then(m => m.SettingsPlaceholderComponent) },
];
