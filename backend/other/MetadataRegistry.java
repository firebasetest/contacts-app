@Component
public class MetadataRegistry {
    
    // Map<BU_ID, Map<FieldName, AttributeDefinition>>
    private Map<UUID, Map<String, AttributeDefinition>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        // Query database for all attribute definitions and group them by BU
        List<AttributeDefinition> definitions = repo.findAll();
        this.cache = definitions.stream()
            .collect(Collectors.groupingBy(AttributeDefinition::getBuId,
                     Collectors.toMap(AttributeDefinition::getName, d -> d)));
    }

    public boolean validate(UUID buId, String fieldName, Object value) {
        AttributeDefinition def = cache.get(buId).get(fieldName);
        if (def == null) return false; 
        
        // Dynamic validation based on data type stored in metadata
        return switch (def.getDataType()) {
            case "NUMBER" -> value instanceof Number;
            case "DATE" -> isValidDate(value);
            case "EMAIL" -> ((String) value).contains("@");
            default -> true;
        };
    }
}