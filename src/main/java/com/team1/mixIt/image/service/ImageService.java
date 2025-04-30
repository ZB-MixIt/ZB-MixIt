package com.team1.mixIt.image.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    Image create(MultipartFile image);

    Image create(MultipartFile image, String loginId);

    void delete(Long id, User user);

    void delete(Image image, User user);

    void delete(Image image);

    void setOwner(Image image, User user);

    void setOwner(List<Image> image,User user);
    // 주어진 이미지 ID목록으로 Image엔티티 한번에 조회
    List<Image> findAllById(List<Long> ids);
    // 특정 게시물과 연결된 이미지 post 외래키 해제
    void unassignAllFromPost(Long postId);

    // Todo 유저가 할당되지 않은 Image 에 대해서 기간 지나면 삭제하는 로직 필요.
}
