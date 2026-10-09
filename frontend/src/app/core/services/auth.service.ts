import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable, computed, signal } from "@angular/core";
import { Observable, map, switchMap, tap } from "rxjs";
import {
  AuthSession,
  JwtPayload,
  LoginRequest,
  LoginResponse,
  UserLookupResponse,
  RegisterRequest,
  UserMeResponse,
} from "../models/auth.models";

const SESSION_KEY = "graduation_soa_auth_session";

@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly sessionState = signal<AuthSession | null>(
    this.readSession(),
  );

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => {
    const session = this.sessionState();
    return !!session && !this.isTokenExpired(session.accessToken);
  });

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthSession> {
    return this.http.post<LoginResponse>("/api/auth/login", request).pipe(
      switchMap((response) => {
        if (!response.accessToken || response.userId == null) {
          throw new Error("Auth Service không trả accessToken hoặc userId.");
        }

        const headers = new HttpHeaders({
          Authorization: `Bearer ${response.accessToken}`,
        });

        return this.http
          .get<UserLookupResponse>(`/api/auth/users/${response.userId}`, {
            headers,
          })
          .pipe(
            map((user) => ({
              accessToken: response.accessToken,
              userId: response.userId,
              role: user.role,
              status: user.status,
            })),
          );
      }),
      tap((session) => this.saveSession(session)),
    );
  }

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post("/api/auth/register", request);
  }

  getMe(): Observable<UserMeResponse> {
    return this.http.get<UserMeResponse>("/api/users/me");
  }

  refreshProfile(): Observable<UserLookupResponse> {
    const current = this.sessionState();
    if (!current) {
      throw new Error("Chưa đăng nhập.");
    }

    return this.http
      .get<UserLookupResponse>(`/api/auth/users/${current.userId}`)
      .pipe(
        tap((user) => {
          this.saveSession({
            ...current,
            role: user.role,
            status: user.status,
          });
        }),
      );
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.sessionState.set(null);
  }

  getAccessToken(): string | null {
    const session = this.sessionState();
    if (!session || this.isTokenExpired(session.accessToken)) {
      return null;
    }
    return session.accessToken;
  }

  decodeJwt(token = this.getAccessToken()): JwtPayload | null {
    if (!token) return null;

    try {
      const payload = token.split(".")[1];
      if (!payload) return null;
      const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
      const padded = normalized.padEnd(
        Math.ceil(normalized.length / 4) * 4,
        "=",
      );
      return JSON.parse(decodeURIComponent(escape(atob(padded)))) as JwtPayload;
    } catch {
      try {
        const payload = token.split(".")[1];
        if (!payload) return null;
        const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
        return JSON.parse(atob(normalized)) as JwtPayload;
      } catch {
        return null;
      }
    }
  }

  isTokenExpired(token: string): boolean {
    const payload = this.decodeJwt(token);
    if (!payload?.exp) return false;
    return payload.exp * 1000 <= Date.now();
  }

  private saveSession(session: AuthSession): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.sessionState.set(session);
  }

  private readSession(): AuthSession | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as AuthSession;
      return parsed?.accessToken && parsed?.userId != null ? parsed : null;
    } catch {
      return null;
    }
  }
}
