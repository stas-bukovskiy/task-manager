import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {AccountService} from "../_services/account.service";
import {map} from "rxjs";

export const authGuard: CanActivateFn = (route, state) => {
  const accountService = inject(AccountService);
  // const toastr = inject(ToastrService);
  const router = inject(Router);
  return accountService.currentUser$.pipe(
    map(user => {
      // console.log(user);
      if (user) {
        return true;
      }
      else {
        // toastr.error('you shall not pass!');
        router.navigate(['/login']);
        // this.router.navigate(['/login'], { queryParams: { returnUrl: state.url }});
        return false;
      }
    })
  )
};
