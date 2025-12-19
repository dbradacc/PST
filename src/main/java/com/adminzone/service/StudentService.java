package com.adminzone.service;

import com.adminzone.dto.StudentRequest;
import com.adminzone.dto.StudentResponse;
import com.adminzone.dto.PageResponse;
import com.adminzone.entity.Student;
import com.adminzone.exception.DuplicateResourceException;
import com.adminzone.exception.ResourceNotFoundException;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> findAll(String query, Integer anStudiu, Pageable pageable) {
        Page<Student> page = studentRepository.search(query, anStudiu, pageable);
        List<StudentResponse> data = page.getContent().stream()
                .map(StudentResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<StudentResponse>builder()
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
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un student cu acest email există deja");
        }

        Student student = Student.builder()
                .nume(request.getNume())
                .prenume(request.getPrenume())
                .email(request.getEmail())
                .telefon(request.getTelefon())
                .anStudiu(request.getAnStudiu())
                .build();

        student = studentRepository.save(student);
        auditService.log("CREATE", "Student", student.getId(), request);
        
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        // Check for email uniqueness if email is being changed
        if (!student.getEmail().equals(request.getEmail()) && 
            studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un student cu acest email există deja");
        }

        student.setNume(request.getNume());
        student.setPrenume(request.getPrenume());
        student.setEmail(request.getEmail());
        student.setTelefon(request.getTelefon());
        student.setAnStudiu(request.getAnStudiu());

        student = studentRepository.save(student);
        auditService.log("UPDATE", "Student", id, request);

        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        
        StudentResponse before = StudentResponse.fromEntity(student);
        studentRepository.delete(student);
        auditService.log("DELETE", "Student", id, before);
    }

    @Transactional(readOnly = true)
    public List<Student> findAllEntities() {
        return studentRepository.findAll();
    }
}
