import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let fakeAuth: { clearState: jasmine.Spy; isSwitchingTenant: boolean; isRedirecting: boolean };
  let routerSpy: jasmine.SpyObj<Router>;
  let snackSpy: jasmine.SpyObj<MatSnackBar>;
  const API_URL = `${environment.apiBaseUrl}/patients`;

  beforeEach(() => {
    fakeAuth = {
      clearState: jasmine.createSpy('clearState'),
      isSwitchingTenant: false,
      isRedirecting: false,
    };
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    snackSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: fakeAuth },
        { provide: Router, useValue: routerSpy },
        { provide: MatSnackBar, useValue: snackSpy },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('TC-06 / AC-07: 401 normal → clearState() y navega a /', () => {
    http.get(API_URL).subscribe({ error: () => {} });

    controller.expectOne(API_URL).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fakeAuth.clearState).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('TC-09 / EC-04: 401 con isRedirecting=true → no llama clearState ni navega', () => {
    fakeAuth.isRedirecting = true;
    http.get(API_URL).subscribe({ error: () => {} });

    controller.expectOne(API_URL).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fakeAuth.clearState).not.toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('TC-10 / EC-06: 401 con isSwitchingTenant=true → no llama clearState ni navega', () => {
    fakeAuth.isSwitchingTenant = true;
    http.get(API_URL).subscribe({ error: () => {} });

    controller.expectOne(API_URL).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fakeAuth.clearState).not.toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('401 en URL /auth/select-tenant → no llama clearState', () => {
    const selectUrl = `${environment.apiBaseUrl}/auth/select-tenant`;
    http.post(selectUrl, {}).subscribe({ error: () => {} });

    controller.expectOne(selectUrl).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fakeAuth.clearState).not.toHaveBeenCalled();
  });

  it('403 → snackbar "Sin permiso"', () => {
    http.get(API_URL).subscribe({ error: () => {} });

    controller.expectOne(API_URL).flush(null, { status: 403, statusText: 'Forbidden' });

    expect(snackSpy.open).toHaveBeenCalledWith('Sin permiso', 'Cerrar', { duration: 4000 });
  });

  it('500 → snackbar "Error del servidor"', () => {
    http.get(API_URL).subscribe({ error: () => {} });

    controller.expectOne(API_URL).flush(null, { status: 500, statusText: 'Server Error' });

    expect(snackSpy.open).toHaveBeenCalledWith('Error del servidor', 'Cerrar', { duration: 4000 });
  });
});
