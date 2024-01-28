import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {AccountService} from "../_services/account.service";
import {map} from "rxjs";

export const authGuard: CanActivateFn = (route, state) => {
  const accountService = inject(AccountService);
  const router = inject(Router);
  return accountService.currentUser$.pipe(
    map(user => {
      if (user) {
        if(state.url === '/login' || state.url === '/registration'){
          router.navigateByUrl('/');
          return false;
        }
        return true;
      }
      else {
        if(state.url === '/login' || state.url === '/registration'){
          return true;
        }
        router.navigateByUrl('/login');
        return false;
      }
    })
  )
};
