import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../app.routes';
import { writeAuthSession } from './auth-session';
import { ForumFacade } from './forum.facade';

describe('ForumFacade', () => {
  let facade: ForumFacade;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    facade = TestBed.inject(ForumFacade);
    http = TestBed.inject(HttpTestingController);
    flushForum(http);
  });

  afterEach(() => {
    http.match(() => true).forEach((request) => request.flush([]));
    http.verify();
    localStorage.clear();
  });

  it('keeps the role returned by the login service', () => {
    let role = '';
    facade.login('admin@ejemplo.dev', 'clave').subscribe((user) => {
      role = user.role;
    });
    const login = http.expectOne('http://127.0.0.1:8081/api/auth/login');
    expect(login.request.body).toEqual({ correo: 'admin@ejemplo.dev', password: 'clave' });
    login.flush({
      token: 'jwt-admin',
      tipo: 'Bearer',
      usuario: { id: 7, nombre: 'Admin real', correo: 'admin@ejemplo.dev', rol: 'ADMIN' }
    });
    flushForum(http);
    http.expectOne('http://127.0.0.1:8081/api/usuarios').flush([
      { id: 7, nombre: 'Admin real', correo: 'admin@ejemplo.dev', rol: 'ADMIN' }
    ]);
    expect(role).toBe('ADMIN');
    expect(facade.isAdmin()).toBe(true);
    expect(facade.currentUser()?.name).toBe('Admin real');
  });

  it('shows categories and posts from the forum service', () => {
    http.expectNone('http://127.0.0.1:8082/api/publicaciones');
    facade.refresh();
    http.expectOne('http://127.0.0.1:8082/api/categorias').flush([{ id: 3, nombre: 'Setups' }]);
    http.expectOne('http://127.0.0.1:8082/api/publicaciones').flush([{
      id: 11, titulo: 'Mi escritorio', contenido: 'Luces cálidas', categoriaId: 3, categoria: 'Setups', autorId: 7, fechaCreacion: '2026-01-02T10:00:00Z'
    }]);
    http.expectOne('http://127.0.0.1:8082/api/comentarios').flush([]);
    expect(facade.categories().map((category) => category.name)).toEqual(['Setups']);
    expect(facade.posts().map((post) => post.title)).toEqual(['Mi escritorio']);
    expect(facade.posts()[0].authorName).toBe('#7');
  });
});

describe('routes', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()]
    });
  });

  afterEach(() => {
    const http = TestBed.inject(HttpTestingController);
    http.match(() => true).forEach((request) => request.flush([]));
    localStorage.clear();
  });

  it('sends a guest from the forum to login', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/forum');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('keeps a regular user out of the admin panel', async () => {
    writeAuthSession({ token: 'jwt-user', usuario: { id: 2, nombre: 'Luna', correo: 'luna@ejemplo.dev', rol: 'USUARIO' } });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/forum');
  });

  it('opens the admin panel when the stored role is ADMIN', async () => {
    writeAuthSession({ token: 'jwt-admin', usuario: { id: 1, nombre: 'Admin', correo: 'admin@ejemplo.dev', rol: 'ADMIN' } });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/admin');
  });
});

function flushForum(http: HttpTestingController): void {
  http.match('http://127.0.0.1:8082/api/categorias').forEach((request) => request.flush([]));
  http.match('http://127.0.0.1:8082/api/publicaciones').forEach((request) => request.flush([]));
  http.match('http://127.0.0.1:8082/api/comentarios').forEach((request) => request.flush([]));
}
