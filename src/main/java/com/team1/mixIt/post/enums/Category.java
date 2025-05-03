package com.team1.mixIt.post.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    CAFE("카페"),
    RESTAURANT("음식점"),
    CONVENIENCE("편의점"),
    OTHER("기타");

    private final String label;

    @JsonValue
    public String toJson() {
        return label;
    }

    @JsonCreator
    public static Category fromJson(String label) {
        for (Category c : values()) {
            if (c.label.equals(label)) {
                return c;
            }
        }
        throw new IllegalArgumentException("알 수 없는 카테고리: " + label);
    }
}
