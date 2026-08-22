package com.mycompany.contactmgr.controller;

import com.mycompany.contactmgr.entity.AttributeDefinition;
import com.mycompany.contactmgr.security.TenantContext;
import com.mycompany.contactmgr.service.AttributeDefinitionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
public class AttributeDefinitionController {
    private final AttributeDefinitionService service;

    public AttributeDefinitionController(AttributeDefinitionService service) {
        this.service = service;
    }

    @GetMapping("/attribute-definitions")
    public ResponseEntity<List<AttributeDefinition>> getDefinitions(@RequestParam UUID buId) {
        // CRITICAL FIX: Validate tenant context first to ensure robustness against
        // null/unauthenticated users.
        // Avoid accepting an arbitrary buId request parameter without checking
        // if it matches the authenticated caller's tenant context.
        // This enables Broken Object Level Authorization (BOLA/IDOR),
        // allowing tenants to query metadata belonging to other organizations.
        if (!TenantContext.isSame(buId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.findByBusinessUnit(buId));
    }

    @PostMapping("/attribute-definitions")
    public ResponseEntity<AttributeDefinition> createDefinition(@RequestBody AttributeDefinition definition) {
        return ResponseEntity.ok(service.create(definition));
    }
}
