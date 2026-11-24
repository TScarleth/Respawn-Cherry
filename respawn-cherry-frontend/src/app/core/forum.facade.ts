import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, Injectable, inject, signal } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { AuthSession, UsuarioApi, clearAuthSession, readAuthSession, writeAuthSession } from './auth-session';
import { ForumCategory, ForumComment, ForumPost, ForumUser, UserRole } from './forum.models';

const USERS_API = 'http://127.0.0.1:8081';
const FORO_API = 'http://127.0.0.1:8082';

interface CategoriaApi {
  id: number;
  nombre: string;
}

interface PublicacionApi {
  id: number;
  titulo: string;
  contenido: string;
  categoriaId: number;
  categoria: string;
  autorId: number;
  fechaCreacion: string;
  likes?: number;
  meGusta?: boolean;
  imagenIds?: number[];
}

interface ComentarioApi {
  id: number;
  contenido: string;
  publicacionId: number;
  autorId: number;
  fechaCreacion: string;
}

export interface ReportItem {
  id: string;
  postId: string;
  title: string;
  authorId: string;
  reporterId: string;
  reason: string;
  explanation: string;
  detail: string;
  status: 'PENDIENTE' | 'REVISADA' | 'RESUELTA';
  createdAt: string;
}

interface DenunciaApi {
  id: number;
  publicacionId: number;
  titulo: string;
  autoraId: number;
  denuncianteId: number;
  motivo: string;
  explicacion: string | null;
  detalle: string | null;
  estado: ReportItem['status'];
  fechaCreacion: string;
}

interface LikeApi {
  likes: number;
  meGusta: boolean;
}

interface LoginApi {
  token: string;
  tipo: string;
  usuario: UsuarioApi;
}

const CATEGORY_STYLE: Record<string, Pick<ForumCategory, 'color' | 'icon' | 'description'>> = {
  videojuegos: { color: '#e65f83', icon: '◈', description: 'Lanzamientos, recomendaciones y conversaciones sobre tus juegos favoritos.' },
  'busca-tu-squad': { color: '#a979da', icon: '⌘', description: 'Encuentra compañeras y compañeros para tu próxima partida.' },
  setups: { color: '#72b6c4', icon: '▦', description: 'Espacios, periféricos y todo lo que hace único tu rincón gamer.' },
  'consejos-gamer': { color: '#d49b5c', icon: '✦', description: 'Guías, trucos y experiencias para subir de nivel.' },
  'cherry-lounge': { color: '#dc7090', icon: '♡', description: 'Un lugar tranquilo para conversar más allá del juego.' }
};

@Injectable({ providedIn: 'root' })
export class ForumFacade {
  private readonly http = inject(HttpClient);
  private readonly usersState = signal<ForumUser[]>([]);
  private readonly postsState = signal<ForumPost[]>([]);
  private readonly commentsState = signal<ForumComment[]>([]);
  private readonly categoriesState = signal<ForumCategory[]>([]);
  private readonly currentUserState = signal<ForumUser | null>(null);
  private readonly memberCountState = signal<number | null>(null);
  private readonly loadErrorState = signal('');
  private readonly reportsState = signal<ReportItem[]>([]);
  private rawPosts: PublicacionApi[] = [];
  private rawComments: ComentarioApi[] = [];

