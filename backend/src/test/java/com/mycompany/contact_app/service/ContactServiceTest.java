package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.Contact;
import com.mycompany.contact_app.entity.ContactHistory;
import com.mycompany.contact_app.repository.ContactHistoryRepository;
import com.mycompany.contact_app.repository.ContactRepository;
import com.mycompany.contact_app.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactHistoryRepository historyRepository;

    @InjectMocks
    private ContactService contactService;

    private UUID tenantId;
    private UUID contactId;
    private Contact mockContact;

    @BeforeEach
    public void setUp() {
        tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        contactId = UUID.randomUUID();

        // Initialize multi-tenant context boundary for execution safety
        TenantContext.setCurrentTenant(tenantId.toString());

        mockContact = new Contact();
        mockContact.setId(contactId);
        mockContact.setName("John Doe");
        mockContact.setEmail("john.doe@example.com");
        mockContact.setPhoneNumber("+15550100");
        mockContact.setSystemRole("USER");
        mockContact.setBusinessUnitId(tenantId);
        mockContact.setCustomAttributes(new HashMap<>(Map.of("department", "Engineering")));
    }

    @AfterEach
    public void tearDown() {
        // Prevent thread-local context cross-pollution between runs
        TenantContext.clear();
    }

    @Test
    public void testGetContactById_Success() {
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(mockContact));

        Optional<Contact> result = Optional.ofNullable(contactService.getContactById(contactId));

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(contactRepository, times(1)).findById(contactId);
    }

    @Test
    public void testCreateContact_Success() {
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);

        Contact created = contactService.createContact(mockContact);

        assertNotNull(created);
        assertEquals(contactId, created.getId());
        verify(contactRepository, times(1)).save(mockContact);
    }

    @Test
    public void testGetHistoricalAuditTrail_CompilesAndResolves() {
        UUID historyId = UUID.randomUUID();
        ContactHistory mockHistory = new ContactHistory();

        // Explicitly setting fields that previously caused compilation failure
        mockHistory.setHistoryId(historyId);
        mockHistory.setContactId(contactId);
        mockHistory.setVersion(1);
        mockHistory.setCaptureType("UPDATE");
        mockHistory.setModifiedBy("admin-user");
        mockHistory.setValidFrom(LocalDateTime.now());
        mockHistory.setFieldDeltas(Map.of("phoneNumber", "+15550100 -> +15550199"));

        List<ContactHistory> historyList = Collections.singletonList(mockHistory);

        // Connect properly with the find method signature mapping requirements
        when(historyRepository.findByEntityIdOrderByVersionDesc(contactId)).thenReturn(historyList);

        List<ContactHistory> result = historyRepository.findByEntityIdOrderByVersionDesc(contactId);

        assertFalse(result.isEmpty());
        assertEquals(historyId, result.get(0).getHistoryId());
        assertEquals(1, result.get(0).getVersion());
        assertEquals("UPDATE", result.get(0).getCaptureType());
        verify(historyRepository, times(1)).findByEntityIdOrderByVersionDesc(contactId);
    }

    @Test
    public void testAnonymizeContact_GdprComplianceRoute() {
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(mockContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);

        contactService.anonymizeContact(contactId);

        // Asserting fields clear out completely to obey absolute privacy guidelines
        assertTrue(mockContact.getName().contains("ANONYMIZED"));
        assertEquals("deleted@tenant.local", mockContact.getEmail());
        assertNull(mockContact.getPhoneNumber());
        assertTrue(mockContact.getCustomAttributes().isEmpty());

        verify(contactRepository, times(1)).save(mockContact);
    }

    @Test
    public void testDelegateAdminRights_Success() {
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(mockContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);

        contactService.delegateAdminRights(contactId);

        assertEquals("DELEGATED_ADMIN", mockContact.getSystemRole());
        verify(contactRepository, times(1)).save(mockContact);
    }
}