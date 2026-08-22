package com.mycompany.contactmgr.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.contactmgr.entity.Company;
import com.mycompany.contactmgr.entity.Contact;
import com.mycompany.contactmgr.repository.ContactRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ContactSecurityEvaluatorTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactSecurityEvaluator evaluator;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentExternalUserIdFromAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("external-user-123", "password")
        );

        assertEquals("external-user-123", evaluator.getCurrentExternalUserId());
    }

    @Test
    void shouldAllowInternalEmployeeToManageContact() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("internal-user", "password")
        );

        Contact actor = new Contact();
        actor.setExternalUserId("internal-user");
        actor.setSystemRole("INTERNAL_EMPLOYEE");
        when(contactRepository.findByExternalUserId("internal-user")).thenReturn(Optional.of(actor));

        UUID contactId = UUID.randomUUID();

        assertTrue(evaluator.canManageContact(contactId));
        verify(contactRepository, never()).findById(any());
    }

    @Test
    void shouldAllowDelegatedAdminToManageContactInSameCompany() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("delegated-user", "password")
        );

        UUID companyId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        Contact actor = new Contact();
        actor.setExternalUserId("delegated-user");
        actor.setSystemRole("DELEGATED_ADMIN");
        Company actorCompany = new Company();
        actorCompany.setId(companyId);
        actor.setParentCompany(actorCompany);

        Contact targetContact = new Contact();
        targetContact.setId(contactId);
        Company targetCompany = new Company();
        targetCompany.setId(companyId);
        targetContact.setParentCompany(targetCompany);

        when(contactRepository.findByExternalUserId("delegated-user")).thenReturn(Optional.of(actor));
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(targetContact));

        assertTrue(evaluator.canManageContact(contactId));
    }

    @Test
    void shouldRejectDelegatedAdminManagingContactOutsideCompany() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("delegated-user", "password")
        );

        UUID companyId = UUID.randomUUID();
        UUID otherCompanyId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();

        Contact actor = new Contact();
        actor.setExternalUserId("delegated-user");
        actor.setSystemRole("DELEGATED_ADMIN");
        Company actorCompany = new Company();
        actorCompany.setId(companyId);
        actor.setParentCompany(actorCompany);

        Contact targetContact = new Contact();
        targetContact.setId(contactId);
        Company targetCompany = new Company();
        targetCompany.setId(otherCompanyId);
        targetContact.setParentCompany(targetCompany);

        when(contactRepository.findByExternalUserId("delegated-user")).thenReturn(Optional.of(actor));
        when(contactRepository.findById(contactId)).thenReturn(Optional.of(targetContact));

        assertFalse(evaluator.canManageContact(contactId));
    }

    @Test
    void shouldAllowDelegatedAdminToCreateContactInSameCompany() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("delegated-user", "password")
        );

        UUID companyId = UUID.randomUUID();

        Contact actor = new Contact();
        actor.setExternalUserId("delegated-user");
        actor.setSystemRole("DELEGATED_ADMIN");
        Company actorCompany = new Company();
        actorCompany.setId(companyId);
        actor.setParentCompany(actorCompany);

        when(contactRepository.findByExternalUserId("delegated-user")).thenReturn(Optional.of(actor));

        assertTrue(evaluator.canCreateContactUnderCompany(companyId));
    }

    @Test
    void shouldAllowInternalEmployeeToCreateCompany() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("internal-user", "password")
        );

        Contact actor = new Contact();
        actor.setExternalUserId("internal-user");
        actor.setSystemRole("INTERNAL_EMPLOYEE");
        when(contactRepository.findByExternalUserId("internal-user")).thenReturn(Optional.of(actor));

        assertTrue(evaluator.canCreateCompany());
    }

    @Test
    void shouldRejectDelegatedAdminFromCreatingCompany() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("delegated-user", "password")
        );

        Contact actor = new Contact();
        actor.setExternalUserId("delegated-user");
        actor.setSystemRole("DELEGATED_ADMIN");
        when(contactRepository.findByExternalUserId("delegated-user")).thenReturn(Optional.of(actor));

        assertFalse(evaluator.canCreateCompany());
    }
}
