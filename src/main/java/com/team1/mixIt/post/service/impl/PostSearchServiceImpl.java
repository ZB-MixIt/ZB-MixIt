package com.team1.mixIt.post.service.impl;

import com.team1.mixIt.image.service.ImageService;
import com.team1.mixIt.post.dto.request.PostSearchRequest;
import com.team1.mixIt.post.dto.response.PostResponse;
import com.team1.mixIt.post.dto.response.RatingResponse;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.repository.PostRepository;
import com.team1.mixIt.post.service.PostBookmarkService;
import com.team1.mixIt.post.service.PostRatingService;
import com.team1.mixIt.post.service.PostSearchService;
import com.team1.mixIt.utils.ImageUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.team1.mixIt.post.service.PostService.DEFAULT_IMAGE_URL;

@Service
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {

    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostBookmarkService postBookmarkService;
    private final PostRatingService ratingService;

    @Override
    public Page<PostResponse> search(PostSearchRequest req) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(req.getSortDir()),
                req.getSortBy().equals("views7d") ? "viewsLast7Days" : req.getSortBy()
        );
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (req.getKeyword() != null) {
                String like = "%" + req.getKeyword() + "%";
                Predicate titleMatch = cb.like(root.get("title"), like);
                Join<Post, ?> tagJoin = root.join("hashtag", JoinType.LEFT);
                Predicate tagMatch = cb.like(tagJoin.get("hashtag"), like);
                preds.add(cb.or(titleMatch, tagMatch));
                query.distinct(true);
            }
            if (req.getCategory() != null) {
                preds.add(cb.equal(root.get("category"), req.getCategory()));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };


        return postRepository.findAll(spec, pageable)
                .map(p -> {
                    RatingResponse rating = ratingService.getRatingResponse(p.getId());
                    return PostResponse.fromEntity(
                            p,
                            null,
                            DEFAULT_IMAGE_URL,
                            imageService,
                            postBookmarkService,
                            rating
                    );
                });
    }
}
