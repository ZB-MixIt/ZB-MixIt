package com.team1.mixIt.post.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.post.dto.response.BookmarkResponse;
import com.team1.mixIt.post.dto.response.BookmarkResponsePage;
import com.team1.mixIt.post.service.PostBookmarkService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "게시판 API", description = "게시물 북마크 관련 API")
@RequiredArgsConstructor
public class PostBookmarkController {

 private final PostBookmarkService bookmarkService;

 @Operation(
         summary = "게시물 북마크 등록",
         description = "현재 유저가 지정된 게시물을 북마크에 추가합니다."
 )
 @ApiResponse(
         responseCode = "200",
         description = "북마크 등록 성공",
         content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
 )
 @PostMapping("/posts/{postId}/bookmark")
 public ResponseTemplate<Void> add(
         @AuthenticationPrincipal User user,
         @PathVariable Long postId
 ) {
  bookmarkService.addBookmark(postId, user);
  return ResponseTemplate.ok();
 }


 @Operation(
         summary = "게시물 북마크 해제",
         description = "현재 유저가 지정된 게시물을 북마크에서 제거합니다."
 )
 @ApiResponse(
         responseCode = "200",
         description = "북마크 해제 성공",
         content = @Content(schema = @Schema(implementation = ResponseTemplate.class))
 )
 @DeleteMapping("/posts/{postId}/bookmark")
 public ResponseTemplate<Void> remove(
         @AuthenticationPrincipal User user,
         @PathVariable Long postId
 ) {
  bookmarkService.removeBookmark(postId, user);
  return ResponseTemplate.ok();
 }


 @Operation(
         summary = "내 북마크 목록 조회",
         description = "현재 유저가 북마크한 게시물 목록을 페이징하여 반환합니다."
 )
 @ApiResponse(
         responseCode = "200",
         description = "북마크 목록 조회 성공",
         content = @Content(
                 mediaType = "application/json",
                 schema = @Schema(implementation = BookmarkResponsePage.class)
         )
 )
 @GetMapping("/users/me/bookmarks")
 public ResponseTemplate<BookmarkResponsePage> list(
         @AuthenticationPrincipal User user,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size
 ) {
  BookmarkResponsePage dto = bookmarkService.getMyBookmarks(user.getId(), page, size);
  return ResponseTemplate.ok(dto);
 }
}
