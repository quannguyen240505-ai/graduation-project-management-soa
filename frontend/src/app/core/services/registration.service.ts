import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
  CreateRegistrationRequest,
  Registration,
  RegistrationStatus,
  Topic,
} from "../models/registration.models";

@Injectable({ providedIn: "root" })
export class RegistrationService {
  private readonly http = inject(HttpClient);

  getRegistrations(): Observable<Registration[]> {
    return this.http.get<Registration[]>("/api/registrations");
  }

  getRegistration(id: number): Observable<Registration> {
    return this.http.get<Registration>(`/api/registrations/${id}`);
  }

  createRegistration(body: CreateRegistrationRequest): Observable<Registration> {
    return this.http.post<Registration>("/api/registrations", body);
  }

  updateStatus(id: number, status: RegistrationStatus): Observable<Registration> {
    return this.http.put<Registration>(`/api/registrations/${id}/status`, {
      status,
    });
  }

  deleteRegistration(id: number): Observable<void> {
    return this.http.delete<void>(`/api/registrations/${id}`);
  }

  getTopics(): Observable<Topic[]> {
    return this.http.get<Topic[]>("/api/topics");
  }
}
