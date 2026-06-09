package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.BaseContact;
import com.mycompany.contact_app.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    // RLS in Postgres will automatically filter these by BU_ID
    List<Contact> findByStatus(String status);

    List<Contact> findBySource(String source);

    Optional<BaseContact> findByExternalUserId(String externalUserId);
}