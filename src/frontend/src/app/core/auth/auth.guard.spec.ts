import { TestBed } from '@angular/core/testing';
import { GuardResult, MaybeAsync, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let authSpy: jasmine.SpyObj<AuthService>;

  function runGuard(): MaybeAsync<GuardResult> {
    return TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
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

  it('TC-01 / AC-02: UNAUTHENTICATED → redirige a /', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('unauthenticated');
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/');
  });

  it('TC-02 / AC-03: IDENTITY_CONFIRMED → redirige a /select-tenant', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('identity_confirmed');
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/select-tenant');
  });

  it('READY → permite acceso (true)', () => {
    (authSpy.authState as jasmine.Spy).and.returnValue('ready');
    const result = runGuard();
    expect(result).toBeTrue();
  });
});
