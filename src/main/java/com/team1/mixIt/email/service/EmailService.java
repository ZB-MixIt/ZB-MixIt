package com.team1.mixIt.email.service;

import com.team1.mixIt.common.config.CacheConfig;
import com.team1.mixIt.email.exception.EmailNotVerifiedException;
import com.team1.mixIt.email.exception.EmailVerificationCodeNotMatch;
import com.team1.mixIt.email.exception.EmailVerificationHistoryNotFound;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class EmailService {

    private final EmailClient emailClient;
    private final Cache emailCache;

    private static final String EMAIL_VERIFICATION_SUBJECT = "[MixIt] 이메일 인증";
    private static final String EMAIL_VERIFICATION_BODY_FORMAT = "이메일 인증번호: %s";

    private static final String PASSWORD_RESET_SUBJECT = "[MixIt] 비밀번호 초기화";
    private static final String PASSWORD_RESET_BODY_FORMAT = "비밀번호: %s";

    public EmailService(EmailClient client, CacheManager cacheManager) {
        this.emailClient = client;
        this.emailCache = cacheManager.getCache(CacheConfig.EMAIL_VERIFICATION_CACHE_NAME);
    }

    public void sendPasswordResetEmail(String email, String pwd) {
        String body = String.format(PASSWORD_RESET_BODY_FORMAT, pwd);
        emailClient.sendEmail(PASSWORD_RESET_SUBJECT, body, email);
    }

    public void sendVerificationEmail(String email) {
        String random = UUID.randomUUID().toString().substring(0, 6);
        String body = String.format(EMAIL_VERIFICATION_BODY_FORMAT, random);

        emailClient.sendEmail(EMAIL_VERIFICATION_SUBJECT, body, email);
        EmailVerificationData data = new EmailVerificationData();
        data.setVerified(false);
        data.setCode(random);
        emailCache.put(email, data);
    }

    public void verifyEmail(String email, String code) {
        EmailVerificationData data = getVerifyEmailData(email);

        if (Objects.isNull(data)) throw new EmailVerificationHistoryNotFound();
        if (!data.getCode().equals(code)) throw new EmailVerificationCodeNotMatch();

        data.setVerified(true);
        emailCache.put(email, data);
    }

    public void checkIsEmailVerified(String email) {
        EmailVerificationData data = getVerifyEmailData(email);
        if (data != null && data.isVerified()) throw new EmailNotVerifiedException();
    }

    public void deleteVerifiedHistory(String email) {
        emailCache.evict(email);
    }

    public EmailVerificationData getVerifyEmailData(String email) {
        return emailCache.get(email, EmailVerificationData.class);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailVerificationData {
        private String code;
        private boolean verified;
    }
}
