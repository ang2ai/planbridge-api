package com.planbridge.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void register(String requestId, SseEmitter emitter) {
        emitters.put(requestId, emitter);
        log.debug("SSE emitter registered: {}", requestId);
    }

    public void notifyCompleted(String targetId, Object data) {
        SseEmitter emitter = emitters.get(targetId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("analysis-complete")
                        .data(data));
                emitter.complete();
                log.info("SSE notification sent and completed for: {}", targetId);
            } catch (IOException e) {
                log.warn("SSE notification failed for {}: {}", targetId, e.getMessage());
                emitter.completeWithError(e);
            } finally {
                emitters.remove(targetId);
            }
        } else {
            log.debug("No active SSE emitter found for targetId: {}", targetId);
        }
    }

    public void remove(String requestId) {
        emitters.remove(requestId);
    }
}
