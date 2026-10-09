import { Component, computed, inject } from "@angular/core";
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from "@angular/router";
import { AuthService } from "../../core/services/auth.service";

@Component({
  selector: "app-shell",
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: "./shell.component.html",
  styleUrl: "./shell.component.css",
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly session = computed(() => this.auth.session());

  logout(): void {
    this.auth.logout();
    void this.router.navigate(["/login"]);
  }
}
