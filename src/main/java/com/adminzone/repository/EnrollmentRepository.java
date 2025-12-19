package com.adminzone.repository;

import com.adminzone.entity.Enrollment;
import com.adminzone.entity.EnrollmentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.student " +
           "JOIN FETCH e.course " +
           "ORDER BY e.student.nume, e.student.prenume, e.course.denumire")
    Page<Enrollment> findAllWithDetails(Pageable pageable);

    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.student s " +
           "JOIN FETCH e.course c " +
           "WHERE (:studentId IS NULL OR s.id = :studentId) " +
           "AND (:courseId IS NULL OR c.id = :courseId)")
    List<Enrollment> findByFilters(@Param("studentId") Long studentId,
                                    @Param("courseId") Long courseId);
}
