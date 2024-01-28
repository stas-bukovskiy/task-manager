import { Component } from '@angular/core';
import {AccountService} from "../../_services/account.service";
import {Router, RouterLink} from "@angular/router";
import {ModalService} from "../../_services/modal.service";
import {AsyncPipe} from "@angular/common";
import {ModalComponent} from "../modal/modal.component";
import {UserInfoComponent} from "../user-info/user-info.component";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    RouterLink,
    AsyncPipe,
    ModalComponent,
    UserInfoComponent
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {
  constructor(public accountService: AccountService, private router: Router, public modalService: ModalService) {

  }
  logout(){
    this.accountService.logout();
    this.router.navigateByUrl('/login');
  }
  test(){
    const user = localStorage.getItem('token') ?? '';
    console.log(user);
  }
}
