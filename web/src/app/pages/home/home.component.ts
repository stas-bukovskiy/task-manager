import { Component } from '@angular/core';
import {AccountService} from "../../_services/account.service";
import {Router} from "@angular/router";
import {MenuComponent} from "../../components/menu/menu.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    MenuComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

}
