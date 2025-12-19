package com.adminzone.service;

import com.adminzone.entity.Attendance;
import com.adminzone.entity.Course;
import com.adminzone.entity.Enrollment;
import com.adminzone.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final StudentService studentService;
    private final CourseService courseService;
    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;

    public void exportStudentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=students.csv");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Nume,Prenume,Email,Telefon,An Studiu");

        List<Student> students = studentService.findAllEntities();
        for (Student s : students) {
            writer.println(String.format("%d,%s,%s,%s,%s,%d",
                    s.getId(),
                    escapeCsv(s.getNume()),
                    escapeCsv(s.getPrenume()),
                    escapeCsv(s.getEmail()),
                    escapeCsv(s.getTelefon()),
                    s.getAnStudiu()));
        }
        writer.flush();
    }

    public void exportCoursesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=courses.csv");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Denumire,Profesor,Credite,Semestru");

        List<Course> courses = courseService.findAllEntities();
        for (Course c : courses) {
            writer.println(String.format("%d,%s,%s,%d,%d",
                    c.getId(),
                    escapeCsv(c.getDenumire()),
                    escapeCsv(c.getProfesorTitular()),
                    c.getNrCredite(),
                    c.getSemester()));
        }
        writer.flush();
    }

    public void exportAttendanceCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance.csv");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Data,Semestru,ID Student,Student,ID Curs,Curs,Status");

        List<Attendance> attendances = attendanceService.findAllEntities();
        for (Attendance a : attendances) {
            writer.println(String.format("%d,%s,%d,%d,%s,%d,%s,%s",
                    a.getId(),
                    a.getData().toString(),
                    a.getSemester(),
                    a.getStudent().getId(),
                    escapeCsv(a.getStudent().getFullName()),
                    a.getCourse().getId(),
                    escapeCsv(a.getCourse().getDenumire()),
                    escapeCsv(a.getStatus())));
        }
        writer.flush();
    }

    public void exportEnrollmentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=enrollments.csv");

        PrintWriter writer = response.getWriter();
        writer.println("ID Student,Student,ID Curs,Curs,Nota Finala");

        List<Enrollment> enrollments = enrollmentService.findAllEntities();
        for (Enrollment e : enrollments) {
            writer.println(String.format("%d,%s,%d,%s,%s",
                    e.getStudent().getId(),
                    escapeCsv(e.getStudent().getFullName()),
                    e.getCourse().getId(),
                    escapeCsv(e.getCourse().getDenumire()),
                    e.getNotaFinala() != null ? e.getNotaFinala().toString() : ""));
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
