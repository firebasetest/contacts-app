package com.mycompany.contactmgr.repository;

import com.mycompany.contactmgr.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    // Inherits automatic tenant screening via Postgres RLS applied to the
    // 'contacts' table
}