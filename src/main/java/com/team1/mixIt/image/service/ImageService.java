package com.team1.mixIt.image.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.post.entity.Post;
import com.team1.mixIt.post.entity.Review;
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

    List<Image> findAllById(List<Long> ids);

    void updateAssignedImages(List<Long> originalIds, List<Long> newIds);

}
