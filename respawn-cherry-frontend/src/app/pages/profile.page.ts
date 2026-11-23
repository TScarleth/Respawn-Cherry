import { HttpErrorResponse } from '@angular/common/http';
import { Component, effect, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { fieldError } from '../core/form-errors';
import { ForumFacade, apiErrorMessage } from '../core/forum.facade';
import { passwordRules } from '../core/password-policy';

function matchingPasswords(control: AbstractControl): ValidationErrors | null {
  return control.get('password')?.value === control.get('confirmPassword')?.value ? null : { passwordMismatch: true };
}

function optionalPhone(control: AbstractControl): { phone: true } | null {
  const value = String(control.value ?? '').trim();
  if (!value) return null;
  return /^\+?[\d\s()-]{8,20}$/.test(value) ? null : { phone: true };
}

@Component({
  selector: 'app-profile-page',
  imports: [ReactiveFormsModule],
  template: `
    <div class="page-heading"><div><span class="eyebrow">TU ESPACIO</span><h1 class="category-title">Mi perfil</h1><p>Administra cómo apareces en Respawn Cherry.</p></div></div>
    @if (facade.currentUser(); as user) {
      <div class="profile-grid">
        <section class="card profile-summary">
          <label class="profile-photo-picker">
            <span class="avatar">@if (photo) { <img [src]="photo" alt=""> } @else { {{ user.name.charAt(0).toUpperCase() }} }</span>
            <input type="file" accept="image/*" (change)="onPhoto($event)" aria-label="Subir foto de perfil">
            <span class="avatar-upload-label">Subir foto</span>
          </label>
          @if (photo) { <button class="avatar-upload-label" type="button" (click)="photo = ''">Quitar foto</button> }
          <div>
            <h2>{{ user.name }}</h2>
            <p>{{ user.email }}</p>
            @if (user.country) { <p>{{ user.country }}</p> }
            @if (user.phone) { <p>{{ user.phone }}</p> }
            @if (user.bio) { <p>{{ user.bio }}</p> }
            <span class="role-pill">{{ user.role }}</span>
          </div>
        </section>
        <div class="profile-forms">
        <form class="card form-card" [formGroup]="form" (ngSubmit)="save()">
          <div class="section-heading"><h2>Datos de la cuenta</h2></div>
          <div class="form-field"><label for="profile-name">Nombre gamer</label><input id="profile-name" formControlName="name" maxlength="100" [class.invalid]="form.controls.name.touched && form.controls.name.invalid">@if (fieldError(form.controls.name); as message) { <small class="field-error">{{ message }}</small> }</div>
          <div class="form-field"><label for="profile-email">Correo electrónico</label><input id="profile-email" type="email" formControlName="email" maxlength="150" [class.invalid]="form.controls.email.touched && form.controls.email.invalid">@if (fieldError(form.controls.email); as message) { <small class="field-error">{{ message }}</small> }</div>
          <div class="form-field"><label for="profile-phone">Teléfono</label><input id="profile-phone" type="tel" formControlName="phone" maxlength="20" placeholder="+56 9 1234 5678" [class.invalid]="form.controls.phone.touched && form.controls.phone.invalid">@if (fieldError(form.controls.phone); as message) { <small class="field-error">{{ message }}</small> }</div>
          <div class="form-field"><label for="profile-country">País</label><input id="profile-country" formControlName="country" maxlength="60" placeholder="Chile"><label class="check-line"><input type="checkbox" formControlName="showCountry"> Visualizar en el foro</label><small class="field-hint">Si lo marcas, tu país aparece junto a tu nombre en los comentarios.</small></div>
          <div class="form-field"><label for="profile-bio">Descripción</label><textarea id="profile-bio" class="profile-bio" formControlName="bio" maxlength="300" placeholder="Cuéntanos un poco de ti" [class.invalid]="form.controls.bio.touched && form.controls.bio.invalid"></textarea>@if (fieldError(form.controls.bio); as message) { <small class="field-error">{{ message }}</small> }</div>
          @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
          @if (success) { <div class="form-success" role="status">{{ success }}</div> }
          <div class="form-actions"><button class="button button-primary" type="submit">Guardar cambios</button></div>
        </form>
        <form class="card form-card" [formGroup]="passwordForm" (ngSubmit)="savePassword()">
          <div class="section-heading"><h2>Cambiar contraseña</h2></div>
          <div class="form-field"><label for="profile-current-password">Contraseña actual</label><input id="profile-current-password" type="password" formControlName="currentPassword" autocomplete="current-password" [class.invalid]="passwordForm.controls.currentPassword.touched && passwordForm.controls.currentPassword.invalid">@if (fieldError(passwordForm.controls.currentPassword); as message) { <small class="field-error">{{ message }}</small> }</div>
          <div class="form-field"><label for="profile-new-password">Nueva contraseña</label><input id="profile-new-password" type="password" formControlName="password" autocomplete="new-password" [class.invalid]="passwordForm.controls.password.touched && passwordForm.controls.password.invalid">@if (fieldError(passwordForm.controls.password); as message) { <small class="field-error">{{ message }}</small> } @else { <small class="field-hint">8–72 caracteres, con letras, números y un carácter especial.</small> }</div>
          <div class="form-field"><label for="profile-confirm-password">Confirma la nueva contraseña</label><input id="profile-confirm-password" type="password" formControlName="confirmPassword" autocomplete="new-password" [class.invalid]="passwordForm.controls.confirmPassword.touched && passwordForm.controls.confirmPassword.invalid"></div>
          @if (passwordForm.touched && passwordForm.errors?.['passwordMismatch']) { <div class="field-error">Las contraseñas no coinciden.</div> }
          @if (passwordError) { <div class="form-error" role="alert">{{ passwordError }}</div> }
          @if (passwordSuccess) { <div class="form-success" role="status">{{ passwordSuccess }}</div> }
          <div class="form-actions"><button class="button button-primary" type="submit">Actualizar contraseña</button></div>
        </form>
        </div>
      </div>
    }
  `
})
export class ProfilePage {
  readonly facade = inject(ForumFacade);
  readonly fieldError = fieldError;
  private readonly fb = inject(FormBuilder).nonNullable;
  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    phone: ['', [optionalPhone, Validators.maxLength(20)]],
    country: ['', [Validators.maxLength(60)]],
    showCountry: [false],
    bio: ['', [Validators.maxLength(300)]]
  });
  readonly passwordForm = this.fb.group({
    currentPassword: ['', Validators.required],
    password: ['', passwordRules],
    confirmPassword: ['', Validators.required]
  }, { validators: matchingPasswords });
  error = '';
  success = '';
  passwordError = '';
  passwordSuccess = '';
  photo = '';

  constructor() {
    effect(() => {
      const user = this.facade.currentUser();
      if (!user) return;
      this.form.patchValue({ name: user.name, email: user.email, bio: user.bio ?? '', phone: user.phone ?? '', country: user.country ?? '', showCountry: user.showCountry ?? false });
      this.photo = user.photo ?? '';
    });
  }

  onPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.error = 'Elige una imagen para tu foto.';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.error = 'La imagen debe pesar menos de 5 MB.';
      return;
    }
    this.error = '';
    const reader = new FileReader();
    reader.onload = () => {
      const image = new Image();
      image.onload = () => {
        const max = 360;
        const scale = Math.min(1, max / Math.max(image.width, image.height));
        const canvas = document.createElement('canvas');
        canvas.width = Math.max(1, Math.round(image.width * scale));
        canvas.height = Math.max(1, Math.round(image.height * scale));
        const context = canvas.getContext('2d');
        if (!context) return;
        context.drawImage(image, 0, 0, canvas.width, canvas.height);
        this.photo = canvas.toDataURL('image/jpeg', 0.85);
      };
      image.src = String(reader.result);
    };
    reader.readAsDataURL(file);
  }

  save(): void {
    this.error = '';
    this.success = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { name, email, bio, phone, country, showCountry } = this.form.getRawValue();
    const extras = [bio, phone, country, this.photo].some((value) => value.trim()) || showCountry;
    this.facade.updateProfile(name, email).subscribe({
      next: () => {
        this.success = extras
          ? 'Tu nombre y correo se guardaron. La descripción, el teléfono, la foto y el país todavía no existen en el servicio de usuarios.'
          : 'Tu perfil se actualizó correctamente.';
      },
      error: (err: HttpErrorResponse) => {
        this.error = apiErrorMessage(err, 'No se pudo actualizar el perfil.');
      }
    });
  }

  savePassword(): void {
    this.passwordError = '';
    this.passwordSuccess = '';
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const { currentPassword, password } = this.passwordForm.getRawValue();
    this.facade.changePassword(currentPassword, password).subscribe({
      next: () => {
        this.passwordForm.reset();
        this.passwordSuccess = 'Tu contraseña se actualizó correctamente.';
      },
      error: (err: HttpErrorResponse) => {
        this.passwordError = err.status === 401
          ? 'La contraseña actual no coincide.'
          : apiErrorMessage(err, 'No se pudo actualizar la contraseña.');
      }
    });
  }
}