  readonly users = this.usersState.asReadonly();
  readonly posts = computed(() => [...this.postsState()].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
  readonly comments = this.commentsState.asReadonly();
  readonly categories = this.categoriesState.asReadonly();
  readonly currentUser = this.currentUserState.asReadonly();
  readonly isAdmin = computed(() => this.currentUserState()?.role === 'ADMIN');
  readonly memberCount = this.memberCountState.asReadonly();
  readonly loadError = this.loadErrorState.asReadonly();
  readonly reports = this.reportsState.asReadonly();
  private readonly searchState = signal<ForumPost[] | null>(null);
  private searchToken = 0;
  readonly searchHits = this.searchState.asReadonly();

  constructor() {
    const session = readAuthSession();
    if (session) this.currentUserState.set(toUser(session.usuario));
    this.refresh();
    if (session) this.refreshCurrentUser(session.usuario.id);
  }

  refresh(): void {
    forkJoin({
      categorias: this.http.get<CategoriaApi[]>(`${FORO_API}/api/categorias`),
      publicaciones: this.http.get<PublicacionApi[]>(`${FORO_API}/api/publicaciones`),
      comentarios: this.http.get<ComentarioApi[]>(`${FORO_API}/api/comentarios`)
    }).subscribe({
      next: (data) => {
        this.loadErrorState.set('');
        this.categoriesState.set(data.categorias.map(toCategory));
        this.rawPosts = data.publicaciones;
        this.rawComments = data.comentarios;
        this.publishContent();
      },
      error: () => this.loadErrorState.set('No pudimos cargar el foro. Revisa que foro-service esté activo en el puerto 8082.')
    });
  }

  login(correo: string, password: string): Observable<ForumUser> {
    return this.http.post<LoginApi>(`${USERS_API}/api/auth/login`, { correo: correo.trim(), password }).pipe(
      map((response) => this.adoptSession(response)),
      tap(() => this.refresh())
    );
  }

  register(nombre: string, correo: string, password: string): Observable<void> {
    return this.http.post<UsuarioApi>(`${USERS_API}/api/auth/registro`, {
      nombre: nombre.trim(),
      correo: correo.trim(),
      password
    }).pipe(map(() => undefined));
  }

  logout(): void {
    clearAuthSession();
    this.currentUserState.set(null);
    this.usersState.set([]);
    this.memberCountState.set(null);
    this.publishContent();
  }

  updateProfile(name: string, email: string): Observable<ForumUser> {
    const current = this.requireUser();
    return this.http.put<UsuarioApi>(`${USERS_API}/api/usuarios/${current.id}`, {
      nombre: name.trim(),
      correo: email.trim()
    }).pipe(map((usuario) => this.adoptUsuario(usuario)));
  }

  changePassword(currentPassword: string, nextPassword: string): Observable<void> {
    const current = this.requireUser();
    return this.http.post<LoginApi>(`${USERS_API}/api/auth/login`, { correo: current.email, password: currentPassword }).pipe(
      switchMap((response) => {
        this.storeSession(response);
        return this.http.put<UsuarioApi>(`${USERS_API}/api/usuarios/${current.id}`, { password: nextPassword });
      }),
      map((usuario) => {
        this.adoptUsuario(usuario);
      })
    );
  }

  createPost(title: string, body: string, categoryId: string, files: File[] = []): Observable<ForumPost> {
    return this.http.post<PublicacionApi>(`${FORO_API}/api/publicaciones`, {
      titulo: title.trim(),
      contenido: body.trim(),
      categoriaId: Number(categoryId)
    }).pipe(switchMap((created) => {
      if (!files.length) return of(created);
      const data = new FormData();
      for (const file of files) data.append('archivos', file, file.name);
      return this.http.post<number[]>(`${FORO_API}/api/publicaciones/${created.id}/imagenes`, data).pipe(
        map((ids) => ({ ...created, imagenIds: ids })),
        switchMap((withImages) => of(withImages)),
        tap({
          error: () => this.http.delete(`${FORO_API}/api/publicaciones/${created.id}`).subscribe()
        })
      );
    }), map((created) => {
      this.rawPosts = [created, ...this.rawPosts.filter((post) => post.id !== created.id)];
      this.publishContent();
      return this.postsState().find((post) => post.id === String(created.id))!;
    }));
  }

  searchPosts(titulo: string): void {
    const query = titulo.trim();
    const token = ++this.searchToken;
    if (!query) {
      this.searchState.set(null);
      return;
    }
    this.http.get<PublicacionApi[]>(`${FORO_API}/api/publicaciones`, { params: { titulo: query } }).subscribe({
      next: (items) => {
        if (token !== this.searchToken) return;
        const names = new Map(this.usersState().map((user) => [user.id, user.name]));
        this.searchState.set(items.map((post) => toPost(post, names)));
      }
    });
  }

  toggleLike(postId: string): void {
    this.http.post<LikeApi>(`${FORO_API}/api/publicaciones/${postId}/likes`, {}).subscribe({
      next: (result) => {
        this.rawPosts = this.rawPosts.map((post) => post.id === Number(postId)
          ? { ...post, likes: result.likes, meGusta: result.meGusta }
          : post);
        this.publishContent();
      }
    });
  }

  report(postId: string, motivo: string, explicacion: string, detalle: string): Observable<void> {
    return this.http.post(`${FORO_API}/api/denuncias`, {
      publicacionId: Number(postId),
      motivo,
      explicacion: explicacion.trim() || null,
      detalle: detalle.trim() || null
    }).pipe(map(() => undefined));
  }

  loadReports(): void {
    this.http.get<DenunciaApi[]>(`${FORO_API}/api/denuncias`).subscribe({
      next: (items) => this.reportsState.set(items.map(toReport))
    });
  }

  updateReport(id: string, estado: ReportItem['status']): void {
    this.http.patch<DenunciaApi>(`${FORO_API}/api/denuncias/${id}`, { estado }).subscribe({
      next: (item) => this.reportsState.update((items) => items.map((report) => report.id === id ? toReport(item) : report))
    });
  }

  imageUrl(postId: string, imageId: string): string {
    return `${FORO_API}/api/publicaciones/${postId}/imagenes/${imageId}`;
  }

  addComment(postId: string, body: string): Observable<void> {
    return this.http.post<ComentarioApi>(`${FORO_API}/api/publicaciones/${postId}/comentarios`, {
      contenido: body.trim()
    }).pipe(map((created) => {
      this.rawComments = [...this.rawComments.filter((comment) => comment.id !== created.id), created];
      this.publishContent();
    }));
  }

  removePost(postId: string): void {
    this.http.delete(`${FORO_API}/api/publicaciones/${postId}`).subscribe({
      next: () => {
        this.rawPosts = this.rawPosts.filter((post) => String(post.id) !== postId);
        this.rawComments = this.rawComments.filter((comment) => String(comment.publicacionId) !== postId);
        this.publishContent();
      }
    });
  }

  removeComment(commentId: string): void {
    this.http.delete(`${FORO_API}/api/comentarios/${commentId}`).subscribe({
      next: () => {
        this.rawComments = this.rawComments.filter((comment) => String(comment.id) !== commentId);
        this.publishContent();
      }
    });
  }

  updateUserRole(userId: string, role: UserRole): void {
    this.http.put<UsuarioApi>(`${USERS_API}/api/usuarios/${userId}`, { rol: role }).subscribe({
      next: (usuario) => this.remember(usuario)
    });
  }

  removeUser(userId: string): void {
    if (this.currentUserState()?.id === userId) return;
    this.http.delete(`${USERS_API}/api/usuarios/${userId}`).subscribe({
      next: () => this.usersState.update((users) => users.filter((user) => user.id !== userId))
    });
  }

  isSeedAccount(_email?: string): boolean {
    return false;
  }

  getPost(id: string): ForumPost | undefined {
    return this.postsState().find((post) => post.id === id);
  }

  getCategory(id: string): ForumCategory | undefined {
    return this.categoriesState().find((category) => category.id === id);
  }

  commentsFor(postId: string): ForumComment[] {
    return this.commentsState()
      .filter((comment) => comment.postId === postId)
      .sort((a, b) => a.createdAt.localeCompare(b.createdAt));
  }

  canManage(authorId: string): boolean {
    const user = this.currentUserState();
    return user !== null && (user.role === 'ADMIN' || user.id === authorId);
  }

  visibleCountry(_authorId?: string): string {
    return '';
  }

  authorPhoto(_authorId?: string): string {
    return '';
  }

  private refreshCurrentUser(id: number): void {
    this.http.get<UsuarioApi>(`${USERS_API}/api/usuarios/${id}`).subscribe({
      next: (usuario) => {
        this.adoptUsuario(usuario);
        if (usuario.rol === 'ADMIN') this.loadDirectory();
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) this.logout();
      }
    });
  }

