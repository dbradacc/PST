package com.adminzone.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatsResponse {

    private Long studentId;
    private String studentName;
    private Long semester1Count;
    private Long semester2Count;
}
