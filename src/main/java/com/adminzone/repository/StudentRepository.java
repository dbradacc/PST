package com.adminzone.repository;

import com.adminzone.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Student s WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(s.nume) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.prenume) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:anStudiu IS NULL OR s.anStudiu = :anStudiu)")
    Page<Student> search(@Param("query") String query, 
                         @Param("anStudiu") Integer anStudiu, 
                         Pageable pageable);
}
