package com.mycompany.contact_app.service;

import com.mycompany.contact_app.dto.ContactHistoryResponseDto;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContactAuditService {
    private static final Logger log = LoggerFactory.getLogger(ContactAuditService.class);
    private final ContactHistoryRepository historyRepository;
    private final ContactService contactService;

    public ContactAuditService(ContactHistoryRepository historyRepository, ContactService contactService) {
        this.historyRepository = historyRepository;
        this.contactService = contactService;
    }

    /**
     * Extracts full historical mutation trails sorted chronologically.
     * Computes value deltas between sequential entity revisions.
     */
    @Transactional(readOnly = true)
    public List<ContactHistoryResponseDto> getEntityChangelog(UUID entityId) {
        // Multi-tenant Guard: Fails immediately if the entity is outside the active
        // tenant boundary
        contactService.findById(entityId);

        // Fetch chronological tracking history (oldest to newest for smooth delta
        // calculations)
        List<ContactHistory> rawHistory = historyRepository.findByContactIdOrderByValidFromDesc(entityId);
        Collections.reverse(rawHistory);

        List<ContactHistoryResponseDto> changelog = new ArrayList<>();
        ContactHistory previousSnapshot = null;

        for (ContactHistory current : rawHistory) {
            ContactHistoryResponseDto dto = new ContactHistoryResponseDto();
            dto.setHistoryId(current.getHistoryId());
            dto.setVersion(current.getVersion());
            dto.setCaptureType(current.getCaptureType());
            dto.setValidFrom(current.getValidFrom());
            dto.setModifiedBy(current.getModifiedBy() != null ? current.getModifiedBy() : "System Ingestion");
            dto.setName(current.getName());
            dto.setEmail(current.getEmail());
            dto.setPhoneNumber(current.getPhoneNumber());
            dto.setCustomAttributes(current.getCustomAttributes());

            // Compute field-level modifications against the previous state
            dto.setFieldDeltas(computeDiff(previousSnapshot, current));

            changelog.add(dto);
            previousSnapshot = current;
        }

        // Reverse back to deliver newest updates first to the API consumer
        Collections.reverse(changelog);
        return changelog;
    }

    /**
     * Reflective diff utility comparing properties across historical state
     * transitions.
     */
    private Map<String, String> computeDiff(ContactHistory prev, ContactHistory curr) {
        Map<String, String> diffs = new LinkedHashMap<>();
        if (prev == null) {
            diffs.put("Record", "Initial creation profile setup.");
            return diffs;
        }

        compareField("name", prev.getName(), curr.getName(), diffs);
        compareField("email", prev.getEmail(), curr.getEmail(), diffs);
        compareField("phone_number", prev.getPhoneNumber(), curr.getPhoneNumber(), diffs);

        // Handle Unstructured JSONB Dynamic Attribute Map Diffing
        compareCustomAttributes(prev.getCustomAttributes(), curr.getCustomAttributes(), diffs);

        if (diffs.isEmpty()) {
            diffs.put("System", "Metadata adjustments or minor save event without property modifications.");
        }
        return diffs;
    }

    private void compareField(String fieldName, String oldVal, String newVal, Map<String, String> diffs) {
        String safeOld = oldVal == null ? "" : oldVal;
        String safeNew = newVal == null ? "" : newVal;
        if (!safeOld.equals(safeNew)) {
            diffs.put(fieldName, String.format("Changed from '%s' to '%s'", safeOld, safeNew));
        }
    }

    private void compareCustomAttributes(Map<String, Object> oldMap, Map<String, Object> newMap,
            Map<String, String> diffs) {
        Map<String, Object> safeOld = oldMap != null ? oldMap : Collections.emptyMap();
        Map<String, Object> safeNew = newMap != null ? newMap : Collections.emptyMap();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(safeOld.keySet());
        allKeys.addAll(safeNew.keySet());

        for (String key : allKeys) {
            Object oldV = safeOld.get(key);
            Object newV = safeNew.get(key);

            if (oldV == null && newV != null) {
                diffs.put("attribute." + key, String.format("Added property with value: '%s'", newV));
            } else if (oldV != null && newV == null) {
                diffs.put("attribute." + key, "Removed property field.");
            } else if (oldV != null && !oldV.equals(newV)) {
                diffs.put("attribute." + key, String.format("Updated from '%s' to '%s'", oldV, newV));
            }
        }
    }

    /**
     * Fetches the complete chronological history trail for a single contact record.
     * PostgreSQL Row-Level Security automatically isolates visibility at the
     * database layer.
     */
    @Transactional(readOnly = true)
    public List<ContactHistory> getAuditTrailForEntity(UUID entityId) {
        if (entityId == null) {
            log.warn("Attempted to fetch audit log trail for a null entity identifier.");
            return Collections.emptyList();
        }

        log.info("Fetching chronological change ledger trail for record: {}", entityId);

        // Return sorted historical events matching our layout expectation
        return historyRepository.findByEntityIdOrderByVersionDesc(entityId);
    }
}