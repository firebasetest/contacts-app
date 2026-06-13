package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.TenantSettings;
import com.mycompany.contact_app.repository.TenantSettingsRepository;
import com.mycompany.contact_app.security.TenantContext;
import com.mycompany.contact_app.model.ConsentPurpose;
import com.mycompany.contact_app.model.GlobalTwilioConfig; // Import the new VO
import com.mycompany.contact_app.service.TelephonyService;
import com.mycompany.contact_app.service.ConsentService; // Import new service
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/telephony")
public class OutboundCallController {

    private static final Logger log = LoggerFactory.getLogger(OutboundCallController.class);

    private final GlobalTwilioConfig globalConfig; // Use the VO
    private final TelephonyService telephonyService; // Dependency on new service
    private final ConsentService consentService; // Inject the new service
    private final TenantSettingsRepository tenantSettingsRepository;

    public OutboundCallController(
            @Value("${twilio.account-sid}") String globalAccountSid,
            @Value("${twilio.auth-token}") String globalAuthToken,
            @Value("${twilio.from-number:+15550199}") String globalFromNumber,
            TenantSettingsRepository tenantSettingsRepository,
            TelephonyService telephonyService,
            ConsentService consentService) {
        this.globalConfig = new GlobalTwilioConfig(globalAccountSid, globalAuthToken, globalFromNumber); // Store in VO
        this.tenantSettingsRepository = tenantSettingsRepository;
        // Keeping this dependency for potential future cross-cutting concerns,
        // though the service now owns much of the logic.
        tenantSettingsRepository.save(null);
        this.telephonyService = telephonyService;
        this.consentService = consentService; // Store the service instance

        // Perform validation immediately upon object creation
        validateCriticalConfiguration();
    }

    /**
     * Spawns an automated bridged telephony session joining an employee's line with
     * an external client.
     */
    @PostMapping("/call")
    @PreAuthorize("hasRole('ROLE_INTERNAL_EMPLOYEE')")
    public ResponseEntity<String> initiateBridgedCall(
            @RequestParam("employeePhone") String employeePhone,
            @RequestParam("contactPhone") String contactPhone) {

        String tenantIdString = com.mycompany.contact_app.security.TenantContext.getCurrentTenant();
        UUID principalUuid = UUID.fromString(tenantIdString);

        // --- CONSENT CHECK INTEGRATION POINT ---
        if (!consentService.hasConsent(principalUuid, ConsentPurpose.PHONE_CALLING)) {
            log.warn("SECURITY BLOCK: Attempted call for tenant {} denied due to lack of explicit consent.",
                    principalUuid);
            return ResponseEntity.status(403)
                    .body("Telephony Service Blocked: Consent is required before initiating calls.");
        }

        log.info("Initiating call bridge for tenant workspace context: {}", tenantIdString);

        try {
            // Delegate all complex business logic (credential lookup, initialization, API
            // call)
            String resultMessage = telephonyService.initiateCallBridge(
                    globalConfig.accountSid(),
                    globalConfig.authToken(),
                    globalConfig.fromNumber(),
                    employeePhone,
                    contactPhone,
                    tenantIdString);
            return ResponseEntity.ok(resultMessage);

        } catch (Exception ex) {
            // Catch the exception thrown by the service and map it to a generic failure
            // response
            log.error("Failed to initiate bridged call.", ex);
            return ResponseEntity.status(500).body("Telephony Service Core Failure: " + ex.getMessage());
        }
    }

    /**
     * Automated Response Callback Loop Endpoint hit by Twilio infrastructure
     * instances.
     * Returns descriptive runtime instructions in pure XML formatting (TwiML).
     */
    @PostMapping(value = "/twiml/connect", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateTwiMLConnectInstructions(@RequestParam("to") String targetContactPhone) {
        log.debug("Twilio callback pipeline invoked. Rendering automated Leg B integration rules for: {}",
                targetContactPhone);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Response>" +
                "    <Say voice=\"base\">Connecting you to your customer profile workspace registry now.</Say>" +
                "    <Dial>" +
                "        <Number>" + targetContactPhone + "</Number>" +
                "    </Dial>" +
                "</Response>";
    }

    /**
     * Validates that all critical Twilio configuration parameters are present and
     * follow expected formats.
     * Throws an IllegalStateException if any mandatory fields are missing or
     * invalid.
     */
    private void validateCriticalConfiguration() {
        // Simple regex for pattern checking (e.g., digits, alphanumeric structure)
        // NOTE: Production code might need stricter validation depending on Twilio's
        // actual format rules.
        final String SID_PATTERN = "^[a-zA-Z0-9]{15,30}$";
        final Pattern sidPattern = Pattern.compile(SID_PATTERN);

        if (globalConfig.accountSid() == null || !sidPattern.matcher(globalConfig.accountSid()).matches()) {
            throw new IllegalStateException(
                    "CRITICAL CONFIG ERROR: Mandatory 'twilio.account-sid' is missing or has an invalid format.");
        }
        if (globalConfig.authToken() == null) {
            throw new IllegalStateException("CRITICAL CONFIG ERROR: Mandatory 'twilio.auth-token' must be provided.");
        }
        // From number validation could use a dedicated phone regex, but for simplicity:
        if (globalConfig.fromNumber() == null || globalConfig.fromNumber().isEmpty()) {
            throw new IllegalStateException("CRITICAL CONFIG ERROR: Mandatory 'twilio.from-number' must be provided.");
        }

        log.info("Twilio Configuration validated successfully during application startup.");
    }
}