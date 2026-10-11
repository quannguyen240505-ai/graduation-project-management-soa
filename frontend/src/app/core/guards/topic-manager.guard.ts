import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";

/** Hỗ trợ điều hướng; Gateway và Topic Service vẫn quyết định quyền thật. */
export const topicManagerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) return router.createUrlTree(["/login"]);
  const role = auth.session()?.role;
  return role === "ADMIN" || role === "LECTURER"
    ? true
    : router.createUrlTree(["/forbidden"]);
};
