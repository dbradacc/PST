package com.adminzone.service;

import com.adminzone.entity.AuditLog;
import com.adminzone.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(String action, String entity, Long entityId, Object payload) {
        String username = getCurrentUsername();
        String ip = getCurrentIp();
        String payloadJson = serializePayload(payload);

        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .ip(ip)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .payloadJson(payloadJson)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit: {} {} {} by {} from {}", action, entity, entityId, username, ip);
    }

    @Transactional
    public void logAuth(String action, String username, String ip) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .ip(ip)
                .action(action)
                .entity("Auth")
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Auth audit: {} for user {} from {}", action, username, ip);
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String getCurrentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not determine IP address", e);
        }
        return "unknown";
    }

    private String serializePayload(Object payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize audit payload", e);
            return payload.toString();
        }
    }
}
