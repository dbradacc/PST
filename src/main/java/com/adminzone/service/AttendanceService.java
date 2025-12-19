package com.adminzone.service;

import com.adminzone.dto.AttendanceRequest;
import com.adminzone.dto.AttendanceResponse;
import com.adminzone.dto.AttendanceStatsResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.entity.Attendance;
import com.adminzone.entity.Course;
import com.adminzone.entity.Student;
import com.adminzone.exception.BusinessRuleException;
import com.adminzone.exception.ResourceNotFoundException;
import com.adminzone.repository.AttendanceRepository;
import com.adminzone.repository.CourseRepository;
import com.adminzone.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AuditService auditService;

    @Value("${app.max-attendance-per-semester:14}")
    private int maxAttendancePerSemester;

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> findAll(Pageable pageable) {
        Page<Attendance> page = attendanceRepository.findAllWithDetails(pageable);
        List<AttendanceResponse> data = page.getContent().stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<AttendanceResponse>builder()
                .data(data)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> search(String studentName, String courseName, Integer semester) {
        return attendanceRepository.search(studentName, courseName, semester).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceResponse findById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prezență", id));
        return AttendanceResponse.fromEntity(attendance);
    }

    @Transactional
    public AttendanceResponse create(AttendanceRequest request) {
        // Validate student and course exist
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curs", request.getCourseId()));

        // Business rule: max 14 attendances per semester per student/course
        long currentCount = attendanceRepository.countByStudentAndCourseAndSemester(
                request.getStudentId(), request.getCourseId(), request.getSemester());
        
        if (currentCount >= maxAttendancePerSemester) {
            throw new BusinessRuleException(
                    "Studentul a atins limita maximă de " + maxAttendancePerSemester + 
                    " prezențe pentru acest curs în semestrul " + request.getSemester());
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .course(course)
                .data(request.getData())
                .semester(request.getSemester())
                .status(request.getStatus())
                .build();

        attendance = attendanceRepository.save(attendance);
        auditService.log("CREATE", "Attendance", attendance.getId(), request);

        return AttendanceResponse.fromEntity(attendance);
    }

    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prezență", id));

        // If changing student/course/semester, check the limit
        boolean needsCheck = !attendance.getStudent().getId().equals(request.getStudentId()) ||
                            !attendance.getCourse().getId().equals(request.getCourseId()) ||
                            !attendance.getSemester().equals(request.getSemester());

        if (needsCheck) {
            long currentCount = attendanceRepository.countByStudentAndCourseAndSemester(
                    request.getStudentId(), request.getCourseId(), request.getSemester());
            
            if (currentCount >= maxAttendancePerSemester) {
                throw new BusinessRuleException(
                        "Studentul a atins limita maximă de " + maxAttendancePerSemester + 
                        " prezențe pentru acest curs în semestrul " + request.getSemester());
            }

            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Curs", request.getCourseId()));
            
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setSemester(request.getSemester());
        }

        attendance.setData(request.getData());
        attendance.setStatus(request.getStatus());

        attendance = attendanceRepository.save(attendance);
        auditService.log("UPDATE", "Attendance", id, request);

        return AttendanceResponse.fromEntity(attendance);
    }

    @Transactional
    public void delete(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prezență", id));

        AttendanceResponse before = AttendanceResponse.fromEntity(attendance);
        attendanceRepository.delete(attendance);
        auditService.log("DELETE", "Attendance", id, before);
    }

    @Transactional(readOnly = true)
    public List<AttendanceStatsResponse> getStatistics() {
        return attendanceRepository.getStatistics().stream()
                .map(row -> AttendanceStatsResponse.builder()
                        .studentId((Long) row[0])
                        .studentName((String) row[1])
                        .semester1Count((Long) row[2])
                        .semester2Count((Long) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Attendance> findAllEntities() {
        return attendanceRepository.findAll();
    }
}
