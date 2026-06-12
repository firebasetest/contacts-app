
package com.mycompany.contact_app.service;

import com.mycompany.contact_app.entity.TenantSettings;
import com.mycompany.contact_app.repository.TenantSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import com.twilio.wrapper.Twilio; // Assuming Twilio initialization is done here

/**
 * Dedicated service layer for managing outgoing telephony calls,
 * handling tenant context lookup and external API integration complexity.
 */
@Service
public class TelephonyService {

    private static final Logger log = LoggerFactory.getLogger(TelephonyService.class);

    private final TenantSettingsRepository tenantSettingsRepository;
    // Dependency Injection for configuration values (These are better managed via constructor parameters
    // or passed in, but we keep the structure similar to the controller for now)

    public TelephonyService(TenantSettingsRepository tenantSettingsRepository) {
        this.tenantSettingsRepository = tenantSettingsRepository;
    }

    /**
     * Attempts to retrieve customized credentials and account details based on the current tenant context.
     */
    private boolean loadContextualCredentials(String tenantIdString, String[] globalAccountSid, String[] globalAuthToken, String[] fromCallerId) {
        if (tenantIdString == null || tenantIdString.isBlank()) {
            return false; // Cannot find tenant context
        }

        try {
            UUID tenantUuid = UUID.fromString(tenantIdString);
            TenantSettings customSettings = tenantSettingsRepository.findByBusinessUnitId(tenantUuid)
                    .orElse(null);

            if (customSettings != null && "TWILIO".equalsIgnoreCase(customSettings.getTelephonyProvider())) {
                var creds = customSettings.getTelephonyCredentials();
                if (creds != null && creds.containsKey("accountSid") && creds.containsKey("authToken")) {
                    // Update the passed arrays safely with retrieved credentials
                    String accId = (String) creds.get("accountSid");
                    String authTok = (String) creds.get("authToken");
                    String fromNum = (String) creds.getOrDefault("fromNumber", null);

                    if (accId != null && authTok != null) {
                        globalAccountSid[0] = accId;
                        globalAuthToken[0] = authTok;
                        // Use the default number if custom one is missing, or null to allow the caller to handle it.
                        fromCallerId[0] = fromNum != null ? fromNum : globalFromNumber;
                        log.debug("Custom tenant telephony configurations successfully loaded for target workspace session.");
                        return true;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Catch case where tenantIdString is not a valid UUID
            log.warn("Invalid Business Unit ID format provided for TelephonyService: {}", tenantIdString);
        }
        return false; // Fallback to global defaults
    }

    /**
     * Spawns an automated bridged telephony session joining an employee's line with
     * an external client, orchestrating credential retrieval and API call.
     */
    public String initiateCallBridge(String globalAccountSid, String globalAuthToken, String globalFromNumber,
                                     String employeePhone, String contactPhone, String tenantIdString) throws Exception {

        // Use mutable wrappers (arrays of String) to allow the method to modify the credentials safely
        String[] accountSid = new String[]{globalAccountSid};
        String[] authToken = new String[]{globalAuthToken};
        String[] fromCallerId = new String[]{globalFromNumber};

        boolean success = loadContextualCredentials(tenantIdString, accountSid, authToken, fromCallerId);

        try {
            // Contextually initialize the thread-local instance credentials safely
            Twilio.init(accountSid[0], authToken[0]);

            String fromCallingId = fromCallerId[0];
            if (fromCallingId == null) {
                throw new IllegalStateException("Failed to resolve a valid outbound caller ID.");
            }

            // Webhook pointer that Twilio callbacks hit once Leg A answers to inject subsequent step tasks
            String callbackUrl = "https://your-platform-domain.com/api/v1/telephony/twiml/connect?to=" + contactPhone;

            Call call = Call.creator(
                    new PhoneNumber(employeePhone), // Leg A: Employee Terminal Destination
                    new PhoneNumber(fromCallingId), // Verified Platform Caller ID Outbound Route
                    new java.net.URI(callbackUrl) // Next Step Routing Blueprint Handler
            ).create();

            log.info("Outbound call bridge successfully launched. Twilio Tracking Sid Reference: {}", call.getSid());
            return "Call bridged successfully. Session Reference Tracker: " + call.getSid();

        } catch (Exception ex) {
            log.error("Outbound call routing initialization collapsed within service layer", ex);
            throw ex; // Re-throw the exception to be handled by the controller's HTTP response status
        }
    }
}