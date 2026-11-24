import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { fieldError } from '../core/form-errors';
import { ForumFacade, apiErrorMessage } from '../core/forum.facade';
import { passwordRules } from '../core/password-policy';
import { PasswordRecoveryService } from '../core/password-recovery.service';

function matchingPasswords(control: AbstractControl): ValidationErrors | null {
  return control.get('password')?.value === control.get('confirmPassword')?.value ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-auth-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-layout">
      <section class="auth-art">
        <a class="brand" routerLink="/login" aria-label="Respawn Cherry">
          <img class="brand-mark" src="logo-respawn.png?v=2" alt="">
          <span class="brand-copy"><strong>respawn<span>cherry</span></strong><small class="brand-tag">community gaming for girls</small></span>
        </a>
        <div class="auth-art-copy">
          <span class="eyebrow">UN LUGAR PARA VOLVER A JUGAR</span>
          <h1>Cada jugador<br><span>tiene una historia</span></h1>
          <p>y la tuya tiene mucho por jugar. Conoce Respawn Cherry y encuentra tu lugar en la comunidad</p>
        </div>
        <div class="auth-art-foot">JUEGA A TU MANERA · ENCUENTRA TU COMUNIDAD</div>
      </section>

      <section class="auth-panel-wrap">
        <div class="auth-panel">
          @if (mode() === 'login') {
            <span class="eyebrow">¡QUE BUENO TENERTE DE VUELTA!</span>
            <h2>Inicia sesión</h2>
            <p>Tus amigos, tus partidas y tus próximas aventuras están a solo un inicio de sesión</p>
            <div class="auth-switch"><a class="active" routerLink="/login">Iniciar sesión</a><a routerLink="/register">Crear cuenta</a></div>
            <form [formGroup]="loginForm" (ngSubmit)="login()">
              <div class="form-field"><label for="login-email">Correo electrónico</label><input id="login-email" type="email" formControlName="email" placeholder="tu@correo.com" autocomplete="email" [class.invalid]="loginForm.controls.email.touched && loginForm.controls.email.invalid">@if (fieldError(loginForm.controls.email); as message) { <small class="field-error">{{ message }}</small> }</div>
              <div class="form-field"><label for="login-password">Contraseña</label><input id="login-password" type="password" formControlName="password" placeholder="Tu contraseña" autocomplete="current-password" [class.invalid]="loginForm.controls.password.touched && loginForm.controls.password.invalid">@if (fieldError(loginForm.controls.password); as message) { <small class="field-error">{{ message }}</small> }</div>
              @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
              <button class="button button-primary auth-submit" type="submit">Entrar a la comunidad <span>→</span></button>
            </form>
            <p class="auth-bottom"><a class="text-link" routerLink="/recover">¿Olvidaste tu contraseña?</a></p>
          } @else if (mode() === 'register') {
            <span class="eyebrow">TU COMUNIDAD TE ESPERA</span>
            <h2>Crea tu cuenta</h2>
            <p>Un nombre, un correo y ya estás dentro.</p>
            <div class="auth-switch"><a routerLink="/login">Iniciar sesión</a><a class="active" routerLink="/register">Crear cuenta</a></div>
            <form [formGroup]="registerForm" (ngSubmit)="register()">
              <div class="form-field"><label for="register-name">Nombre gamer</label><input id="register-name" formControlName="name" placeholder="¿Cómo te llamamos?" autocomplete="nickname" [class.invalid]="registerForm.controls.name.touched && registerForm.controls.name.invalid">@if (fieldError(registerForm.controls.name); as message) { <small class="field-error">{{ message }}</small> }</div>
              <div class="form-field"><label for="register-email">Correo electrónico</label><input id="register-email" type="email" formControlName="email" placeholder="tu@correo.com" autocomplete="email" [class.invalid]="registerForm.controls.email.touched && registerForm.controls.email.invalid">@if (fieldError(registerForm.controls.email); as message) { <small class="field-error">{{ message }}</small> }</div>
              <div class="form-field"><label for="register-password">Contraseña</label><input id="register-password" type="password" formControlName="password" placeholder="Al menos 8 caracteres" autocomplete="new-password" [class.invalid]="registerForm.controls.password.touched && registerForm.controls.password.invalid">@if (fieldError(registerForm.controls.password); as message) { <small class="field-error">{{ message }}</small> } @else { <small class="field-hint">8–72 caracteres, con letras, números y un carácter especial.</small> }</div>
              <div class="form-field"><label for="register-confirm">Confirma tu contraseña</label><input id="register-confirm" type="password" formControlName="confirmPassword" placeholder="Repite tu contraseña" autocomplete="new-password" [class.invalid]="registerForm.controls.confirmPassword.touched && registerForm.controls.confirmPassword.invalid">@if (fieldError(registerForm.controls.confirmPassword); as message) { <small class="field-error">{{ message }}</small> }</div>
              @if (registerForm.touched && registerForm.errors?.['passwordMismatch']) { <div class="field-error">Las contraseñas no coinciden.</div> }
              @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
              <button class="button button-primary auth-submit" type="submit">Crear cuenta <span>→</span></button>
            </form>
            <p class="auth-bottom">¿Ya tienes cuenta? <a class="text-link" routerLink="/login">Inicia sesión</a></p>
          } @else if (mode() === 'verificar') {
            <span class="eyebrow">TU ENLACE</span>
            <h2>Verifica tu correo</h2>
            <p>Estamos confirmando el enlace que te enviamos.</p>
            @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
            @if (success) { <div class="form-success" role="status">{{ success }}</div> }
            <p class="auth-bottom"><a class="text-link" routerLink="/login">Volver a iniciar sesión</a></p>
          } @else if (mode() === 'restablecer') {
            <span class="eyebrow">TU ENLACE</span>
            <h2>Elige una contraseña nueva</h2>
            <p>Este paso llega desde el correo que te enviamos.</p>
            <form [formGroup]="resetForm" (ngSubmit)="resetPassword()">
              <div class="form-field"><label for="reset-password">Nueva contraseña</label><input id="reset-password" type="password" formControlName="password" placeholder="Al menos 8 caracteres" autocomplete="new-password" [class.invalid]="resetForm.controls.password.touched && resetForm.controls.password.invalid">@if (fieldError(resetForm.controls.password); as message) { <small class="field-error">{{ message }}</small> } @else { <small class="field-hint">8–72 caracteres, con letras, números y un carácter especial.</small> }</div>
              <div class="form-field"><label for="reset-confirm">Confirma tu contraseña</label><input id="reset-confirm" type="password" formControlName="confirmPassword" placeholder="Repite tu contraseña" autocomplete="new-password" [class.invalid]="resetForm.controls.confirmPassword.touched && resetForm.controls.confirmPassword.invalid">@if (fieldError(resetForm.controls.confirmPassword); as message) { <small class="field-error">{{ message }}</small> }</div>
              @if (resetForm.touched && resetForm.errors?.['passwordMismatch']) { <div class="field-error">Las contraseñas no coinciden.</div> }
              @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
              @if (success) { <div class="form-success" role="status">{{ success }}</div> }
              <button class="button button-primary auth-submit" type="submit" [disabled]="sending">Guardar contraseña</button>
            </form>
            <p class="auth-bottom"><a class="text-link" routerLink="/login">Volver a iniciar sesión</a></p>
          } @else {
            <span class="eyebrow">RECUPERA EL ACCESO</span>
            <h2>¿Has olvidado tu contraseña?</h2>
            <p>Recupérala de manera sencilla :)</p>
            <form [formGroup]="recoverForm" (ngSubmit)="recover()">
              <div class="form-field"><label for="recover-email">Correo electrónico</label><input id="recover-email" type="email" formControlName="email" placeholder="tu@gmail.com" autocomplete="email" [class.invalid]="recoverForm.controls.email.touched && recoverForm.controls.email.invalid">@if (fieldError(recoverForm.controls.email); as message) { <small class="field-error">{{ message }}</small> }</div>
              @if (error) { <div class="form-error" role="alert">{{ error }}</div> }
              <button class="button button-primary auth-submit" type="submit" [disabled]="sending">Enviar</button>
            </form>
            <p class="auth-bottom"><a class="text-link" routerLink="/login">Volver a iniciar sesión</a></p>
          }
        </div>
      </section>
    </div>
    @if (recoverySent() || verificationSent()) {
      <div class="mail-modal" role="dialog" aria-modal="true" aria-labelledby="mail-title">
        <div class="mail-card">
          <div class="mail-hearts" aria-hidden="true"><span>♡</span><span>♡</span><span>♡</span></div>
          <img class="mail-cherry" src="logo-respawn.png?v=2" alt="">
          <h2 id="mail-title">{{ recoverySent() ? '¡Solicitud enviada! Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña. Revisa tu bandeja de entrada.' : '¡Solicitud enviada! Revisa tu bandeja de entrada para verificar tu correo antes de iniciar sesión.' }}</h2>
          <button class="button button-primary" type="button" (click)="closeRecovery()">Listo</button>
        </div>
      </div>
    }
  `
})
export class AuthPage {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly facade = inject(ForumFacade);
  private readonly recovery = inject(PasswordRecoveryService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  readonly fieldError = fieldError;
  readonly mode = signal(this.readMode());
  readonly recoverySent = signal(false);
  readonly verificationSent = signal(false);
  error = '';
  success = '';
  sending = false;

  constructor() {
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.mode.set(this.readMode());
      this.error = '';
      this.success = '';
      this.recoverySent.set(false);
      this.verificationSent.set(false);
      if (this.mode() === 'verificar') this.confirmEmail();
    });
    if (this.mode() === 'verificar') this.confirmEmail();
  }

  private readMode(): string {
    return this.router.url.split('?')[0].split('#')[0].split('/').filter(Boolean).at(-1) ?? 'login';
  }

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  readonly registerForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', passwordRules],
    confirmPassword: ['', Validators.required]
  }, { validators: matchingPasswords });

  readonly recoverForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly resetForm = this.fb.group({
    password: ['', passwordRules],
    confirmPassword: ['', Validators.required]
  }, { validators: matchingPasswords });

  login(): void {
    this.error = '';
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    const { email, password } = this.loginForm.getRawValue();
    this.facade.login(email, password).subscribe({
      next: () => void this.router.navigateByUrl('/forum'),
      error: (err: HttpErrorResponse) => {
        this.error = apiErrorMessage(err, 'Correo o contraseña incorrectos.');
      }
    });
  }

  register(): void {
    this.error = '';
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    const { name, email, password } = this.registerForm.getRawValue();
    this.facade.register(name, email, password).subscribe({
      next: () => {
        this.registerForm.reset();
        this.verificationSent.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.error = apiErrorMessage(err, 'No se pudo crear la cuenta.');
      }
    });
  }

  confirmEmail(): void {
    this.error = '';
    this.success = '';
    const token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!token.trim()) {
      this.error = 'El enlace no es válido.';
      return;
    }
    this.sending = true;
    this.recovery.confirm(token.trim()).subscribe({
      next: () => {
        this.sending = false;
        this.success = 'Tu correo quedó verificado. Ya puedes iniciar sesión.';
      },
      error: (err: HttpErrorResponse) => {
        this.sending = false;
        this.error = this.mailError(err);
      }
    });
  }

  recover(): void {
    this.error = '';
    if (this.recoverForm.invalid) {
      this.recoverForm.markAllAsTouched();
      return;
    }
    const email = this.recoverForm.getRawValue().email.trim();
    this.sending = true;
    this.recovery.request(email).subscribe({
      next: () => {
        this.sending = false;
        this.recoverForm.reset();
        this.recoverySent.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.sending = false;
        this.error = this.mailError(err);
      }
    });
  }

  resetPassword(): void {
    this.error = '';
    this.success = '';
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }
    const token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!token) {
      this.error = 'El enlace no es válido.';
      return;
    }
    const password = this.resetForm.getRawValue().password;
    this.sending = true;
    this.recovery.reset(token, password).subscribe({
      next: () => {
        this.sending = false;
        this.success = 'Tu contraseña quedó actualizada. Ya puedes iniciar sesión.';
        this.resetForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.sending = false;
        this.error = this.mailError(err);
      }
    });
  }

  closeRecovery(): void {
    this.recoverySent.set(false);
    this.verificationSent.set(false);
  }

  private mailError(err: HttpErrorResponse): string {
    const mensaje = err.error && typeof err.error === 'object' && 'mensaje' in err.error
      ? String(err.error.mensaje)
      : '';
    return mensaje || 'No pudimos enviar el correo. Revisa que el servidor esté activo.';
  }
}
