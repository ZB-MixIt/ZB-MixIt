package com.team1.mixIt.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class BannedWordsConfig {

    // 리소스에 넣어둔 텍스트 파일 이름
    private static final String BANNED_WORDS_FILE = "banned-words.txt";

    // 실제 금칙어 목록이 담길 리스트
    private final List<String> bannedWords = new ArrayList<>();


    @PostConstruct
    public void loadBannedWords() {
        Resource resource = new ClassPathResource(BANNED_WORDS_FILE);
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                bannedWords.add(trimmed);
            }
        } catch (IOException e) {
            throw new RuntimeException("금칙어 목록을 불러오는 중에 오류가 발생했습니다: " + BANNED_WORDS_FILE, e);
        }
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }


    public boolean containsBanned(String text) {
        if (text == null) {
            return false;
        }
        for (String banned : bannedWords) {
            if (text.contains(banned)) {
                return true;
            }
        }
        return false;
    }
}
