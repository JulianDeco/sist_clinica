import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { tenantInterceptor } from './tenant.interceptor';
import { AuthService } from '../auth/auth.service';
import { TenantMembership } from '../auth/models/tenant-membership.model';
import { environment } from '../../../environments/environment';

describe('tenantInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  const stateSignal = signal<string>('ready');
  const tenantSignal = signal<TenantMembership | null>({
    tenantId: 't-1',
    tenantName: 'X',
    role: 'DOCTOR',
  });

  const fakeAuth = {
    authState: stateSignal,
    activeTenant: tenantSignal,
  };

  beforeEach(() => {
    stateSignal.set('ready');
    tenantSignal.set({ tenantId: 't-1', tenantName: 'X', role: 'DOCTOR' });

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tenantInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: fakeAuth },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('TC-04 / AC-06: adjunta X-Tenant-ID cuando READY y URL es apiBaseUrl', () => {
    http.get(`${environment.apiBaseUrl}/appointments`).subscribe();

    const req = controller.expectOne(`${environment.apiBaseUrl}/appointments`);
    expect(req.request.headers.get('X-Tenant-ID')).toBe('t-1');
    req.flush({});
  });

  it('TC-05 / AC-06: NO adjunta X-Tenant-ID cuando estado es IDENTITY_CONFIRMED', () => {
    stateSignal.set('identity_confirmed');
    http.get(`${environment.apiBaseUrl}/appointments`).subscribe();

    const req = controller.expectOne(`${environment.apiBaseUrl}/appointments`);
    expect(req.request.headers.has('X-Tenant-ID')).toBeFalse();
    req.flush({});
  });

  it('TC-08 / EC-02: NO adjunta X-Tenant-ID en URL externa', () => {
    http.get('https://external.example.com/data').subscribe();

    const req = controller.expectOne('https://external.example.com/data');
    expect(req.request.headers.has('X-Tenant-ID')).toBeFalse();
    req.flush({});
  });

  it('NO adjunta X-Tenant-ID cuando no hay tenant activo', () => {
    tenantSignal.set(null);
    http.get(`${environment.apiBaseUrl}/appointments`).subscribe();

    const req = controller.expectOne(`${environment.apiBaseUrl}/appointments`);
    expect(req.request.headers.has('X-Tenant-ID')).toBeFalse();
    req.flush({});
  });
});
