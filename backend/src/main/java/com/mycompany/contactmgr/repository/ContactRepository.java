package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.BaseContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ContactRepository extends JpaRepository<BaseContact, UUID> {
    // RLS in Postgres will automatically filter these by BU_ID
    // Returns List<BaseContact> populated with runtime subclasses (Contact/Company)
    List<BaseContact> findByStatus(String status);

    List<BaseContact> findBySource(String source);

    Optional<BaseContact> findByExternalUserId(String externalUserId);

    @Query(value = "SELECT * FROM contacts c WHERE c.business_unit_id = current_setting('app.current_bu_id', true)::BIGINT "
            +
            "AND c.custom_attributes @> cast(:filter as jsonb) " +
            "AND c.valid_from <= NOW() AND c.valid_to > NOW()", nativeQuery = true)
    List<BaseContact> findByCustomAttribute(@Param("filter") String filterJson);

}