  refreshDirectory(): void {
    if (this.currentUserState()?.role === 'ADMIN') this.loadDirectory();
  }

  private loadDirectory(): void {
    this.http.get<UsuarioApi[]>(`${USERS_API}/api/usuarios`).subscribe({
      next: (usuarios) => {
        this.memberCountState.set(usuarios.length);
        for (const usuario of usuarios) this.remember(usuario);
      }
    });
  }

  private adoptSession(response: LoginApi): ForumUser {
    this.storeSession(response);
    const user = this.adoptUsuario(response.usuario);
    if (user.role === 'ADMIN') this.loadDirectory();
    return user;
  }

  private storeSession(response: LoginApi): void {
    const session: AuthSession = { token: response.token, usuario: response.usuario };
    writeAuthSession(session);
  }

  private adoptUsuario(usuario: UsuarioApi): ForumUser {
    const session = readAuthSession();
    if (session) writeAuthSession({ ...session, usuario });
    const user = toUser(usuario);
    this.currentUserState.set(user);
    this.remember(usuario);
    return user;
  }

  private remember(usuario: UsuarioApi): void {
    const user = toUser(usuario);
    this.usersState.update((users) => {
      const next = users.filter((item) => item.id !== user.id);
      return [...next, user];
    });
    this.publishContent();
  }

