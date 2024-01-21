import { Component } from '@angular/core';
import {AccountService} from "../_services/account.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
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
