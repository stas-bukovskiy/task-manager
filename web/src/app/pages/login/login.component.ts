import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AccountService} from "../../_services/account.service";
import {Router, RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginError = '';
  constructor(public accountService: AccountService, private router: Router) {

  }
  loginForm = new FormGroup({
    login: new FormControl<string>('', [
      Validators.required,
    ]),
    password: new FormControl<string>('', [
      Validators.required
    ]),
  })
  signIn(){
    if(this.loginForm.status === "INVALID") {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.accountService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigateByUrl('home');
      },
      error: (err) => {
        // console.error(err);
        this.loginError = 'Login or password are incorrect';
      }
    })
  }
  get login() {
    return this.loginForm.controls.login as FormControl;
  }
  get password() {
    return this.loginForm.controls.password as FormControl;
  }

}