  private publishContent(): void {
    const names = new Map(this.usersState().map((user) => [user.id, user.name]));
    this.postsState.set(this.rawPosts.map((post) => toPost(post, names)));
    this.commentsState.set(this.rawComments.map((comment) => toComment(comment, names)));
  }

  private requireUser(): ForumUser {
    const user = this.currentUserState();
    if (!user) throw new Error('Se requiere una sesión para realizar esta acción');
    return user;
  }
}

function toUser(usuario: UsuarioApi): ForumUser {
  return {
    id: String(usuario.id),
    name: usuario.nombre,
    email: usuario.correo,
    role: usuario.rol,
    joinedAt: ''
  };
}

function toCategory(categoria: CategoriaApi): ForumCategory {
  const slug = slugify(categoria.nombre);
  const style = CATEGORY_STYLE[slug] ?? { color: '#e65f83', icon: '◈', description: '' };
  return { id: String(categoria.id), slug, name: categoria.nombre, ...style };
}

function toPost(post: PublicacionApi, names: Map<string, string>): ForumPost {
  const authorId = String(post.autorId);
  return {
    id: String(post.id),
    authorId,
    authorName: names.get(authorId) ?? `#${authorId}`,
    categoryId: String(post.categoriaId),
    title: post.titulo,
    body: post.contenido,
    createdAt: post.fechaCreacion,
    likes: post.likes ?? 0,
    liked: post.meGusta ?? false,
    imageIds: (post.imagenIds ?? []).map(String)
  };
}

function toComment(comment: ComentarioApi, names: Map<string, string>): ForumComment {
  const authorId = String(comment.autorId);
  return {
    id: String(comment.id),
    postId: String(comment.publicacionId),
    authorId,
    authorName: names.get(authorId) ?? `#${authorId}`,
    body: comment.contenido,
    createdAt: comment.fechaCreacion
  };
}

function toReport(item: DenunciaApi): ReportItem {
  return {
    id: String(item.id),
    postId: String(item.publicacionId),
    title: item.titulo,
    authorId: String(item.autoraId),
    reporterId: String(item.denuncianteId),
    reason: item.motivo,
    explanation: item.explicacion ?? '',
    detail: item.detalle ?? '',
    status: item.estado,
    createdAt: item.fechaCreacion
  };
}

function slugify(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

export function apiErrorMessage(error: HttpErrorResponse, fallback: string): string {
  const body = error.error;
  if (body && typeof body === 'object' && 'mensaje' in body && typeof body.mensaje === 'string' && body.mensaje) {
    return body.mensaje;
  }
  return fallback;
}
