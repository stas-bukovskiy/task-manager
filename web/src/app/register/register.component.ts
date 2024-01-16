import { Component } from '@angular/core';
import {AccountService} from "../_services/account.service";
import {Router, RouterLink} from "@angular/router";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  constructor(public accountService: AccountService, private router: Router) {

  }
  registrationForm = new FormGroup({
    username: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2)
    ]),
    email: new FormControl<string>('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(6)
    ]),
  })
  register(){
    console.log(this.registrationForm);
    this.accountService.register(this.registrationForm.value).subscribe({
      // next: () => {
      //   this.router.navigateByUrl('/members')
      // },
      // error: error => this.toastr.error(error.error)
    })
  }
}
