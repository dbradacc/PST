package com.adminzone.repository;

import com.adminzone.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findBySemester(Integer semester, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(c.denumire) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.profesorTitular) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:semester IS NULL OR c.semester = :semester)")
    Page<Course> search(@Param("query") String query,
                        @Param("semester") Integer semester,
                        Pageable pageable);
}
