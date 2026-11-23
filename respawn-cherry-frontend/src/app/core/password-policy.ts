import { AbstractControl, ValidationErrors, Validators } from '@angular/forms';

function whenFilled(check: (value: string) => boolean, error: ValidationErrors) {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '');
    if (!value) return null;
    return check(value) ? null : error;
  };
}

export const passwordRules = [
  Validators.required,
  Validators.minLength(8),
  Validators.maxLength(72),
  whenFilled((value) => /[A-Za-z]/.test(value), { letter: true }),
  whenFilled((value) => /\d/.test(value), { digit: true }),
  whenFilled((value) => /[^A-Za-z\d]/.test(value), { special: true })
];
