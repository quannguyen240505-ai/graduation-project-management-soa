import { HttpClient } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { UserLookupResponse } from "../models/auth.models";
import { Topic, TopicRequest } from "../models/topic.models";

/** URL tương đối được proxy tới Gateway; interceptor hiện có tự gắn JWT. */
@Injectable({ providedIn: "root" })
export class TopicService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Topic[]> {
    return this.http.get<Topic[]>("/api/topics");
  }

  getById(id: number): Observable<Topic> {
    return this.http.get<Topic>(`/api/topics/${id}`);
  }

  create(request: TopicRequest): Observable<Topic> {
    return this.http.post<Topic>("/api/topics", request);
  }

  update(id: number, request: TopicRequest): Observable<Topic> {
    return this.http.put<Topic>(`/api/topics/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/topics/${id}`);
  }

  getLecturer(id: number): Observable<UserLookupResponse> {
    return this.http.get<UserLookupResponse>(`/api/auth/users/${id}`);
  }
}
