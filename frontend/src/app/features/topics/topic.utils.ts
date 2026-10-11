import { HttpErrorResponse } from "@angular/common/http";
import { Topic } from "../../core/models/topic.models";

export function canManageTopics(role: string | undefined): boolean {
  return role === "ADMIN" || role === "LECTURER";
}

export function hasAvailableSlots(topic: Topic): boolean {
  return topic.status === "OPEN" && topic.currentStudents < topic.maxStudents;
}

export function topicStatusLabel(topic: Topic): string {
  if (topic.status === "OPEN") {
    return hasAvailableSlots(topic) ? "Còn chỗ" : "Đã đủ sinh viên";
  }
  return topic.status === "CLOSED" ? "Đã đóng" : "Chưa mở";
}

export function topicOccupancy(topic: Topic): number {
  if (topic.maxStudents <= 0) return 0;
  return Math.min(100, Math.max(0, (topic.currentStudents / topic.maxStudents) * 100));
}

export function normalizedSearch(value: string): string {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/gi, "d").toLowerCase().trim();
}

export function parseTopicId(value: string | null): number | null {
  if (!value || !/^[1-9]\d*$/.test(value)) return null;
  const id = Number(value);
  return Number.isSafeInteger(id) ? id : null;
}

/** Không đưa stack trace hoặc thông báo SQL lên giao diện. */
export function topicErrorMessage(error: unknown, action: "load" | "save" | "delete"): string {
  if (!(error instanceof HttpErrorResponse)) {
    return "Có lỗi xảy ra. Vui lòng thử lại.";
  }
  switch (error.status) {
    case 0:
      return "Không thể kết nối hệ thống. Vui lòng kiểm tra kết nối và thử lại.";
    case 400:
      return action === "save"
        ? "Không thể lưu đề tài. Kiểm tra giảng viên và số lượng tối đa; số lượng tối đa không được thấp hơn số sinh viên đã duyệt."
        : "Yêu cầu không hợp lệ. Vui lòng tải lại trang.";
    case 401:
      return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    case 403:
      return "Bạn không có quyền thực hiện thao tác này.";
    case 404:
      return "Đề tài không tồn tại hoặc đã được xóa.";
    case 409:
      return action === "delete"
        ? "Không thể xóa đề tài đã có sinh viên được duyệt. Hãy tải lại dữ liệu để kiểm tra."
        : "Mã đề tài đã tồn tại. Vui lòng sử dụng mã khác.";
    case 502:
    case 503:
    case 504:
      return "Dịch vụ đang tạm thời không khả dụng. Vui lòng thử lại sau.";
    default:
      return "Hệ thống chưa xử lý được yêu cầu. Vui lòng thử lại sau.";
  }
}
