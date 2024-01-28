import {Component} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {ModalService} from "../../_services/modal.service";

@Component({
  selector: 'app-user-info',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './user-info.component.html',
  styleUrl: './user-info.component.scss'
})
export class UserInfoComponent {
  constructor(public modalService: ModalService) {
  }
  userInfoForm = new FormGroup({
    username: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(2),
      Validators.pattern('^[A-Za-z0-9]+$')
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

  changeInfo() {

  }

  get username() {
    return this.userInfoForm.controls.username as FormControl;
  }

  get firstName() {
    return this.userInfoForm.controls.first_name as FormControl;
  }

  get lastName() {
    return this.userInfoForm.controls.last_name as FormControl;
  }

}
