package com.team1.mixIt.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class BannedWordsConfig {

    @Value("classpath:banned-words.txt")
    private Resource resource;

    private Set<String> bannedWords = new HashSet<>();

    @PostConstruct
    public void load() throws Exception {
        try (InputStream input = resource.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
                 String line;
                 while ((line = br.readLine()) != null) {
                     bannedWords.add(line.strip().toLowerCase());
                 }
            }
        }

        public boolean containsBanned(String text) {
            String lower = text.toLowerCase();
            return bannedWords.stream().anyMatch(lower::contains);
        }
}