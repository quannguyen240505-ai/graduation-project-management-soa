package com.graduation.topic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TopicCapacityRequest(
        @NotNull Boolean approved,
        @NotNull @Min(0) Long version) {
}
