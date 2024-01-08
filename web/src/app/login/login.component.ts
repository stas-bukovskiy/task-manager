import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AccountService} from "../_services/account.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  constructor(public accountService: AccountService, private router: Router) {

  }
  loginForm = new FormGroup({
    email: new FormControl<string>('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl<string>('', [
      Validators.required
    ]),
  })
  login(){
    console.log(this.loginForm);
    this.accountService.login(this.loginForm.value).subscribe({
      // next: () => {
      //   this.router.navigateByUrl('/members')
      // },
      // error: error => this.toastr.error(error.error)
    })
  }
}
