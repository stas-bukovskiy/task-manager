import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import {LoginComponent} from "./pages/login/login.component";
import {AccountService} from "./_services/account.service";
import {User} from "./_models/user";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit{
  title = 'Task Manager';

  constructor(private accountService: AccountService) {
  }
  ngOnInit(): void {
    this.setCurrentUser();
  }

  setCurrentUser() {
    const token = localStorage.getItem('token');
    if (!token) return;
    // const user: User = JSON.parse(userString);
    this.accountService.setCurrentToken(token);
  }

}