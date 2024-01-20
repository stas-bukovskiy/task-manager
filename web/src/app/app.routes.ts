import {Routes} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {RegisterComponent} from "./register/register.component";
import {AppComponent} from "./app.component";
import {authGuard} from "./_guards/authGuard";
import {HomeComponent} from "./home/home.component";

export const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [authGuard]},
  {path: 'login', component: LoginComponent},
  {path: 'registration', component: RegisterComponent},
  {path: '**', redirectTo: ''}
];
