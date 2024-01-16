import { Routes } from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {RegisterComponent} from "./register/register.component";
import {AppComponent} from "./app.component";
import {AuthGuard} from "./_guards/auth.guard";
import {HomeComponent} from "./home/home.component";

export const routes: Routes = [
  {path: '', component: AppComponent, canActivate:[AuthGuard]},
  {path: 'login', component: LoginComponent},
  {path: 'registration', component: RegisterComponent},
  {path: 'home', component: HomeComponent},
];
