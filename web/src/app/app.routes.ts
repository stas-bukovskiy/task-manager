import {Routes} from '@angular/router';
import {LoginComponent} from "./pages/login/login.component";
import {RegisterComponent} from "./pages/register/register.component";
import {AppComponent} from "./app.component";
import {authGuard} from "./_guards/authGuard";
import {HomeComponent} from "./pages/home/home.component";

export const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: []},
  {path: 'login', component: LoginComponent, canActivate: [authGuard]},
  {path: 'registration', component: RegisterComponent, canActivate: [authGuard]},
  {path: '**', redirectTo: ''}
];
