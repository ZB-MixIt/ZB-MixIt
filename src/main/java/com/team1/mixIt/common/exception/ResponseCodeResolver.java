package com.team1.mixIt.common.exception;

import com.team1.mixIt.common.code.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ResponseCodeResolver {

    private final Map<Class<? extends Exception>, ResponseCode> cache;

    public ResponseCodeResolver() {
        this.cache = new HashMap<>();

        for (ResponseCode responseCode : ResponseCode.values()) {
            if (Objects.isNull(responseCode.getExceptions())) continue;
            for (Class<? extends Exception> ex : responseCode.getExceptions()) {
                if (cache.containsKey(ex)) throw new ResponseCodeAlreadyMappedException(ex);
                cache.put(ex, responseCode);
            }
        }
    }

    public ResponseCode resolve(Exception exception) {
        ResponseCode responseCode = cache.getOrDefault(exception.getClass(), ResponseCode.INTERNAL_SERVER_ERROR);
        log.debug("{} resolved by {}", responseCode.name(), exception.getClass().getSimpleName());
        return responseCode;
    }

    public static class ResponseCodeAlreadyMappedException extends RuntimeException {
        public ResponseCodeAlreadyMappedException(Class<? extends Exception> ex) {
            super(ex.getName());
        }
    }
}
