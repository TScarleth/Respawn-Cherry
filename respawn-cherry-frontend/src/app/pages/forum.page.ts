import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ForumFacade } from '../core/forum.facade';
import { countLabel } from '../core/labels';
import { ReportDialog } from './report-dialog';

@Component({
  selector: 'app-forum-page',
  imports: [DatePipe, RouterLink, ReportDialog],
  template: `
    <section class="hero-banner">
      <img class="hero-media" src="banner-respawn.gif?v=3" alt="">
      <div class="hero-overlay">
        <div class="hero-copy">
          <h1>ENTRE PARTIDAS NACEN HISTORIAS</h1>
          <p>tus juegos favoritos, nuevas amistades y conversaciones que nunca terminan</p>
        </div>
        <a routerLink="/new-post" class="button button-primary hero-cta">＋ Compartir algo</a>
      </div>
    </section>
    <div class="hero-stats">
      <span class="stat-item"><strong>{{ facade.memberCount() ?? '—' }}</strong> miembros</span>
      <span class="stat-item"><strong>{{ facade.posts().length }}</strong> conversaciones</span>
      <span class="stat-item"><i class="online-indicator"><i></i></i> comunidad activa</span>
    </div>

    <section>
      <div class="section-heading"><h2 class="jersey-title">sumérgete en<br>la comunidad</h2><a routerLink="/categories">Ver categorías →</a></div>
      <div class="category-grid">
        @for (category of facade.categories(); track category.id) {
          <a class="category-card" [routerLink]="['/forum']" [queryParams]="{ category: category.slug }">
            <span class="category-icon" [style.color]="category.color">{{ category.icon }}</span>
            <strong class="neon-title">{{ category.name }}</strong>
            <small>{{ countLabel(countFor(category.id), 'conversación', 'conversaciones') }}</small>
          </a>
        }
      </div>
    </section>

    <section id="conversaciones">
      <div class="section-heading"><h2 class="neon-title">{{ activeCategoryName() || 'Conversaciones recientes' }}</h2><span class="post-count">{{ filteredPosts().length }} publicaciones</span></div>
      <div class="feed-toolbar">
        <div class="feed-tabs">
          <button type="button" [class.active]="!activeCategory()" (click)="selectCategory('')">Todo</button>
          @for (category of facade.categories(); track category.id) {
            <button type="button" [class.active]="activeCategory() === category.slug" (click)="selectCategory(category.slug)">{{ category.name }}</button>
          }
        </div>
        <label class="search-wrap"><span>⌕</span><input class="search-input" [value]="search()" (input)="onSearch($event)" placeholder="Buscar publicaciones..." aria-label="Buscar publicaciones"></label>
      </div>

      <div class="post-list">
        @for (post of filteredPosts(); track post.id) {
          <article class="card post-card">
            <div class="post-topline">
              <span class="category-pill">{{ facade.getCategory(post.categoryId)?.name }}</span>
              <span>·</span><time [attr.datetime]="post.createdAt">{{ post.createdAt | date:'d MMM, HH:mm' }}</time>
            </div>
            <h3><a [routerLink]="['/posts', post.id]">{{ post.title }}</a></h3>
            <p class="post-excerpt">{{ post.body }}</p>
            @if (post.imageIds.length) {
              <div class="post-photos">
                @for (imageId of post.imageIds; track imageId) {
                  <img [src]="facade.imageUrl(post.id, imageId)" alt="">
                }
              </div>
            }
            <div class="post-footer">
              <span class="author-inline"><span class="avatar">@if (facade.authorPhoto(post.authorId); as photo) { <img [src]="photo" alt=""> } @else { {{ post.authorName.charAt(0) }} }</span>{{ post.authorName }}</span>
              <button class="heart" type="button" [class.liked]="post.liked" [attr.aria-pressed]="post.liked" (click)="like(post.id)">{{ post.liked ? '♥' : '♡' }} {{ post.likes }}</button>
              <button class="text-link report-link" type="button" (click)="reporting.set(post.id)">Reportar</button>
              <a class="post-actions" [routerLink]="['/posts', post.id]">{{ countLabel(facade.commentsFor(post.id).length, 'comentario', 'comentarios') }} <span>→</span></a>
            </div>
          </article>
        } @empty {
          @if (facade.loadError()) {
            <div class="empty-state"><strong>{{ facade.loadError() }}</strong></div>
          } @else {
            <div class="empty-state"><strong>No encontramos publicaciones</strong>Prueba otra búsqueda o sé la primera en compartir algo.</div>
          }
        }
      </div>
    </section>
    @if (reporting(); as postId) {
      <app-report-dialog [postId]="postId" (closed)="reporting.set('')" />
    }
  `
})
export class ForumPage {
  readonly facade = inject(ForumFacade);
  readonly countLabel = countLabel;
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly search = signal('');
  readonly reporting = signal('');
  readonly activeCategory = signal('');
  readonly activeCategoryName = computed(() =>
    this.facade.categories().find((category) => category.slug === this.activeCategory())?.name ?? ''
  );
  readonly filteredPosts = computed(() => {
    const query = this.search().trim().toLowerCase();
    const category = this.activeCategory();
    const source = query ? (this.facade.searchHits() ?? []) : this.facade.posts();
    return source.filter((post) => {
      const matchesCategory = !category || this.facade.getCategory(post.categoryId)?.slug === category;
      const matchesQuery = !query || `${post.title} ${post.body} ${post.authorName}`.toLowerCase().includes(query);
      return matchesCategory && matchesQuery;
    });
  });

  like(postId: string): void {
    if (!this.facade.currentUser()) {
      void this.router.navigate(['/login']);
      return;
    }
    this.facade.toggleLike(postId);
  }

  constructor() {
    if (this.route.snapshot.fragment !== 'conversaciones') window.scrollTo(0, 0);
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.activeCategory.set(params.get('category') ?? '');
      if (this.route.snapshot.fragment === 'conversaciones') this.scrollToPosts();
    });
  }

  private scrollToPosts(): void {
    const go = () => document.getElementById('conversaciones')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    requestAnimationFrame(() => requestAnimationFrame(go));
    window.setTimeout(go, 480);
  }

  selectCategory(slug: string): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: slug ? { category: slug } : {},
      replaceUrl: true
    });
  }

  onSearch(event: Event): void {
    const target = event.target;
    if (target instanceof HTMLInputElement) {
      this.search.set(target.value);
      this.facade.searchPosts(target.value);
    }
  }

  countFor(categoryId: string): number {
    return this.facade.posts().filter((post) => post.categoryId === categoryId).length;
  }
}
