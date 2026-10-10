/** Contract của TopicResponse; số chỗ và trạng thái do backend quản lý. */
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

/** Chỉ gửi các trường mà TopicRequest của backend chấp nhận. */
export interface TopicRequest {
  topicCode: string;
  topicName: string;
  description: string;
  lecturerId: number;
  maxStudents: number;
  field: string;
}
