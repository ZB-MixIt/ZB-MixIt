package com.team1.mixIt.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.team1.mixIt.image.entity.Image;
import com.team1.mixIt.image.repository.ImageRepository;
import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Override
    public Image create(MultipartFile image) {
        return this.create(image, null);
    }

    @Override
    public Image create(MultipartFile file, String loginId) {
        if (file.isEmpty() || Objects.isNull(file.getOriginalFilename())) {
            throw new RuntimeException(); // Todo Exception 정의
        }

        if(!this.validateExtension(file.getOriginalFilename())) throw new RuntimeException(); // Todo Exception 정의

        String src;
        try {
            src = uploadS3(file);
        } catch (IOException e) {
            throw new RuntimeException(); // Todo Exception 정의
        }

        Image.ImageBuilder builder = Image.builder()
                .url(src);
        userRepository.findByLoginId(loginId).ifPresent(builder::user);
        Image image = builder.build();

        // Todo save 실패에 따른 처리 로직 필요
        return imageRepository.save(image);
    }

    @Override
    public void delete(Long imageId, User user) {
        Image image = imageRepository.findById(imageId).orElseThrow(); // Todo Exception 정의
        delete(image, user);
    }

    @Override
    public void delete(Image image, User user) {
        if (image.getUser() != user) throw new RuntimeException(); // Todo Exception 정의
        delete(image);
    }

    @Override
    public void delete(Image image) {
        String key = getKey(image.getUrl());
        imageRepository.delete(image);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception ex) {
            log.error("Failed to delete S3 Image. id: {}, url={}", image.getId(), image.getUrl(), ex);
        }
    }

    @Override
    public void setOwner(Image image, User user) {
        image.updateUser(user);
        imageRepository.save(image);
    }

    @Override
    public void setOwner(List<Image> images, User user) {
        images.forEach(image -> {image.updateUser(user);});
        imageRepository.saveAll(images);
    }

    private String uploadS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename;

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try{
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest);
        }catch (Exception e){
            throw new RuntimeException(e); // Todo Exception 정의
        }finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    private boolean validateExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return false;
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        return allowedExtentionList.contains(extension);
    }

    private String getKey(String urlStr) {
        try{
            URL url = new URL(urlStr);
            String decodingKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
            return decodingKey.substring(1);
        }catch (MalformedURLException e){
            throw new RuntimeException(e); // Todo Exception 정의
        }
    }

    @Override
    public List<Image> findAllById(List<Long> ids) {
        return imageRepository.findAllById(ids);
    }

    @Override
    public void unassignAllFromPost(Long postId) {
        List<Image> images = imageRepository.findByPostId(postId);
        for (Image img : images) {
            img.updateReview(null);
            img.updateUser(null);
        }
        imageRepository.saveAll(images);
    }
}
