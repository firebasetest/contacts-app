@Component
public class CsvExportStrategy implements ExportStrategy {
    public byte[] export(List<Map<String, Object>> data) {
        // Implementation using OpenCSV to write data to a ByteArrayOutputStream
        return ...; 
    }
    public String getContentType() { return "text/csv"; }
    public String getFileExtension() { return "csv"; }
}

@Component
public class PdfExportStrategy implements ExportStrategy {
    public byte[] export(List<Map<String, Object>> data) {
        // Implementation using OpenPDF/iText to build a branded PDF
        return ...;
    }
    public String getContentType() { return "application/pdf"; }
    public String getFileExtension() { return "pdf"; }
}