package com.mycompany.contactmgr.model;

/**
 * Represents the operational status of a Business Unit/Tenant.
 */
public enum BusinessUnitStatus {
    ACTIVE, // Fully operational, accepts data
    INACTIVE, // Suspended, cannot process new data
    PENDING_DELETION // Soft-deleted, pending removal
}