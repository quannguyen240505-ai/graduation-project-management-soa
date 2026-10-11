import {
  HttpErrorResponse,
  HttpInterceptorFn,
} from "@angular/common/http";

import { inject } from "@angular/core";
import { Router } from "@angular/router";

import {
  catchError,
  throwError,
} from "rxjs";

import {
  AuthService,
} from "../services/auth.service";

export const authInterceptor: HttpInterceptorFn = (
  request,
  next,
) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token =
    auth.getAccessToken();

  const requestWithToken = token
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : request;

  return next(requestWithToken).pipe(
    catchError(
      (error: HttpErrorResponse) => {

        // =========================
        // 401 - Token không hợp lệ
        // hoặc đã hết hạn
        // =========================

        if (
          error.status === 401 &&
          !request.url.includes(
            "/api/auth/login",
          ) &&
          !request.url.includes(
            "/api/auth/register",
          )
        ) {
          auth.logout();

          void router.navigate(
            ["/login"],
            {
              queryParams: {
                reason: "expired",
              },
            },
          );
        }

        // =========================
        // 403 - Không có quyền
        // =========================

        if (error.status === 403) {

          /*
           * Topic và Registration
           * tự xử lý lỗi 403 ngay trên trang
           * để hiển thị thông báo phù hợp
           * cho người dùng.
           */
          const handledByPage =
            request.url.startsWith(
              "/api/registrations",
            ) ||
            request.url.startsWith(
              "/api/topics",
            );

          /*
           * Các API khác nếu bị 403
           * sẽ chuyển sang trang Forbidden.
           */
          if (!handledByPage) {
            void router.navigate(
              ["/forbidden"],
            );
          }
        }

        return throwError(
          () => error,
        );
      },
    ),
  );
};