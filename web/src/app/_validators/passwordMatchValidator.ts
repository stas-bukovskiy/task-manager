import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Validator Function
export function passwordMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    return password === confirmPassword ? null : { notSame: true };
  };
}
