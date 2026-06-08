@Service
public class DynamicContactService {

    private final MetadataRegistry metadataRegistry;
    private final AttributeRulesEngine rulesEngine;
    private final ContactRepository contactRepository;

    public DynamicContactService(MetadataRegistry mr, AttributeRulesEngine re, ContactRepository cr) {
        this.metadataRegistry = mr;
        this.rulesEngine = re;
        this.contactRepository = cr;
    }

    public void saveDynamicContact(Contact contact) {
        List<AttributeDefinition> definitions = metadataRegistry.getDefinitionsForBu(contact.getBusinessUnitId());
        
        // Validate every dynamic field against its rules
        for (AttributeDefinition def : definitions) {
            if (!rulesEngine.validate(contact.getCustomAttributes(), def)) {
                throw new IllegalArgumentException("Invalid dynamic field: " + def.getName());
            }
        }
        contactRepository.save(contact);
    }
}