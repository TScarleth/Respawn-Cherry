import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForumFacade } from '../core/forum.facade';
import { countLabel } from '../core/labels';

@Component({
  selector: 'app-categories-page',
  imports: [RouterLink],
  template: `
    <div class="page-heading">
      <div><span class="eyebrow">ENCUENTRA TU ESPACIO</span><h1 class="category-title">Categorías</h1><p>Cinco rincones, una misma comunidad.</p></div>
    </div>
    <div class="category-directory">
      @for (category of facade.categories(); track category.id) {
        <a class="card directory-card" [routerLink]="['/forum']" [queryParams]="{ category: category.slug }">
          <span class="category-icon directory-icon" [style.color]="category.color">{{ category.icon }}</span>
          <span class="directory-copy"><strong>{{ category.name }}</strong><small>{{ category.description }}</small></span>
          <span class="directory-count">{{ countLabel(countFor(category.id), 'publicación', 'publicaciones') }} <b>→</b></span>
        </a>
      }
    </div>
  `,
  styles: [`
    .category-directory { display: grid; gap: 12px; }
    .directory-card { display: flex; align-items: center; gap: 16px; padding: 18px; transition: border-color .2s ease, transform .2s ease; }
    .directory-card:hover { border-color: #754255; transform: translateX(2px); }
    .directory-icon { width: 43px; height: 43px; flex: 0 0 auto; font-size: 19px; }
    .directory-copy { min-width: 0; flex: 1; display: grid; gap: 6px; }
    .directory-copy strong { font: 700 13px Manrope, sans-serif; }
    .directory-copy small { color: var(--muted); font-size: 10px; line-height: 1.5; }
    .directory-count { display: flex; align-items: center; gap: 12px; color: var(--subtle); font-size: 9px; white-space: nowrap; }
    .directory-count b { color: var(--cherry-light); font-size: 15px; }
    @media (max-width: 600px) { .directory-card { gap: 11px; padding: 14px; } .directory-count { font-size: 8px; gap: 6px; } .directory-copy small { font-size: 9px; } }
  `]
})
export class CategoriesPage {
  readonly facade = inject(ForumFacade);
  readonly countLabel = countLabel;

  countFor(categoryId: string): number {
    return this.facade.posts().filter((post) => post.categoryId === categoryId).length;
  }
}
