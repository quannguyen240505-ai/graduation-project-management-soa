package com.graduation.topic.service;

/** Quy tắc tính số chỗ; không phụ thuộc HTTP hay database. */
public final class TopicCapacityPolicy {

    private TopicCapacityPolicy() {
    }

    public record PreviousState(boolean approved, long version) {
    }

    public static int calculate(
            int current, int maximum, boolean open,
            PreviousState previous, boolean approved, long version) {

        if (version < 0) {
            throw new IllegalArgumentException("version phải không âm");
        }
        if (maximum <= 0 || current < 0 || current > maximum) {
            throw new IllegalStateException("Số lượng hiện tại của đề tài không hợp lệ");
        }

        if (previous != null) {
            if (version < previous.version()) {
                throw new IllegalStateException(
                        "Yêu cầu cập nhật số chỗ đã cũ; hãy đồng bộ lại đăng ký");
            }
            if (version == previous.version()) {
                if (approved != previous.approved()) {
                    throw new IllegalStateException(
                            "Cùng version nhưng khác trạng thái; hãy gửi lại yêu cầu trước đó");
                }
                return current;
            }
        }

        boolean previouslyApproved = previous != null && previous.approved();
        if (approved == previouslyApproved) {
            return current;
        }

        if (approved) {
            if (!open) {
                throw new IllegalStateException("Đề tài đã đóng đăng ký");
            }
            if (current >= maximum) {
                throw new IllegalStateException("Đề tài đã đủ số lượng sinh viên");
            }
            return current + 1;
        }

        if (current == 0) {
            throw new IllegalStateException(
                    "Số chỗ và lịch sử đăng ký không khớp; cần kiểm tra dữ liệu");
        }
        return current - 1;
    }
}
