import { TestBed } from '@angular/core/testing';
import { GuardResult, MaybeAsync, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { roleGuard } from './role.guard';
import { AuthService } from './auth.service';

describe('roleGuard', () => {
  let authSpy: jasmine.SpyObj<AuthService>;

  function runGuard(requiredRole: string): MaybeAsync<GuardResult> {
    return TestBed.runInInjectionContext(() => roleGuard(requiredRole)({} as any, {} as any));
  }

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['hasRole']);

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authSpy }],
    });
  });

  it('TC-07 / AC-08: rol no coincide → redirige a /app/unauthorized', () => {
    authSpy.hasRole.and.returnValue(false);
    const result = runGuard('DOCTOR');
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/app/unauthorized');
  });

  it('rol coincide → permite acceso (true)', () => {
    authSpy.hasRole.and.returnValue(true);
    const result = runGuard('DOCTOR');
    expect(result).toBeTrue();
  });
});
