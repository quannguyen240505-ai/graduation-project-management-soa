import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { AuthService } from "../services/auth.service";

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.getAccessToken();

  const requestWithToken = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(requestWithToken).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !request.url.includes("/api/auth/login")) {
        auth.logout();
        void router.navigate(["/login"], {
          queryParams: { reason: "expired" },
        });
      }
      if (error.status === 403) {
        const handledByPage =
          request.url.startsWith("/api/registrations") ||
          request.url.startsWith("/api/topics");

        if (!handledByPage) {
          void router.navigate(["/forbidden"]);
        }
      }
      return throwError(() => error);
    }),
  );
};
