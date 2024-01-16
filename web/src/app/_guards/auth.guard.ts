import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {AccountService} from "../_services/account.service";
import {map} from "rxjs";

export const AuthGuard: CanActivateFn = (route, state) => {
  const accountService = inject(AccountService);
  // const toastr = inject(ToastrService);

  return accountService.currentUser$.pipe(
    map(user => {
      console.log(user);
      if (user) {
        return true;
      }
      else {
        // toastr.error('you shall not pass!');
        return false;
      }
    })
  )
};
