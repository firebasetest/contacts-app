package com.mycompany.contact_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "attribute_definitions")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter
@Setter
public class AttributeDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // The ID of the Business Unit that owns this custom attribute definition
    @Column(name = "business_unit_id")
    private UUID buId;
    // The name of the field (e.g., "Contract_Start_Date", "Partner_Tier")
    private String name;
    // The expected data type (e.g., STRING, NUMBER, DATE)
    private String dataType;

    // The boolean flag used by MetadataRegistry to enforce presence
    private boolean required;

    @Type(value = JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
}