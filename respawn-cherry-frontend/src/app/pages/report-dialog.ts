import { Component, OnDestroy, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ForumFacade, apiErrorMessage } from '../core/forum.facade';
import { inject } from '@angular/core';

const REASONS = [
  { id: 'EXPLICITO', label: 'Contenido explícito.' },
  { id: 'OFENSIVO', label: 'Vocabulario grosero u ofensivo.' },
  { id: 'PERTENENCIA', label: 'Cuenta que podría no cumplir los requisitos de pertenencia a nuestra comunidad exclusiva para mujeres.' },
  { id: 'OTRO', label: 'Otro.' }
] as const;

@Component({
  selector: 'app-report-dialog',
  imports: [FormsModule],
  template: `
    @if (thanks()) {
      <div class="farewell" [class.farewell-hide]="thanksHiding()" role="status">
        <div class="farewell-card"><p>Gracias por apoyar a nuestra comunidad con tu reporte:)</p></div>
      </div>
    } @else {
      <div class="report-modal" role="dialog" aria-modal="true" aria-labelledby="report-title">
        <form class="report-card" (ngSubmit)="send()">
          <h2 id="report-title">¿Por qué quieres reportar esta publicación?</h2>
          <div class="report-scroll">
            <div class="report-options">
              @for (reason of reasons; track reason.id) {
                <label><input type="radio" name="motivo" [value]="reason.id" [(ngModel)]="reasonId"> {{ reason.label }}</label>
              }
            </div>
            @if (reasonId === 'OTRO') {
              <label class="report-field">Explica el motivo
                <textarea name="explicacion" maxlength="300" [(ngModel)]="explanation"></textarea>
                <small>Quedan {{ 300 - explanation.length }} caracteres</small>
              </label>
            }
            <label class="report-field">Información adicional, si quieres contarla
              <textarea name="detalle" maxlength="500" [(ngModel)]="detail"></textarea>
              <small>Quedan {{ 500 - detail.length }} caracteres</small>
            </label>
            @if (error()) { <p class="form-error">{{ error() }}</p> }
          </div>
          <div class="form-actions">
            <button class="button button-quiet" type="button" (click)="closed.emit()">Cancelar</button>
            <button class="button button-primary" type="submit" [disabled]="sending()">Enviar denuncia</button>
          </div>
        </form>
      </div>
    }
  `
})
export class ReportDialog implements OnDestroy {
  readonly postId = input.required<string>();
  readonly closed = output<void>();
  private readonly facade = inject(ForumFacade);
  readonly reasons = REASONS;
  reasonId = '';
  explanation = '';
  detail = '';
  readonly error = signal('');
  readonly sending = signal(false);
  readonly thanks = signal(false);
  readonly thanksHiding = signal(false);
  private thanksTimer = 0;
  private hideTimer = 0;

  ngOnDestroy(): void {
    window.clearTimeout(this.thanksTimer);
    window.clearTimeout(this.hideTimer);
  }

  send(): void {
    this.error.set('');
    if (!this.reasonId) {
      this.error.set('Elige un motivo.');
      return;
    }
    if (this.reasonId === 'OTRO' && !this.explanation.trim()) {
      this.error.set('Cuéntanos el motivo cuando eliges Otro.');
      return;
    }
    this.sending.set(true);
    this.facade.report(this.postId(), this.reasonId, this.explanation, this.detail).subscribe({
      next: () => {
        this.sending.set(false);
        this.thanks.set(true);
        this.thanksTimer = window.setTimeout(() => {
          this.thanksHiding.set(true);
          this.hideTimer = window.setTimeout(() => this.closed.emit(), 460);
        }, 1600);
      },
      error: (err: HttpErrorResponse) => {
        this.sending.set(false);
        this.error.set(apiErrorMessage(err, 'No se pudo enviar la denuncia.'));
      }
    });
  }
}
