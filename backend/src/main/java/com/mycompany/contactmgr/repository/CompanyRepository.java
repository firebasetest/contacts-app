package com.mycompany.contact_app.repository;

import com.mycompany.contact_app.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    // Inherits automatic tenant screening via Postgres RLS applied to the
    // 'contacts' table
}