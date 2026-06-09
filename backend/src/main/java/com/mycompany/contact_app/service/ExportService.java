package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@Slf4j
public class ExportService {

    public byte[] exportToCSV(List<Contact> contacts) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                        "ID", "Name", "Email", "Phone", "Notes", "Created At", "Updated At"))) {

            for (Contact contact : contacts) {
                csvPrinter.printRecord(
                        contact.getId(),
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhoneNumber(),
                        contact.getNotes() != null ? contact.getNotes() : "",
                        contact.getCreatedAt(),
                        contact.getUpdatedAt());
            }
            csvPrinter.flush();
        }
        log.info("Exported {} contacts to CSV", contacts.size());
        return outputStream.toByteArray();
    }

    public byte[] exportToPDF(List<Contact> contacts) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1, 3, 3, 2, 3 });

        // Header
        addTableHeader(table, "ID");
        addTableHeader(table, "Name");
        addTableHeader(table, "Email");
        addTableHeader(table, "Phone");
        addTableHeader(table, "Notes");

        // Data
        for (Contact contact : contacts) {
            table.addCell(new PdfPCell(new Phrase(contact.getId().toString())));
            table.addCell(new PdfPCell(new Phrase(contact.getName())));
            table.addCell(new PdfPCell(new Phrase(contact.getEmail())));
            table.addCell(new PdfPCell(new Phrase(contact.getPhoneNumber())));
            table.addCell(new PdfPCell(new Phrase(contact.getNotes() != null ? contact.getNotes() : "")));
        }

        document.add(table);
        document.close();
        log.info("Exported {} contacts to PDF", contacts.size());
        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase(header));
        table.addCell(cell);
    }
}
