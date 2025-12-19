package com.adminzone.service;

import com.adminzone.dto.EnrollmentRequest;
import com.adminzone.dto.EnrollmentResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.entity.Course;
import com.adminzone.entity.Enrollment;
import com.adminzone.entity.EnrollmentId;
import com.adminzone.entity.Student;
import com.adminzone.exception.DuplicateResourceException;
import com.adminzone.exception.ResourceNotFoundException;
import com.adminzone.repository.CourseRepository;
import com.adminzone.repository.EnrollmentRepository;
import com.adminzone.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> findAll(Pageable pageable) {
        Page<Enrollment> page = enrollmentRepository.findAllWithDetails(pageable);
        List<EnrollmentResponse> data = page.getContent().stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<EnrollmentResponse>builder()
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
    public List<EnrollmentResponse> findByFilters(Long studentId, Long courseId) {
        return enrollmentRepository.findByFilters(studentId, courseId).stream()
                .map(EnrollmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse findById(Long studentId, Long courseId) {
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Înscriere pentru studentul " + studentId + " la cursul " + courseId + " nu a fost găsită"));
        return EnrollmentResponse.fromEntity(enrollment);
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        EnrollmentId id = new EnrollmentId(request.getStudentId(), request.getCourseId());
        
        // Check for duplicate
        if (enrollmentRepository.existsById(id)) {
            throw new DuplicateResourceException("Studentul este deja înscris la acest curs");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curs", request.getCourseId()));

        Enrollment enrollment = Enrollment.builder()
                .id(id)
                .student(student)
                .course(course)
                .notaFinala(request.getNotaFinala())
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        auditService.log("CREATE", "Enrollment", null, request);

        return EnrollmentResponse.fromEntity(enrollment);
    }

    @Transactional
    public EnrollmentResponse update(Long studentId, Long courseId, EnrollmentRequest request) {
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Înscriere pentru studentul " + studentId + " la cursul " + courseId + " nu a fost găsită"));

        enrollment.setNotaFinala(request.getNotaFinala());
        enrollment = enrollmentRepository.save(enrollment);
        auditService.log("UPDATE", "Enrollment", null, request);

        return EnrollmentResponse.fromEntity(enrollment);
    }

    @Transactional
    public void delete(Long studentId, Long courseId) {
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Înscriere pentru studentul " + studentId + " la cursul " + courseId + " nu a fost găsită"));

        EnrollmentResponse before = EnrollmentResponse.fromEntity(enrollment);
        enrollmentRepository.delete(enrollment);
        auditService.log("DELETE", "Enrollment", null, before);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findAllEntities() {
        return enrollmentRepository.findAll();
    }
}
