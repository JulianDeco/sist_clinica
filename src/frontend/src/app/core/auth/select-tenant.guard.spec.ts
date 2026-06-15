import { TestBed } from '@angular/core/testing';
import { GuardResult, MaybeAsync, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { selectTenantGuard } from './select-tenant.guard';
import { AuthService } from './auth.service';

describe('selectTenantGuard', () => {
  let authSpy: jasmine.SpyObj<AuthService>;

  function runGuard(): MaybeAsync<GuardResult> {
    return TestBed.runInInjectionContext(() => selectTenantGuard({} as any, {} as any));
  }

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['hasRole'], {
      authState: jasmine.createSpy().and.returnValue('unauthenticated'),
    });

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authSpy }],
    });
  });

  it('TC-12 / EC-07: READY → redirige a /app/agenda', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('ready');
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/app/agenda');
  });

  it('IDENTITY_CONFIRMED → permite acceso (true)', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('identity_confirmed');
    const result = runGuard();
    expect(result).toBeTrue();
  });

  it('UNAUTHENTICATED → redirige a /', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('unauthenticated');
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/');
  });
});
