package com.adminzone.service;

import com.adminzone.entity.Attendance;
import com.adminzone.entity.Course;
import com.adminzone.entity.Enrollment;
import com.adminzone.entity.Student;
import com.adminzone.exception.ResourceNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final StudentService studentService;
    private final CourseService courseService;
    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;

    // --- METODE PENTRU CSV (NESCHIMBATE) ---

    public void exportStudentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=students.csv");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Nume,Prenume,Email,Telefon,An Studiu");
        for (Student s : studentService.findAllEntities()) {
            writer.println(String.format("%d,%s,%s,%s,%s,%d", s.getId(), escapeCsv(s.getNume()), escapeCsv(s.getPrenume()), escapeCsv(s.getEmail()), escapeCsv(s.getTelefon()), s.getAnStudiu()));
        }
        writer.flush();
    }

    public void exportCoursesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=courses.csv");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Denumire,Profesor,Credite,Semestru");
        for (Course c : courseService.findAllEntities()) {
            writer.println(String.format("%d,%s,%s,%d,%d", c.getId(), escapeCsv(c.getDenumire()), escapeCsv(c.getProfesorTitular()), c.getNrCredite(), c.getSemester()));
        }
        writer.flush();
    }

    public void exportAttendanceCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance.csv");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Data,Semestru,ID Student,Student,ID Curs,Curs,Status");
        for (Attendance a : attendanceService.findAllEntities()) {
            writer.println(String.format("%d,%s,%d,%d,%s,%d,%s,%s", a.getId(), a.getData().toString(), a.getSemester(), a.getStudent().getId(), escapeCsv(a.getStudent().getFullName()), a.getCourse().getId(), escapeCsv(a.getCourse().getDenumire()), escapeCsv(a.getStatus())));
        }
        writer.flush();
    }

    public void exportEnrollmentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=enrollments.csv");
        PrintWriter writer = response.getWriter();
        writer.println("ID Student,Student,ID Curs,Curs,Nota Finala");
        for (Enrollment e : enrollmentService.findAllEntities()) {
            writer.println(String.format("%d,%s,%d,%s,%s", e.getStudent().getId(), escapeCsv(e.getStudent().getFullName()), e.getCourse().getId(), escapeCsv(e.getCourse().getDenumire()), e.getNotaFinala() != null ? e.getNotaFinala().toString() : ""));
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

    // --- METODA PENTRU PDF (FĂRĂ DIACRITICE) ---

    public void exportTranscriptPdf(Long studentId, HttpServletResponse response) throws IOException {
        Student student = studentService.findAllEntities().stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        List<Enrollment> enrollments = enrollmentService.findAllEntities().stream()
                .filter(e -> e.getStudent().getId().equals(studentId))
                .collect(Collectors.toList());

        response.setContentType("application/pdf");
        // Eliminăm diacriticele și din numele fișierului
        String numeFisier = removeDiacritics(student.getNume() + "_" + student.getPrenume());
        String filename = "Matricola_" + numeFisier + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Folosim fonturi standard (Helvetica) care merg perfect cu text simplu
        Font fontTitlu = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        // 1. Titlu (Modificat: Fără 'Ă')
        Paragraph titlu = new Paragraph("FOAIE MATRICOLA", fontTitlu);
        titlu.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titlu);
        document.add(new Paragraph("\n"));

        // 2. Informații Student (Curățăm datele dinamice de diacritice)
        document.add(new Paragraph("Student: " + removeDiacritics(student.getNume() + " " + student.getPrenume()), fontInfo));
        document.add(new Paragraph("Email: " + student.getEmail(), fontInfo));
        document.add(new Paragraph("An Studiu: " + student.getAnStudiu(), fontInfo));
        // Modificat: "generarii"
        document.add(new Paragraph("Data generarii: " + java.time.LocalDate.now(), fontInfo));
        document.add(new Paragraph("\n"));

        // 3. Tabelul cu Note
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 4f, 1.5f, 1.5f, 2f });

        // Header Tabel (Modificat: Fără 'ă')
        addTableHeader(table, "Materie", fontHeader);
        addTableHeader(table, "Semestru", fontHeader);
        addTableHeader(table, "Credite", fontHeader);
        addTableHeader(table, "Nota Finala", fontHeader);

        // Date Tabel
        double media = 0;
        int count = 0;

        for (Enrollment e : enrollments) {
            // Curățăm denumirea cursului de diacritice (ex: "Științe" -> "Stiinte")
            table.addCell(new Phrase(removeDiacritics(e.getCourse().getDenumire()), fontInfo));
            table.addCell(new Phrase(String.valueOf(e.getCourse().getSemester()), fontInfo));
            table.addCell(new Phrase(String.valueOf(e.getCourse().getNrCredite()), fontInfo));

            String nota = (e.getNotaFinala() != null) ? e.getNotaFinala().toString() : "-";
            table.addCell(new Phrase(nota, fontInfo));

            if (e.getNotaFinala() != null) {
                media += e.getNotaFinala().doubleValue();
                count++;
            }
        }

        document.add(table);

        // 4. Media Generală (Modificat: Fără 'ă')
        if (count > 0) {
            document.add(new Paragraph("\n"));
            Paragraph pMedia = new Paragraph("Media Generala: " + String.format("%.2f", media / count), fontTitlu);
            pMedia.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(pMedia);
        }

        document.close();
    }

    private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

    // --- FUNCȚIE UTILITARĂ PENTRU ELIMINAREA DIACRITICELOR ---
    private String removeDiacritics(String input) {
        if (input == null) return "";
        // Normalizează textul (separă caracterul de accent) și șterge accentele
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
