// Use Jackson to serialize the map to JSONB
String jsonBValue = objectMapper.writeValueAsString(dto.getCustomAttributes());