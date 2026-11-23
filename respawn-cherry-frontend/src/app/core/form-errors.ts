import { AbstractControl } from '@angular/forms';

export function fieldError(control: AbstractControl): string | null {
  if (!control.touched || !control.errors) return null;
  if (control.errors['required']) return 'Este campo es obligatorio.';
  if (control.errors['email']) return 'Ingresa un correo válido.';
  if (control.errors['minlength']) return 'La contraseña debe tener al menos 8 caracteres.';
  if (control.errors['letter']) return 'La contraseña debe incluir al menos una letra.';
  if (control.errors['digit']) return 'La contraseña debe incluir al menos un número.';
  if (control.errors['special']) return 'La contraseña debe incluir al menos un carácter especial.';
  if (control.errors['phone']) return 'Ingresa un teléfono válido.';
  if (control.errors['maxlength']) {
    return control.errors['maxlength'].requiredLength === 72
      ? 'La contraseña no puede superar 72 caracteres.'
      : 'Supera el máximo permitido.';
  }
  return 'Revisa este campo.';
}
