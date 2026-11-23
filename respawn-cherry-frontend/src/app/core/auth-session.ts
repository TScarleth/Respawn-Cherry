export interface UsuarioApi {
  id: number;
  nombre: string;
  correo: string;
  rol: 'USUARIO' | 'ADMIN';
}

export interface AuthSession {
  token: string;
  usuario: UsuarioApi;
}

const AUTH_KEY = 'respawn-cherry-auth';
const LEGACY_FORUM_KEY = 'respawn-cherry-forum-v1';

export function readAuthSession(): AuthSession | null {
  localStorage.removeItem(LEGACY_FORUM_KEY);
  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) return null;
  try {
    const parsed: unknown = JSON.parse(raw);
    if (typeof parsed !== 'object' || parsed === null) return null;
    const record = parsed as Partial<AuthSession>;
    const usuario = record.usuario;
    if (!record.token || !usuario || typeof usuario.id !== 'number' || (usuario.rol !== 'ADMIN' && usuario.rol !== 'USUARIO')) {
      return null;
    }
    return { token: record.token, usuario };
  } catch {
    return null;
  }
}

export function writeAuthSession(session: AuthSession): void {
  localStorage.setItem(AUTH_KEY, JSON.stringify(session));
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_KEY);
}

export function readAuthToken(): string | null {
  return readAuthSession()?.token ?? null;
}
