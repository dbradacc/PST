package com.adminzone.dto;

import com.adminzone.entity.AuditLog;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private String username;
    private String ip;
    private String action;
    private String entity;
    private Long entityId;
    private String payloadJson;
    private LocalDateTime timestamp;

    public static AuditLogResponse fromEntity(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .ip(log.getIp())
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .payloadJson(log.getPayloadJson())
                .timestamp(log.getTimestamp())
                .build();
    }
}
