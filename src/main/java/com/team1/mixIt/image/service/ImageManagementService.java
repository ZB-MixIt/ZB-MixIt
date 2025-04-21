package com.team1.mixIt.image.service;

import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageManagementService {

    private final ImageRepository imageRepository;
    private final ImageService imageService;

    /**
     * 10분 전 생성된, 소유자 미지정 이미지 삭제
     * (매 1분마다 실행)
     */
    @Scheduled(fixedRate = 60_000)
    public void taskDeleteUnusedImage() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Image> images = imageRepository.findByUserIsNullAndCreatedAtBefore(cutoff);
        images.forEach(imageService::delete);
        if (!images.isEmpty()) {
            log.info("Deleted images: {}", images.stream().map(Image::getId).collect(Collectors.toList()));
        }
    }
}
