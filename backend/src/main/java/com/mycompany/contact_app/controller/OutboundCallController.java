package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.entity.TenantSettings;
import com.mycompany.contact_app.repository.TenantSettingsRepository;
import com.mycompany.contact_app.security.TenantContext;
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

@RestController
@RequestMapping("/api/v1/telephony")
public class OutboundCallController {

    private static final Logger log = LoggerFactory.getLogger(OutboundCallController.class);

    private final String globalAccountSid;
    private final String globalAuthToken;
    private final String globalFromNumber;
    private final TenantSettingsRepository tenantSettingsRepository;

    public OutboundCallController(
            @Value("${twilio.account-sid}") String globalAccountSid,
            @Value("${twilio.auth-token}") String globalAuthToken,
            @Value("${twilio.from-number:+15550199}") String globalFromNumber,
            TenantSettingsRepository tenantSettingsRepository) {
        this.globalAccountSid = globalAccountSid;
        this.globalAuthToken = globalAuthToken;
        this.globalFromNumber = globalFromNumber;
        this.tenantSettingsRepository = tenantSettingsRepository;
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

        String tenantIdString = TenantContext.getCurrentTenant();
        log.info("Processing cloud outbound call request for tenant workspace context: {}", tenantIdString);

        // Fallback defaults drawn securely from application environment parameters
        String accountSid = globalAccountSid;
        String authToken = globalAuthToken;
        String fromCallerId = globalFromNumber;

        // Check for customized enterprise configurations managed by this business unit
        // override
        if (tenantIdString != null) {
            UUID tenantUuid = UUID.fromString(tenantIdString);
            TenantSettings customSettings = tenantSettingsRepository.findByBusinessUnitId(tenantUuid).orElse(null);

            if (customSettings != null && "TWILIO".equalsIgnoreCase(customSettings.getTelephonyProvider())) {
                var creds = customSettings.getTelephonyCredentials();
                if (creds != null && creds.containsKey("accountSid") && creds.containsKey("authToken")) {
                    accountSid = (String) creds.get("accountSid");
                    authToken = (String) creds.get("authToken");
                    fromCallerId = (String) creds.getOrDefault("fromNumber", globalFromNumber);
                    log.debug(
                            "Custom tenant telephony configurations successfully loaded for target workspace session.");
                }
            }
        }

        try {
            // Contextually initialize the thread-local instance credentials safely
            Twilio.init(accountSid, authToken);

            // Webhook pointer that Twilio callbacks hit once Leg A answers to inject
            // subsequent step tasks
            String callbackUrl = "https://your-platform-domain.com/api/v1/telephony/twiml/connect?to=" + contactPhone;

            Call call = Call.creator(
                    new PhoneNumber(employeePhone), // Leg A: Employee Terminal Destination
                    new PhoneNumber(fromCallerId), // Verified Platform Caller ID Outbound Route
                    new URI(callbackUrl) // Next Step Routing Blueprint Handler
            ).create();

            log.info("Outbound call bridge successfully launched. Twilio Tracking Sid Reference: {}", call.getSid());
            return ResponseEntity.ok("Call bridged successfully. Session Reference Tracker: " + call.getSid());

        } catch (Exception ex) {
            log.error("Outbound call routing initialization collapsed on integration gateway", ex);
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
}