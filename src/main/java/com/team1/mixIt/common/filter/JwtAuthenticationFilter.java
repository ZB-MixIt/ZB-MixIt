package com.team1.mixIt.common.filter;

import com.team1.mixIt.common.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

//        final String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return true;
//        }

        if (path.startsWith("/api/v1/login")
                || path.startsWith("/api/v1/logout")
                || path.startsWith("/api/v1/auth/kakao")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")){
            return true;
    }
        if ("GET".equalsIgnoreCase(method)) {
            if (path.matches("/api/v1/posts/\\d+/like")) {
                return false;
            }
            // 그 외 공개 GET
            if (path.startsWith("/api/v1/home")
                    || path.startsWith("/api/v1/posts/")
                    || path.equals("/api/v1/posts/search")
                    || path.startsWith("/api/v1/tags")) {
                return true;
            }
        }
    String authHeader = request.getHeader("Authorization");
    return authHeader == null || !authHeader.startsWith("Bearer ");
}

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
//        String path = request.getRequestURI();
//        String method = request.getMethod();

//        if (path.startsWith("/api/v1/login")
//                || path.startsWith("/api/v1/logout")
//                || path.startsWith("/api/v1/auth/kakao")
//                || path.startsWith("/api/v1/accounts")
//                || path.startsWith("/swagger-ui")
//                || path.startsWith("/v3/api-docs")) {
//            filterChain.doFilter(request, response);
//            return;
//        }

//        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/home")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        if ("GET".equalsIgnoreCase(method)) {
//            if (path.startsWith("/api/v1/posts/")
//                    || path.equals("/api/v1/posts/search")) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//        }
//
//        if ("GET".equalsIgnoreCase(method) &&
//                (path.startsWith("/api/v1/tags/popular")
//                        || path.startsWith("/api/v1/tags/autocomplete"))) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        final String authHeader = request.getHeader("Authorization");

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);


        }
    }
}
