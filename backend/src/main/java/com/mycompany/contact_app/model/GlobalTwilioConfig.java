
package com.mycompany.contact_app.model;

/**
 * Immutable configuration holding default global Twilio credentials and identifiers.
 */
public record GlobalTwilioConfig(
        String accountSid,
        String authToken,
        String fromNumber) {
}