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
        return name();
    }

    @JsonCreator
    public static Category fromJson(String json) {
        return Category.valueOf(json);
    }
}
