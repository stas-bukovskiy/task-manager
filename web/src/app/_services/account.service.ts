import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, map} from "rxjs";
import {User, UserRegister} from "../_models/user";

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  // baseUrl = 'https://api.escuelajs.co/api/v1/';
  // baseUrl = 'https://api.realworld.io/api/users/';
  baseUrl = 'https://localhost:8766/api/v1/';
  private currentUserSource = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSource.asObservable();
  constructor(private http: HttpClient) {

  }
  login(user: any){
    return this.http.post<any>(this.baseUrl + 'auth/sign-in', user).pipe(
      map((response: User) => {
        const user = response;
        if(user){
          this.setCurrentUser(user);
        }
      })
    );
  }

  register(user: any){
    return this.http.post<any>(this.baseUrl + 'auth/sign-up', user).pipe(
      map(user => {
        console.log(user);
        // if(user){
        //   this.setCurrentUser(user);
        // }
      })
    )
  }

  setCurrentUser(user: User){
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUserSource.next(user);
  }
  logout() {
    localStorage.removeItem('user');
    this.currentUserSource.next(null);
  }
}
