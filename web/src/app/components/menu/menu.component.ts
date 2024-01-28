import { Component } from '@angular/core';
import {AccountService} from "../../_services/account.service";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {
  constructor(public accountService: AccountService, private router: Router) {

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
