package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.AttributeDefinition;
import com.mycompany.contact_app.service.AttributeDefinitionService;
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
        return ResponseEntity.ok(service.findByBusinessUnit(buId));
    }

    @PostMapping("/attribute-definitions")
    public ResponseEntity<AttributeDefinition> createDefinition(@RequestBody AttributeDefinition definition) {
        return ResponseEntity.ok(service.create(definition));
    }
}
