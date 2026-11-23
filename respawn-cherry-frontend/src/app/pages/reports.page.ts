import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForumFacade, ReportItem } from '../core/forum.facade';

const REASONS: Record<string, string> = {
  EXPLICITO: 'Contenido explícito',
  OFENSIVO: 'Vocabulario grosero u ofensivo',
  PERTENENCIA: 'Posible cuenta fuera de la comunidad',
  OTRO: 'Otro'
};

const STATUSES: ReportItem['status'][] = ['PENDIENTE', 'REVISADA', 'RESUELTA'];

@Component({
  selector: 'app-reports-page',
  imports: [DatePipe, RouterLink],
  template: `
    @if (!embedded()) {
      <div class="page-heading">
        <div><span class="eyebrow">SOLO ADMINISTRACIÓN</span><h1 class="category-title">Denuncias</h1><p>Estas denuncias solo las ves tú.</p></div>
      </div>
    }
    <div class="report-filters">
      <button type="button" [class.active]="status() === 'TODAS'" (click)="status.set('TODAS')">Todas</button>
      @for (item of statuses; track item) {
        <button type="button" [class.active]="status() === item" (click)="status.set(item)">{{ item }}</button>
      }
    </div>
    <div class="report-list">
      @for (report of visible(); track report.id) {
        <article class="card report-item">
          <header>
            <strong>{{ report.title }}</strong>
            <span>{{ reason(report.reason) }}</span>
          </header>
          <p>Publicación #{{ report.postId }} · autora {{ name(report.authorId) }} · denunció {{ name(report.reporterId) }}</p>
          <p>{{ report.createdAt | date:'d MMM y, HH:mm' }} · {{ report.status }}</p>
          <p>{{ report.explanation || 'Sin explicación adicional.' }}</p>
          @if (report.detail) { <p>{{ report.detail }}</p> }
          <div class="form-actions">
            <a class="button button-quiet" [routerLink]="['/posts', report.postId]">Ver publicación</a>
            @if (report.status !== 'REVISADA') {
              <button class="button button-quiet" type="button" (click)="facade.updateReport(report.id, 'REVISADA')">Marcar revisada</button>
            }
            @if (report.status !== 'RESUELTA') {
              <button class="button button-primary" type="button" (click)="facade.updateReport(report.id, 'RESUELTA')">Marcar resuelta</button>
            }
          </div>
        </article>
      } @empty {
        <div class="empty-state"><strong>No hay denuncias en este estado</strong></div>
      }
    </div>
  `,
  styles: [`
    .report-filters { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 14px; }
    .report-filters button { border: 1px solid #6d2944; background: transparent; color: #ffb3d0; border-radius: 999px; padding: 6px 12px; font-size: 11px; font-weight: 700; cursor: pointer; }
    .report-filters button.active { background: #ffb3d0; color: #3a1020; }
    .report-list { display: grid; gap: 12px; }
    .report-item { padding: 16px 18px; display: grid; gap: 8px; }
    .report-item header { display: flex; justify-content: space-between; gap: 12px; }
    .report-item p { margin: 0; color: #f5eef2; font-size: 13px; line-height: 1.5; }
    .report-item span { color: #ffb3d0; font-size: 12px; font-weight: 700; }
  `]
})
export class ReportsPage implements OnInit {
  readonly embedded = input(false);
  readonly facade = inject(ForumFacade);
  readonly statuses = STATUSES;
  readonly status = signal<'TODAS' | ReportItem['status']>('TODAS');
  readonly visible = computed(() => {
    const selected = this.status();
    const items = this.facade.reports();
    return selected === 'TODAS' ? items : items.filter((report) => report.status === selected);
  });

  ngOnInit(): void {
    this.facade.refreshDirectory();
    this.facade.loadReports();
  }

  reason(value: string): string {
    return REASONS[value] ?? value;
  }

  name(id: string): string {
    return this.facade.users().find((user) => user.id === id)?.name ?? `#${id}`;
  }
}
