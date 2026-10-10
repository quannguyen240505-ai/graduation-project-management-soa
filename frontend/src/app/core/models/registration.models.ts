/** DTO shape used by the Registration and Topic REST controllers. */
export type RegistrationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface Registration {
  id: number;
  studentId: number;
  topicId: number;
  registrationDate: string;
  status: RegistrationStatus | string;
  note: string | null;
  createdAt: string;
  updatedAt: string;
  capacityVersion?: number;
}

export interface CreateRegistrationRequest {
  /** Compatibility with the current API. The backend MUST validate against JWT. */
  studentId: number;
  topicId: number;
  note: string;
}

export interface Topic {
  id: number;
  topicCode: string;
  topicName: string;
  description: string | null;
  lecturerId: number;
  maxStudents: number;
  currentStudents: number;
  field: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}
