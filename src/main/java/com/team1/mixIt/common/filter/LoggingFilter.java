package com.team1.mixIt.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String TX_ID = "request_id";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String txId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TX_ID, txId);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        long duration = System.currentTimeMillis() - startTime;


        // 요청 로그
        String requestBody = getPayload(wrappedRequest.getContentAsByteArray());
        String reqHeaders = getHeadersString(wrappedRequest);
        log.info("\n[REQ]  {} {} \nHeaders: {}\nBody: {}",
                request.getMethod(),
                request.getRequestURI(),
                reqHeaders,
                requestBody
        );

        // 응답 로그
        wrappedResponse.setHeader("tx-id", txId);
        String responseBody = getPayload(wrappedResponse.getContentAsByteArray());
        log.info("\n[RES]  status={} \nHeaders: {}\nBody: {} ({}ms)",
                wrappedResponse.getStatus(),
                wrappedResponse.getHeaderNames(),
                responseBody,
                duration
        );
        wrappedResponse.copyBodyToResponse();
    }

    private String getPayload(byte[] buf) {
        if (buf == null || buf.length == 0) return "";
        // 최대 크기 제한 등 추가 처리 가능
        return new String(buf, StandardCharsets.UTF_8);
    }

    private String getHeadersString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        var names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            var values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                sb.append(name).append(": ").append(values.nextElement()).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
