package com.team1.mixIt.post.dto.request;

import com.team1.mixIt.common.validation.NoBannedWords;
import com.team1.mixIt.common.validation.NoPersonalInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "게시판 등록 요청 DTO")
public class PostCreateRequest {

    @NotNull(message = "카테고리는 필수값입니다.")
    @Schema(description = "게시물 카테고리", example = "카페")
    private String category;

    @NotBlank(message = "게시물 제목은 필수입니다.")
    @Size(max = 20, message = "게시물 제목은 20자 이내로 입력해 주세요.")
    @NoPersonalInfo
    @NoBannedWords(message = "금지된 단어가 포함되어 있습니다.")
    @Schema(description = "게시물 제목 (최대 20자)", example = "서브웨이 꿀조합")
    private String title;

    @NotBlank(message = "게시물 내용은 필수입니다.")
    @Size(max = 5000, message = "게시물 내용은 5000자 이내로 입력해 주세요.")
    @NoPersonalInfo
    @NoBannedWords(message = "금지된 단어가 포함되어 있습니다.")
    @Schema(description = "게시물 내용")
    private String content;

    @NotNull(message = "태그 리스트는 필수값입니다.")
    @Size(max = 10, message = "태그는 최대 10개까지 입력할 수 있습니다.")
    @Valid
    @Schema(description = "게시물에 달릴 태그 목록 (최대 10개, 각 태그는 10자 이내)", example = "[\"서브웨이\", \"에그마요\", \"랜치소스\"]")
    private List<@Size(max = 10, message = "태그는 10자 이내로 입력해 주세요.") @NoBannedWords @NoPersonalInfo String> tags;

    @Schema(description = "첨부 이미지 ID 목록", example = "[1,2,3]")
    private List<Long> imageIds;

}
