package com.team1.mixIt.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(description = "게시판 수정 요청 DTO")
public class PostUpdateRequest {

    @NotNull(message = "카테고리는 필수값입니다.")
    @Schema(description = "게시물 카테고리 (예: 카페, 편의점, 음식점, 기타)", example = "카페")
    private String category;

    @NotBlank(message = "게시물 제목은 필수입니다.")
    @Size(max = 20, message = "게시물 제목은 20자 이내로 입력해 주세요.")
    @Schema(description = "게시물 제목", example = "수정된 제목")
    private String title;

    @NotBlank(message = "게시물 내용은 필수입니다.")
    @Size(max = 5000, message = "게시물 내용은 5000자 이내로 입력해 주세요.")
    @Schema(description = "게시물 내용", example = "수정된 내용")
    private String content;

    @Schema(description = "게시물에 달릴 태그 목록 (최대 10개, 각 태그는 10자 이내)", example = "[\"공지\", \"자유\"]")
    private List<@Size(max = 10, message = "태그는 10자 이내로 입력해 주세요.") String> tags;
}
