@Transactional
public void saveContact(Contact contact) {
    Map<String, Object> attrs = contact.getCustomAttributes();
    
    // Fetch rules from MetadataRegistry
    List<AttributeDefinition> defs = metadataRegistry.getRulesForBU(contact.getBuId());
    
    for (AttributeDefinition def : defs) {
        // If a rule exists, validate the input
        if (!rulesEngine.evaluateVisibility(attrs, def.getRules())) {
            throw new ValidationException("Field " + def.getName() + " is invalid based on current rules.");
        }
        
        // If it's a computed field, auto-calculate it
        if (def.isComputed()) {
            attrs.put(def.getName(), rulesEngine.evaluateComputedField(attrs, def.getFormula()));
        }
    }
    
    contactRepository.save(contact);
}