import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ForumFacade } from '../core/forum.facade';
import { ReportsPage } from './reports.page';

@Component({
  selector: 'app-admin-page',
  imports: [DatePipe, ReportsPage],
  template: `
    <div class="page-heading"><div><span class="eyebrow">SOLO ADMINISTRACIÓN</span><h1 class="category-title">Panel de administración</h1><p class="heading-wine">Denuncias, cuentas y contenido de Respawn Cherry.</p></div></div>
    <section class="admin-reports">
      <div class="section-heading"><h2 class="neon-title">Denuncias</h2></div>
      <app-reports-page [embedded]="true" />
    </section>
    <div class="admin-stats">
      <article class="card admin-stat"><span>Miembros</span><strong>{{ facade.users().length }}</strong></article>
      <article class="card admin-stat"><span>Publicaciones</span><strong>{{ facade.posts().length }}</strong></article>
      <article class="card admin-stat"><span>Comentarios</span><strong>{{ facade.comments().length }}</strong></article>
    </div>

    <section class="card admin-block">
      <div class="section-heading"><h2>Miembros de la comunidad</h2><span class="muted">{{ facade.users().length }} cuentas</span></div>
      @for (user of facade.users(); track user.id) {
        <div class="admin-list-item">
          <span class="avatar">{{ user.name.charAt(0) }}</span>
          <div><strong>{{ user.name }}</strong><small>{{ user.email }} · {{ user.joinedAt | date:'d MMM y' }}</small></div>
          @if (user.id === facade.currentUser()?.id) {
            <span class="role-pill">{{ user.role }}</span>
          } @else {
            <button class="button button-quiet" type="button" (click)="toggleRole(user.id, user.role)">{{ user.role === 'ADMIN' ? 'Quitar ADMIN' : 'Hacer ADMIN' }}</button>
            @if (!facade.isSeedAccount(user.email)) {
              <button class="button button-danger" type="button" (click)="removeUser(user.id)">Eliminar</button>
            }
          }
        </div>
      }
    </section>

    <section class="card admin-block">
      <div class="section-heading"><h2>Publicaciones</h2><span class="muted">Moderación del foro</span></div>
      @for (post of facade.posts(); track post.id) {
        <div class="admin-list-item"><div><strong>{{ post.title }}</strong><small>{{ post.authorName }} · {{ facade.getCategory(post.categoryId)?.name }}</small></div><button class="button button-danger" type="button" (click)="facade.removePost(post.id)">Eliminar</button></div>
      } @empty { <p class="muted">No hay publicaciones.</p> }
    </section>

    <section class="card admin-block">
      <div class="section-heading"><h2>Comentarios</h2><span class="muted">Contenido reciente</span></div>
      @for (comment of facade.comments(); track comment.id) {
        <div class="admin-list-item"><div><strong>{{ comment.body }}</strong><small>{{ comment.authorName }}</small></div><button class="button button-danger" type="button" (click)="facade.removeComment(comment.id)">Eliminar</button></div>
      } @empty { <p class="muted">No hay comentarios.</p> }
    </section>
  `
})
export class AdminPage {
  readonly facade = inject(ForumFacade);

  toggleRole(userId: string, role: 'USUARIO' | 'ADMIN'): void {
    this.facade.updateUserRole(userId, role === 'ADMIN' ? 'USUARIO' : 'ADMIN');
  }

  removeUser(userId: string): void {
    this.facade.removeUser(userId);
  }
}
