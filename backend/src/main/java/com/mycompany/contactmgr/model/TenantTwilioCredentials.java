
package com.mycompany.contact_app.model;

/**
 * Immutable structure holding custom Twilio credentials retrieved from a specific tenant's settings.
 */
public record TenantTwilioCredentials(
        String accountSid,
        String authToken,
        String fromNumber) {

    // Helper constructor to check if the object is fully configured
    public boolean isValid() {
        return this.accountSid != null && !this.accountSid.isEmpty();
    }
}