package com.group.registration.dto;

/** Gửi trạng thái mong muốn và version, thay vì lệnh tăng/giảm mù. */
public record TopicCapacityRequest(boolean approved, long version) {
}
