import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { fieldError } from '../core/form-errors';
import { ForumFacade } from '../core/forum.facade';
import { ReportDialog } from './report-dialog';

@Component({
  selector: 'app-post-page',
  imports: [DatePipe, ReactiveFormsModule, RouterLink, ReportDialog],
  template: `
    <div class="breadcrumbs"><a routerLink="/forum">Foro</a> <span> / </span> <a routerLink="/categories">Categorías</a> <span> / </span> Publicación</div>
    @if (post(); as currentPost) {
      <article class="card detail-card">
        <div class="post-topline"><span class="category-pill">{{ facade.getCategory(currentPost.categoryId)?.name }}</span><span>·</span><time [attr.datetime]="currentPost.createdAt">{{ currentPost.createdAt | date:'d MMMM y, HH:mm' }}</time></div>
        <h1 class="detail-title">{{ currentPost.title }}</h1>
        <div class="detail-meta"><span class="author-inline"><span class="avatar">@if (facade.authorPhoto(currentPost.authorId); as photo) { <img [src]="photo" alt=""> } @else { {{ currentPost.authorName.charAt(0) }} }</span>{{ currentPost.authorName }}</span><button class="heart" type="button" [class.liked]="currentPost.liked" [attr.aria-pressed]="currentPost.liked" (click)="like()">{{ currentPost.liked ? '♥' : '♡' }} {{ currentPost.likes }}</button><button class="text-link report-link" type="button" (click)="reporting.set(true)">Reportar</button></div>
        <p class="detail-copy">{{ currentPost.body }}</p>
        @if (currentPost.imageIds.length) {
          <div class="post-photos detail-photos">
            @for (imageId of currentPost.imageIds; track imageId) {
              <img [src]="facade.imageUrl(currentPost.id, imageId)" alt="">
            }
          </div>
        }
        @if (facade.canManage(currentPost.authorId)) {
          <button class="button button-danger" type="button" (click)="deletePost()">Eliminar publicación</button>
        }
      </article>

      <section class="comment-section">
        <h2>Conversación <span class="muted">({{ comments().length }})</span></h2>
        <form class="comment-form" [formGroup]="commentForm" (ngSubmit)="addComment()">
          <span class="avatar">@if (facade.currentUser()?.photo; as photo) { <img [src]="photo" alt=""> } @else { {{ facade.currentUser()?.name?.charAt(0) }} }</span>
          <textarea formControlName="body" placeholder="Escribe algo amable y útil..." aria-label="Tu comentario" [class.invalid]="commentForm.controls.body.touched && commentForm.controls.body.invalid"></textarea>
          <button class="button button-primary" type="submit">Comentar →</button>
        </form>
        @if (fieldError(commentForm.controls.body); as message) { <small class="field-error">{{ message }}</small> }
        <div class="comment-list">
          @for (comment of comments(); track comment.id) {
            <article class="card comment-card">
              <span class="avatar">@if (facade.authorPhoto(comment.authorId); as photo) { <img [src]="photo" alt=""> } @else { {{ comment.authorName.charAt(0) }} }</span>
              <div class="comment-body">
                <div class="comment-head"><strong>{{ comment.authorName }}@if (facade.visibleCountry(comment.authorId); as country) { <span class="comment-country">· {{ country }}</span> }</strong><span>{{ comment.createdAt | date:'d MMM, HH:mm' }}</span></div>
                <p>{{ comment.body }}</p>
                @if (facade.canManage(comment.authorId)) {
                  <button class="button button-quiet" type="button" (click)="deleteComment(comment.id)">Eliminar</button>
                }
              </div>
            </article>
          } @empty {
            <div class="empty-state">Todavía no hay comentarios. ¡Inicia la conversación!</div>
          }
        </div>
      </section>
      @if (reporting()) {
        <app-report-dialog [postId]="currentPost.id" (closed)="reporting.set(false)" />
      }
    } @else {
      <div class="empty-state"><strong>No encontramos esa publicación</strong><a class="text-link" routerLink="/forum">Volver al foro →</a></div>
    }
  `
})
export class PostPage {
  readonly facade = inject(ForumFacade);
  readonly fieldError = fieldError;
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly postId = toSignal(
    this.route.paramMap.pipe(map((params) => params.get('id') ?? '')),
    { initialValue: this.route.snapshot.paramMap.get('id') ?? '' }
  );
  readonly post = computed(() => this.facade.getPost(this.postId()));
  readonly comments = computed(() => this.facade.commentsFor(this.postId()));
  readonly reporting = signal(false);
  readonly commentForm = this.fb.group({ body: ['', [Validators.required, Validators.maxLength(2000)]] });

  like(): void {
    if (!this.facade.currentUser()) {
      void this.router.navigate(['/login']);
      return;
    }
    this.facade.toggleLike(this.postId());
  }

  addComment(): void {
    if (this.commentForm.invalid || !this.post()) {
      this.commentForm.markAllAsTouched();
      return;
    }
    this.facade.addComment(this.postId(), this.commentForm.controls.body.value).subscribe({
      next: () => this.commentForm.reset()
    });
  }

  deleteComment(id: string): void {
    this.facade.removeComment(id);
  }

  deletePost(): void {
    this.facade.removePost(this.postId());
    void this.router.navigateByUrl('/forum');
  }
}
