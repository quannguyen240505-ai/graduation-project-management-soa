import { Routes } from "@angular/router";
import { authGuard, guestGuard } from "./core/guards/auth.guard";
import { ShellComponent } from "./layout/shell/shell.component";

export const routes: Routes = [
  {
    path: "login",
    canActivate: [guestGuard],
    loadComponent: () =>
      import("./features/login/login.component").then((m) => m.LoginComponent),
  },
  {
    path: "register",
    canActivate: [guestGuard],
    loadComponent: () =>
      import("./features/register/register.component").then(
        (m) => m.RegisterComponent,
      ),
  },
  {
    path: "forbidden",
    loadComponent: () =>
      import("./features/forbidden/forbidden.component").then(
        (m) => m.ForbiddenComponent,
      ),
  },
  {
    path: "",
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: "dashboard",
        loadComponent: () =>
          import("./features/dashboard/dashboard.component").then(
            (m) => m.DashboardComponent,
          ),
      },
      {
        path: "profile",
        loadComponent: () =>
          import("./features/profile/profile.component").then(
            (m) => m.ProfileComponent,
          ),
      },
      {
        path: "gateway-check",
        loadComponent: () =>
          import("./features/gateway-check/gateway-check.component").then(
            (m) => m.GatewayCheckComponent,
          ),
      },
      { path: "", pathMatch: "full", redirectTo: "dashboard" },
    ],
  },
  { path: "**", redirectTo: "dashboard" },
];
