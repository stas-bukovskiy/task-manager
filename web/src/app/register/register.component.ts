import { Component } from '@angular/core';
import {AccountService} from "../_services/account.service";
import {Router, RouterLink} from "@angular/router";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgIf} from "@angular/common";
import {FocusDirective} from "../_directives/focus.directive";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgIf,
    FocusDirective,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registrationError = '';
  constructor(public accountService: AccountService, private router: Router, private http: HttpClient) {
  }
  registrationForm = new FormGroup({
    username: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2),
      Validators.pattern('^[A-Za-z0-9]+$')
    ]),
    email: new FormControl<string>('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(6)
    ]),
    first_name: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2)
    ]),
    last_name: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2)
    ]),
  })
  register(){
      console.log(this.registrationForm);

    if(this.registrationForm.status === "INVALID") {
      this.registrationForm.markAllAsTouched();
      return;
    }
    this.accountService.register(this.registrationForm.value).subscribe({
      next: () => {
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        console.error(err);
        this.registrationError = 'Username or email is already taken';
      }
    })
  }
  get username() {
    return this.registrationForm.controls.username as FormControl;
  }
  get email() {
    return this.registrationForm.controls.email as FormControl;
  }
  get password() {
    return this.registrationForm.controls.password as FormControl;
  }
  get firstName() {
    return this.registrationForm.controls.first_name as FormControl;
  }
  get lastName() {
    return this.registrationForm.controls.last_name as FormControl;
  }
}
