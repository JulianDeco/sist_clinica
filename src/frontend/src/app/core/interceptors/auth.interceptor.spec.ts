import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let fakeAuth: Partial<AuthService>;

  beforeEach(() => {
    fakeAuth = { accessToken: null };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: fakeAuth },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('TC-03 / AC-05: adjunta Authorization cuando hay accessToken y la URL es apiBaseUrl', () => {
    fakeAuth.accessToken = 'test-token';
    http.get(`${environment.apiBaseUrl}/patients`).subscribe();

    const req = controller.expectOne(`${environment.apiBaseUrl}/patients`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush({});
  });

  it('TC-08 / EC-02: NO adjunta header en URL externa', () => {
    fakeAuth.accessToken = 'test-token';
    http.get('https://external.example.com/data').subscribe();

    const req = controller.expectOne('https://external.example.com/data');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('no adjunta header cuando accessToken es null', () => {
    fakeAuth.accessToken = null;
    http.get(`${environment.apiBaseUrl}/patients`).subscribe();

    const req = controller.expectOne(`${environment.apiBaseUrl}/patients`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
