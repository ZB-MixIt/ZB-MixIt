package com.team1.mixIt.post.service.impl;

import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostSearchRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostLikeRepository;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.service.PostBookmarkService;
import com.team1.mixIt.post.service.PostRatingService;
import com.team1.mixIt.post.service.PostSearchService;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import com.team1.mixIt.utils.ImageUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {
    @Value("${mixit.default-image-url}")
    private String defaultImageUrl;

    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostBookmarkService postBookmarkService;
    private final PostRatingService ratingService;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> search(PostSearchRequest req) {
        // Pageable 생성: sortBy, sortDir, page, size 사용
        Sort sort = Sort.by(
                Sort.Direction.fromString(req.getSortDir()),
                req.getSortBy().equals("views7d") ? "viewsLast7Days" : req.getSortBy()
        );
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        // Specification 생성: keyword + category
        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
                String likePattern = "%" + req.getKeyword().trim() + "%";
                Predicate titleMatch = cb.like(root.get("title"), likePattern);
                Join<Post, ?> hashtagJoin = root.join("hashtag", JoinType.LEFT);
                Predicate tagMatch = cb.like(hashtagJoin.get("hashtag"), likePattern);
                preds.add(cb.or(titleMatch, tagMatch));
                query.distinct(true);
            }

            if (req.getCategory() != null) {
                preds.add(cb.equal(root.get("category"), req.getCategory()));
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };

        // 실제 조회 후 Page<PostResponse>로 매핑
        return postRepository.findAll(spec, pageable)
                .map(post -> toDto(post, /* 로그인된 사용자 ID가 있으면 이 쪽에 넣습니다 */ null));
    }

    /**
     * HomeFeedService의 toDto(...)와 동일한 로직.
     * 검색 결과에서도 “이미지/작성자 프로필·닉네임/별점/하트/북마크” 등을 모두 채우려면 이 helper를 사용하세요.
     */
    private PostResponse toDto(Post p, Long currentUserId) {
        // 게시물에 첨부된 이미지 리스트(ImageDto) 생성
        List<PostResponse.ImageDto> imgDtos = p.getImageIds().stream()
                .map(imageService::findById)
                .map(img -> new PostResponse.ImageDto(img.getId(), img.getUrl()))
                .toList();

        // 대표 이미지 URL: 첫 번째 이미지가 있으면 그 URL, 없으면 defaultImageUrl
        String defaultImg;
        if (!p.getImageIds().isEmpty()) {
            Long firstImageId = p.getImageIds().get(0);
            defaultImg = imageService.findById(firstImageId).getUrl();
        } else {
            defaultImg = ImageUtils.getDefaultImageUrl();
        }

        // 좋아요 수와 현재 사용자가 눌렀는지 여부
        long likeCount = postLikeRepository.countByPostId(p.getId());
        boolean hasLiked = (currentUserId != null) &&
                postLikeRepository.findByPostIdAndUserId(p.getId(), currentUserId).isPresent();

        // 별점 정보
        RatingResponse ratingResp = ratingService.getRatingResponse(p.getId());

        // 작성자 정보: User 엔티티에서 닉네임과 프로필 이미지 조회
        User author = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new IllegalStateException("작성자 정보 없음"));
        String authorNickname = author.getNickname();
        String authorProfileImage = null;
        if (author.getProfileImage() != null) {
            authorProfileImage = author.getProfileImage().getUrl();
        }

        // 북마크 여부
        boolean hasBookmarked = (currentUserId != null) &&
                postBookmarkService.isBookmarked(p.getId(), currentUserId);

        //작성자 여부 판정
        boolean isAuthor = (currentUserId != null) && p.getUserId().equals(currentUserId);

        // 최종 빌드
        return PostResponse.fromEntity(
                p,
                currentUserId,
                defaultImg,
                imageService,
                postBookmarkService,
                ratingResp,
                likeCount,
                hasLiked
        );
    }
}
