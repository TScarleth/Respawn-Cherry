import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API = 'http://127.0.0.1:8081/api/auth';

@Injectable({ providedIn: 'root' })
export class PasswordRecoveryService {
  private readonly http = inject(HttpClient);

  request(correo: string): Observable<void> {
    return this.http.post<void>(`${API}/recuperar`, { correo });
  }

  reset(token: string, password: string): Observable<{ correo: string }> {
    return this.http.post<{ correo: string }>(`${API}/restablecer`, { token, password });
  }
}
