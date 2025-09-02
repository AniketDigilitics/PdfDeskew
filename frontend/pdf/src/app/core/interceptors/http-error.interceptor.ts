import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 0) {
        toast.error('Network error');
      } else {
        const msg =
          typeof err.error === 'string'
            ? err.error
            : err.error?.message ?? 'Request failed';
        toast.error(`${err.status}: ${msg}`);
      }
      return throwError(() => err);
    })
  );
};
