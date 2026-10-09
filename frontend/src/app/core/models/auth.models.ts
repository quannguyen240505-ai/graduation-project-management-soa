export type UserRole = "STUDENT" | "LECTURER" | "ADMIN" | string;

export interface LoginRequest {
  userCode: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  userId: number;
  [key: string]: unknown;
}

export interface RegisterRequest {
  userCode: string;
  password: string;
  fullName: string;
  email: string;
  phone?: string;
  className?: string;
  department?: string;
}

export interface UserLookupResponse {
  id: number;
  role: UserRole;
  status: string;
}

export interface UserMeResponse {
  id: number;
  userCode: string;
  fullName: string;
  email: string;
  phone: string | null;
  role: UserRole;
  className: string | null;
  department: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthSession {
  accessToken: string;
  userId: number;
  role: UserRole;
  status: string;
}

export interface JwtPayload {
  iss?: string;
  sub?: string;
  exp?: number;
  iat?: number;
  role?: string;
  userId?: number | string;
  [key: string]: unknown;
}
