package com.adminzone.service;

import com.adminzone.dto.CourseRequest;
import com.adminzone.dto.CourseResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.entity.Course;
import com.adminzone.exception.ResourceNotFoundException;
import com.adminzone.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> findAll(String query, Integer semester, Pageable pageable) {
        Page<Course> page = courseRepository.search(query, semester, pageable);
        List<CourseResponse> data = page.getContent().stream()
                .map(CourseResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
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
    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curs", id));
        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = Course.builder()
                .denumire(request.getDenumire())
                .profesorTitular(request.getProfesorTitular())
                .nrCredite(request.getNrCredite())
                .semester(request.getSemester())
                .build();

        course = courseRepository.save(course);
        auditService.log("CREATE", "Course", course.getId(), request);

        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curs", id));

        course.setDenumire(request.getDenumire());
        course.setProfesorTitular(request.getProfesorTitular());
        course.setNrCredite(request.getNrCredite());
        course.setSemester(request.getSemester());

        course = courseRepository.save(course);
        auditService.log("UPDATE", "Course", id, request);

        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curs", id));

        CourseResponse before = CourseResponse.fromEntity(course);
        courseRepository.delete(course);
        auditService.log("DELETE", "Course", id, before);
    }

    @Transactional(readOnly = true)
    public List<Course> findAllEntities() {
        return courseRepository.findAll();
    }
}
