import { Routes } from "@angular/router";

import {
  authGuard,
  guestGuard,
} from "./core/guards/auth.guard";

import {
  topicManagerGuard,
} from "./core/guards/topic-manager.guard";

import {
  ShellComponent,
} from "./layout/shell/shell.component";

export const routes: Routes = [
  {
    path: "login",
    canActivate: [guestGuard],
    loadComponent: () =>
      import(
        "./features/login/login.component"
      ).then(
        (m) => m.LoginComponent,
      ),
  },

  {
    path: "register",
    canActivate: [guestGuard],
    loadComponent: () =>
      import(
        "./features/register/register.component"
      ).then(
        (m) => m.RegisterComponent,
      ),
  },

  {
    path: "forbidden",
    loadComponent: () =>
      import(
        "./features/forbidden/forbidden.component"
      ).then(
        (m) => m.ForbiddenComponent,
      ),
  },

  {
    path: "",
    component: ShellComponent,
    canActivate: [authGuard],

    children: [
      // =========================
      // Dashboard
      // =========================

      {
        path: "dashboard",
        loadComponent: () =>
          import(
            "./features/dashboard/dashboard.component"
          ).then(
            (m) => m.DashboardComponent,
          ),
      },

      // =========================
      // Profile
      // =========================

      {
        path: "profile",
        loadComponent: () =>
          import(
            "./features/profile/profile.component"
          ).then(
            (m) => m.ProfileComponent,
          ),
      },

      // =========================
      // Topic
      // =========================

      {
        path: "topics",
        loadComponent: () =>
          import(
            "./features/topics/topic-list.component"
          ).then(
            (m) => m.TopicListComponent,
          ),
      },

      {
        path: "topics/new",
        canActivate: [
          topicManagerGuard,
        ],
        loadComponent: () =>
          import(
            "./features/topics/topic-form.component"
          ).then(
            (m) => m.TopicFormComponent,
          ),
      },

      {
        path: "topics/:id/edit",
        canActivate: [
          topicManagerGuard,
        ],
        loadComponent: () =>
          import(
            "./features/topics/topic-form.component"
          ).then(
            (m) => m.TopicFormComponent,
          ),
      },

      {
        path: "topics/:id",
        loadComponent: () =>
          import(
            "./features/topics/topic-detail.component"
          ).then(
            (m) => m.TopicDetailComponent,
          ),
      },

      // =========================
      // Registration
      // =========================

      {
        path: "registrations",
        loadComponent: () =>
          import(
            "./features/registrations/registrations.component"
          ).then(
            (m) => m.RegistrationsComponent,
          ),
      },
      // =========================
      // Notification
      // =========================

      {
        path: "notifications",
        loadComponent: () =>
          import(
            "./features/notifications/notifications.component"
          ).then(
            (m) => m.NotificationsComponent,
          ),
      },

      // =========================
      // System status
      // =========================

      {
        path: "gateway-check",
        loadComponent: () =>
          import(
            "./features/gateway-check/gateway-check.component"
          ).then(
            (m) => m.GatewayCheckComponent,
          ),
      },

      {
        path: "",
        pathMatch: "full",
        redirectTo: "dashboard",
      },
    ],
  },

  {
    path: "**",
    redirectTo: "dashboard",
  },
];