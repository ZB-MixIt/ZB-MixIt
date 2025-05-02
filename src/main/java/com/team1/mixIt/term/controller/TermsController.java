package com.team1.mixIt.term.controller;

import com.team1.mixIt.common.dto.ResponseTemplate;
import com.team1.mixIt.term.entity.Terms;
import com.team1.mixIt.term.entity.TermsType;
import com.team1.mixIt.term.service.TermsService;
import com.team1.mixIt.term.service.UserTermsService;
import com.team1.mixIt.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;
    private final UserTermsService userTermsService;

    @GetMapping
    @Operation(
            summary = "Get all terms",
            description = "등록된 모든 약관을 조회하는 API"
    )
    public ResponseTemplate<GetAllTermsResponse> getAllTerms() {
        List<Terms> termsList = termsService.getAllTerms();
        return ResponseTemplate.ok(GetAllTermsResponse.of(termsList));
    }

    @GetMapping("/agree")
    @Operation(
            summary = "Get user agreed terms",
            description = "유저의 동의 약관 목록 조회 API"
    )
    public ResponseTemplate<GetAgreedTermsResponse> getUserAgreedTerms(@AuthenticationPrincipal User user) {
        List<Terms> termList = userTermsService.getAgreedTerms(user);
        return ResponseTemplate.ok(GetAgreedTermsResponse.of(termList));
    }

    @PostMapping("/agree")
    @Operation(
            summary = "Agree terms",
            description = "유저 약관 동의 API"
    )
    public ResponseTemplate<Void> agreeTerms(@AuthenticationPrincipal User user,
                                             @RequestBody AgreeTermsRequest request) {
        userTermsService.agreeTerms(request.getTerms(), user);
        return ResponseTemplate.ok();
    }

    @PostMapping("/disagree")
    @Operation(
        summary = "Disagree terms",
            description = "유저 약관 동의 해제 API"
    )
    public ResponseTemplate<Void> disagreeTerms(@AuthenticationPrincipal User user,
                                                @RequestBody DisagreeTermsRequest request) {
        userTermsService.disagreeTerms(request.getTerms(), user);
        return ResponseTemplate.ok();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetAgreedTermsResponse {
        private List<Integer> terms;

        public static GetAgreedTermsResponse of(List<Terms> terms) {
            return GetAgreedTermsResponse.builder()
                    .terms(terms.stream().map(Terms::getId).collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DisagreeTermsRequest {
        private List<Integer> terms;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AgreeTermsRequest {
        private List<Integer> terms;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetAllTermsResponse {
        private List<TermsData> terms;

        public static GetAllTermsResponse of(List<Terms> terms) {
            return builder()
                    .terms(terms.stream().map(TermsData::of).collect(Collectors.toList()))
                    .build();
        }

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class TermsData {
            private Integer id;
            private TermsType type;
            private Boolean required;
            private LocalDateTime createdAt;
            private LocalDateTime modifiedAt;

            public static TermsData of(Terms terms) {
                return TermsData.builder()
                        .id(terms.getId())
                        .type(terms.getType())
                        .required(terms.getRequired())
                        .createdAt(terms.getCreatedAt())
                        .modifiedAt(terms.getModifiedAt())
                        .build();
            }
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUserAgreedTerms {
        private List<TermsData> terms;

        public static GetUserAgreedTerms of(List<Terms> terms) {
            return builder()
                    .terms(terms.stream().map(TermsData::of).collect(Collectors.toList()))
                    .build();
        }

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class TermsData {
            private Integer id;
            private TermsType type;
            private LocalDateTime createdAt;
            private LocalDateTime modifiedAt;

            public static TermsData of(Terms terms) {
                return TermsData.builder()
                        .id(terms.getId())
                        .type(terms.getType())
                        .createdAt(terms.getCreatedAt())
                        .modifiedAt(terms.getModifiedAt())
                        .build();
            }
        }
    }
}
