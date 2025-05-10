package com.team1.mixIt.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUtils {
    private static String DEFAULT_IMG_URL;
    private ImageUtils(@Value("${mixit.default-image-url}") String defaultImg) {
        ImageUtils.DEFAULT_IMG_URL = defaultImg;
    }

    public static String getDefaultImageUrl() {
        return ImageUtils.DEFAULT_IMG_URL;
    }
}
