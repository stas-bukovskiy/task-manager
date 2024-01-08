import { Routes } from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {RegisterComponent} from "./register/register.component";

export const routes: Routes = [
  // {path: '', component: ProductPageComponent},
  {path: 'login', component: LoginComponent},
  {path: 'registration', component: RegisterComponent}
];
