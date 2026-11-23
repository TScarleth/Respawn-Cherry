import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { fieldError } from '../core/form-errors';
import { ForumFacade, apiErrorMessage } from '../core/forum.facade';

@Component({
  selector: 'app-create-post-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="page-heading">
      <div><span class="eyebrow">ABRE LA CONVERSACIÓN</span><h1 class="category-title">Nueva publicación</h1><p class="heading-wine">Comparte una idea, una pregunta o algo que te entusiasme.</p></div>
    </div>
    <form class="card form-card" [formGroup]="form" (ngSubmit)="submit()">
      <div class="form-field"><label for="post-title">Título</label><input id="post-title" formControlName="title" maxlength="150" placeholder="¿Qué tienes en mente?" [class.invalid]="form.controls.title.touched && form.controls.title.invalid">@if (fieldError(form.controls.title); as message) { <small class="field-error">{{ message }}</small> } @else { <small class="field-hint">Hasta 150 caracteres.</small> }</div>
      <div class="form-field"><label for="post-category">Categoría</label><select id="post-category" formControlName="categoryId" [class.invalid]="form.controls.categoryId.touched && form.controls.categoryId.invalid"><option value="" disabled>Elige dónde publicar</option>@for (category of facade.categories(); track category.id) { <option [value]="category.id">{{ category.name }}</option> }</select>@if (fieldError(form.controls.categoryId); as message) { <small class="field-error">{{ message }}</small> }</div>
      <div class="form-field"><label for="post-body">Tu publicación</label><textarea id="post-body" formControlName="body" maxlength="4000" placeholder="Cuéntanos un poco más..." [class.invalid]="form.controls.body.touched && form.controls.body.invalid"></textarea>@if (fieldError(form.controls.body); as message) { <small class="field-error">{{ message }}</small> } @else { <small class="field-hint">Hasta 4.000 caracteres. Mantengamos el espacio amable y respetuoso.</small> }</div>
      <div class="form-field">
        <label for="post-photos">Fotografías</label>
        <input id="post-photos" type="file" accept="image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp" multiple (change)="choosePhotos($event)">
        <small class="field-hint">Hasta 4 imágenes JPG, PNG o WebP, de 2 MB cada una.</small>
        @if (photos.length) {
          <div class="photo-preview">
            @for (photo of photos; track photo.url) {
              <figure>
                <img [src]="photo.url" alt="">
                <button type="button" (click)="removePhoto(photo)">Quitar</button>
              </figure>
            }
          </div>
        }
      </div>
      <div class="form-actions"><a class="button button-quiet" routerLink="/forum">Cancelar</a><button class="button button-primary" type="submit">Publicar →</button></div>
      @if (error) { <p class="form-error">{{ error }}</p> }
    </form>
  `
})
export class CreatePostPage implements OnDestroy {
  readonly facade = inject(ForumFacade);
  readonly fieldError = fieldError;
  error = '';
  photos: { file: File; url: string }[] = [];
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly router = inject(Router);
  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    categoryId: ['', Validators.required],
    body: ['', [Validators.required, Validators.maxLength(4000)]]
  });

  choosePhotos(event: Event): void {
    const input = event.target as HTMLInputElement;
    const selected = [...(input.files ?? [])];
    input.value = '';
    this.error = '';
    for (const file of selected) {
      if (this.photos.length >= 4) {
        this.error = 'Puedes adjuntar hasta 4 fotografías.';
        break;
      }
      const name = file.name.toLowerCase();
      const ok = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
        || name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.png') || name.endsWith('.webp');
      if (!ok) {
        this.error = 'Solo se aceptan imágenes JPG, PNG o WebP.';
        continue;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.error = 'Cada imagen puede pesar hasta 2 MB.';
        continue;
      }
      this.photos = [...this.photos, { file, url: URL.createObjectURL(file) }];
    }
  }

  removePhoto(photo: { file: File; url: string }): void {
    URL.revokeObjectURL(photo.url);
    this.photos = this.photos.filter((item) => item !== photo);
  }

  ngOnDestroy(): void {
    for (const photo of this.photos) URL.revokeObjectURL(photo.url);
  }

  submit(): void {
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { title, body, categoryId } = this.form.getRawValue();
    this.facade.createPost(title, body, categoryId, this.photos.map((photo) => photo.file)).subscribe({
      next: (post) => void this.router.navigate(['/posts', post.id]),
      error: (err: HttpErrorResponse) => {
        this.error = apiErrorMessage(err, 'No se pudo publicar. Revisa que hayas iniciado sesión.');
      }
    });
  }
}
