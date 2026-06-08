private ContactDTO mapToContact(CSVRecord record) {
    ContactDTO dto = new ContactDTO();
    Map<String, Object> customAttrs = new HashMap<>();

    // 1. Map Core Fields (Fixed Schema)
    dto.setFirstName(record.get("first_name"));
    dto.setLastName(record.get("last_name"));
    dto.setEmail(record.get("email"));
    
    // 2. Map Dynamic Fields (JSONB)
    // We assume any column NOT in our fixed list is a 'custom attribute'
    Map<String, String> headerMap = record.getParser().getHeaderMap();
    
    for (String header : headerMap.keySet()) {
        if (!isCoreAttribute(header)) {
            // Validate against your metadata registry
            if (metadataRegistry.isValidForBU(header, currentBuId)) {
                customAttrs.put(header, record.get(header));
            }
        }
    }
    
    dto.setCustomAttributes(customAttrs);
    return dto;
}