import { HttpErrorResponse } from '@angular/common/http';

export function translateError(res: HttpErrorResponse) {
  if (res.status === 403) {
    return res.error?.message ? res.error?.message : 'Invalid Credentials';
  }

  return 'Unxpected Error';
}
