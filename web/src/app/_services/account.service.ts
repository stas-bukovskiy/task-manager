import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, map} from "rxjs";
import {User, UserRegister} from "../_models/user";

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  baseUrl = 'http://localhost:8766/api/v1/';
  private currentUserSource = new BehaviorSubject<string | null>(null);
  currentUser$ = this.currentUserSource.asObservable();
  constructor(private http: HttpClient) {

  }
  login(user: any){
    return this.http.post<any>(this.baseUrl + 'auth/sign-in', user).pipe(
      map((response: User) => {
        const user = response;
        if(user){
          this.setCurrentToken(user.token);
          localStorage.setItem('token', user.token);
        }
      })
    );
  }

  register(user: any){
    return this.http.post<any>(this.baseUrl + 'auth/sign-up', user)
      // .pipe(
      // map(user => {
        // console.log(user);
        // if(user){
        //   this.setCurrentUser(user);
        // }
      // })
    // )
  }

  setCurrentToken(token: string){
    this.currentUserSource.next(token);
  }
  logout() {
    localStorage.removeItem('token');
    this.currentUserSource.next(null);
  }
}
