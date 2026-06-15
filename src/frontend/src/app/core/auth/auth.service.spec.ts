import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthApi } from '../api/auth.api';
import { TenantMembership } from './models/tenant-membership.model';

const TENANT_A: TenantMembership = { tenantId: 't-1', tenantName: 'Clínica A', role: 'DOCTOR' };
const TENANT_B: TenantMembership = { tenantId: 't-2', tenantName: 'Clínica B', role: 'ADMIN' };

describe('AuthService', () => {
  let service: AuthService;
  let apiSpy: jasmine.SpyObj<AuthApi>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj('AuthApi', ['login', 'selectTenant', 'switchTenant', 'logout']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    apiSpy.logout.and.returnValue(of(undefined as unknown as void));

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthApi, useValue: apiSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
    service = TestBed.inject(AuthService);
  });

  /** Lleva el servicio al estado READY con TENANT_A activo. */
  function reachReady(): Promise<void> {
    apiSpy.login.and.returnValue(of({ identityToken: 'id-tok', tenants: [TENANT_A, TENANT_B] }));
    apiSpy.selectTenant.and.returnValue(of({ accessToken: 'acc-tok-a' }));
    return new Promise<void>((resolve) =>
      service
        .login({ email: 'a@b.com', password: '123' })
        .subscribe(() => service.selectTenant(TENANT_A.tenantId).subscribe(() => resolve())),
    );
  }

  it('estado inicial es unauthenticated', () => {
    expect(service.authState()).toBe('unauthenticated');
    expect(service.currentUser()).toBeNull();
    expect(service.availableTenants()).toEqual([]);
    expect(service.activeTenant()).toBeNull();
    expect(service.accessToken).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  describe('login() con múltiples tenants', () => {
    it('TC-01: pasa a identity_confirmed y carga los tenants', (done) => {
      apiSpy.login.and.returnValue(of({ identityToken: 'id-tok', tenants: [TENANT_A, TENANT_B] }));

      service.login({ email: 'a@b.com', password: '123' }).subscribe(() => {
        expect(service.authState()).toBe('identity_confirmed');
        expect(service.availableTenants()).toEqual([TENANT_A, TENANT_B]);
        done();
      });
    });
  });

  describe('login() con un solo tenant', () => {
    it('TC-02 / AC-04: llama selectTenant() automáticamente y pasa a READY', (done) => {
      apiSpy.login.and.returnValue(of({ identityToken: 'id-tok', tenants: [TENANT_A] }));
      apiSpy.selectTenant.and.returnValue(of({ accessToken: 'acc-tok' }));

      service.login({ email: 'a@b.com', password: '123' }).subscribe(() => {
        expect(service.authState()).toBe('ready');
        expect(service.activeTenant()).toEqual(TENANT_A);
        expect(service.accessToken).toBe('acc-tok');
        done();
      });
    });
  });

  describe('selectTenant()', () => {
    it('TC-03: pasa a READY, setea accessToken y activeTenant', (done) => {
      apiSpy.login.and.returnValue(of({ identityToken: 'id-tok', tenants: [TENANT_A, TENANT_B] }));
      apiSpy.selectTenant.and.returnValue(of({ accessToken: 'acc-tok' }));

      service.login({ email: 'a@b.com', password: '123' }).subscribe(() => {
        service.selectTenant(TENANT_A.tenantId).subscribe(() => {
          expect(service.authState()).toBe('ready');
          expect(service.accessToken).toBe('acc-tok');
          expect(service.activeTenant()).toEqual(TENANT_A);
          done();
        });
      });
    });
  });

  describe('switchTenant()', () => {
    it('cambia el tenant activo y rota el accessToken', async () => {
      await reachReady();
      apiSpy.switchTenant.and.returnValue(of({ accessToken: 'acc-tok-b' }));

      await new Promise<void>((resolve) =>
        service.switchTenant(TENANT_B.tenantId).subscribe(() => resolve()),
      );

      expect(service.activeTenant()).toEqual(TENANT_B);
      expect(service.accessToken).toBe('acc-tok-b');
      expect(service.isSwitchingTenant).toBeFalse();
    });

    it('resetea isSwitchingTenant en error', async () => {
      await reachReady();
      apiSpy.switchTenant.and.returnValue(throwError(() => new Error('fail')));

      await new Promise<void>((resolve) =>
        service.switchTenant(TENANT_B.tenantId).subscribe({ error: () => resolve() }),
      );

      expect(service.isSwitchingTenant).toBeFalse();
    });
  });

  describe('logout()', () => {
    it('limpia todo el estado y navega a /', () => {
      service.logout();
      expect(service.authState()).toBe('unauthenticated');
      expect(service.accessToken).toBeNull();
      expect(service.activeTenant()).toBeNull();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });
  });

  describe('clearState()', () => {
    it('resetea todos los campos', () => {
      service.isRedirecting = true;
      service.isSwitchingTenant = true;
      service.clearState();
      expect(service.authState()).toBe('unauthenticated');
      expect(service.isRedirecting).toBeFalse();
      expect(service.isSwitchingTenant).toBeFalse();
    });
  });

  describe('hasRole()', () => {
    it('devuelve true si el rol del tenant activo coincide', () => {
      apiSpy.login.and.returnValue(of({ identityToken: 'id-tok', tenants: [TENANT_A] }));
      apiSpy.selectTenant.and.returnValue(of({ accessToken: 'acc-tok' }));

      service.login({ email: 'a@b.com', password: '123' }).subscribe(() => {
        expect(service.hasRole('DOCTOR')).toBeTrue();
        expect(service.hasRole('ADMIN')).toBeFalse();
      });
    });

    it('devuelve false sin tenant activo', () => {
      expect(service.hasRole('DOCTOR')).toBeFalse();
    });
  });
});
