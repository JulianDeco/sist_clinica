import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

/** Página pública de presentación del producto con CTA a /login. */
@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatCardModule],
  template: `
    <!-- Header -->
    <mat-toolbar color="primary" class="landing-header">
      <span class="brand">ClinicaSaaS</span>
      <span class="spacer"></span>
      <a mat-stroked-button routerLink="/login" class="login-btn">Iniciar sesión</a>
    </mat-toolbar>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1>Gestión clínica inteligente para tu consultorio</h1>
        <p class="subtitle">
          Agenda, historia clínica FHIR R4 y predicción de ausentismo
          en una sola plataforma. Diseñado para clínicas de 1 a 5 profesionales.
        </p>
        <div class="hero-actions">
          <a mat-raised-button color="primary" routerLink="/login">
            Comenzar ahora
          </a>
          <a mat-stroked-button color="primary" href="#features">
            Ver características
          </a>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features" id="features">
      <h2>Todo lo que tu clínica necesita</h2>
      <div class="features-grid">
        @for (feature of features; track feature.title) {
          <mat-card class="feature-card">
            <mat-card-content>
              <mat-icon class="feature-icon" color="primary">{{ feature.icon }}</mat-icon>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </mat-card-content>
          </mat-card>
        }
      </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
      <p>© 2026 ClinicaSaaS — Gestión clínica inteligente</p>
    </footer>
  `,
  styles: [`
    .landing-header { position: sticky; top: 0; z-index: 100; }
    .brand { font-weight: 700; font-size: 1.2rem; }
    .spacer { flex: 1; }
    .login-btn { border-color: white; color: white; }

    .hero {
      display: flex; align-items: center; justify-content: center;
      min-height: 70vh; padding: 48px 24px;
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      text-align: center;
    }
    .hero-content { max-width: 680px; }
    .hero h1 { font-size: 2.2rem; font-weight: 700; margin-bottom: 16px; color: #1565c0; }
    .subtitle { font-size: 1.1rem; color: #555; margin-bottom: 32px; line-height: 1.6; }
    .hero-actions { display: flex; gap: 16px; justify-content: center; flex-wrap: wrap; }

    .features { padding: 64px 24px; text-align: center; background: #fff; }
    .features h2 { font-size: 1.8rem; margin-bottom: 40px; color: #1565c0; }
    .features-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 24px; max-width: 960px; margin: 0 auto;
    }
    .feature-card { text-align: center; padding: 8px; }
    .feature-icon { font-size: 2.5rem; height: 2.5rem; width: 2.5rem; margin-bottom: 12px; }

    .landing-footer {
      padding: 24px; text-align: center; background: #1565c0; color: white;
    }
  `],
})
export class LandingComponent {
  readonly features = [
    {
      icon: 'calendar_today',
      title: 'Agenda inteligente',
      description: 'Gestión de turnos con validación de disponibilidad, coberturas y predicción de ausentismo.',
    },
    {
      icon: 'medical_services',
      title: 'Historia clínica FHIR',
      description: 'Registro SOAP estructurado con interoperabilidad FHIR R4 para compartir datos entre sistemas.',
    },
    {
      icon: 'insights',
      title: 'Predicción de ausentismo',
      description: 'Algoritmo heurístico que predice probabilidad de no-show y ajusta recordatorios automáticamente.',
    },
    {
      icon: 'business',
      title: 'Multitenant',
      description: 'Un solo acceso para trabajar en múltiples clínicas. Cambiá de consultorio sin cerrar sesión.',
    },
  ];
}
