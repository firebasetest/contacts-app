
package com.mycompany.contact_app.model;

/**
 * Defines the specific legal purposes for which consent can be granted or
 * revoked.
 * Using an Enum ensures type safety across the application when checking
 * permissions.
 */
public enum ConsentPurpose {
    PHONE_CALLING("ENABLEMENT: TELEPHONY CALLING", "Allows platform to initiate outbound calls."),
    EMAIL_MARKETING("USAGE: MARKETING EMAIL", "Allows sending non-essential promotional emails.");
    // Add more purposes here: DATA_SHARING, PROFILE_SYNC, etc.

    private final String key;
    private final String description;

    ConsentPurpose(String key, String description) {
        this.key = key;
        this.description = description;
    }

    ConsentPurpose(String key) {
        this(key, null); // Constructor for purposes without descriptions
    }

    public String getKey() {
        return key;
    }
}