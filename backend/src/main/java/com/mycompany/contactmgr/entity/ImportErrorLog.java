package com.mycompany.contactmgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "import_error_logs")
public class ImportErrorLog {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Setter
    @Column(name = "row_number")
    private int rowNumber;

    @Setter
    @Column(name = "record_identifier")
    private String recordIdentifier; // Stores email or tax ID for user reference

    @Setter
    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

}