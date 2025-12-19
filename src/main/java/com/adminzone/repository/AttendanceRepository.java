package com.adminzone.repository;

import com.adminzone.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT COUNT(a) FROM Attendance a " +
           "WHERE a.student.id = :studentId " +
           "AND a.course.id = :courseId " +
           "AND a.semester = :semester")
    long countByStudentAndCourseAndSemester(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId,
                                             @Param("semester") Integer semester);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student " +
           "JOIN FETCH a.course " +
           "ORDER BY a.data DESC")
    Page<Attendance> findAllWithDetails(Pageable pageable);

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH a.course c " +
           "WHERE (:studentName IS NULL OR :studentName = '' OR " +
           "       LOWER(s.nume) LIKE LOWER(CONCAT('%', :studentName, '%'))) " +
           "AND (:courseName IS NULL OR :courseName = '' OR " +
           "     LOWER(c.denumire) LIKE LOWER(CONCAT('%', :courseName, '%'))) " +
           "AND (:semester IS NULL OR a.semester = :semester) " +
           "ORDER BY a.data DESC")
    List<Attendance> search(@Param("studentName") String studentName,
                            @Param("courseName") String courseName,
                            @Param("semester") Integer semester);

    // Statistics: count attendances per student per semester
    @Query("SELECT s.id, CONCAT(s.nume, ' ', s.prenume) as studentName, " +
           "SUM(CASE WHEN a.semester = 1 AND a.status = 'prezent' THEN 1 ELSE 0 END) as sem1, " +
           "SUM(CASE WHEN a.semester = 2 AND a.status = 'prezent' THEN 1 ELSE 0 END) as sem2 " +
           "FROM Attendance a " +
           "JOIN a.student s " +
           "GROUP BY s.id, s.nume, s.prenume " +
           "ORDER BY s.nume, s.prenume")
    List<Object[]> getStatistics();
}
