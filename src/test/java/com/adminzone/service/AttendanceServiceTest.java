package com.adminzone.service;

import com.adminzone.dto.AttendanceRequest;
import com.adminzone.entity.Attendance;
import com.adminzone.entity.Course;
import com.adminzone.entity.Student;
import com.adminzone.exception.BusinessRuleException;
import com.adminzone.repository.AttendanceRepository;
import com.adminzone.repository.CourseRepository;
import com.adminzone.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void shouldRejectAttendanceWhenMaxReached() {
        // Set max attendance to 14
        ReflectionTestUtils.setField(attendanceService, "maxAttendancePerSemester", 14);

        // Mock: already 14 attendances exist
        when(attendanceRepository.countByStudentAndCourseAndSemester(1L, 1L, 1))
                .thenReturn(14L);

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(Student.builder().id(1L).build()));
        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(Course.builder().id(1L).build()));

        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .data(LocalDate.now())
                .semester(1)
                .status("prezent")
                .build();

        // Should throw BusinessRuleException
        assertThrows(BusinessRuleException.class, () -> {
            attendanceService.create(request);
        });
    }

    @Test
    void shouldAllowAttendanceWhenUnderLimit() {
        ReflectionTestUtils.setField(attendanceService, "maxAttendancePerSemester", 14);

        // Mock: only 13 attendances exist
        when(attendanceRepository.countByStudentAndCourseAndSemester(1L, 1L, 1))
                .thenReturn(13L);

        Student student = Student.builder()
                .id(1L)
                .nume("Test")
                .prenume("Student")
                .build();
        Course course = Course.builder()
                .id(1L)
                .denumire("Test Course")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Attendance savedAttendance = Attendance.builder()
                .id(1L)
                .student(student)
                .course(course)
                .data(LocalDate.now())
                .semester(1)
                .status("prezent")
                .build();

        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any());

        AttendanceRequest request = AttendanceRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .data(LocalDate.now())
                .semester(1)
                .status("prezent")
                .build();

        // Should not throw
        assertDoesNotThrow(() -> attendanceService.create(request));
    }
}
