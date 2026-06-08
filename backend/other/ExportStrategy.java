public interface ExportStrategy {
    byte[] export(List<Map<String, Object>> data);
    String getContentType();
    String getFileExtension();
}