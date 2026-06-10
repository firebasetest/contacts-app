package com.mycompany.contact_app.controller;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telephony")
public class OutboundCallController {

    // Ideally, load these credentials dynamically per tenant from your
    // database/registry
    private static final String TWILIO_ACCOUNT_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String TWILIO_AUTH_TOKEN = "your_auth_token_here";
    private static final String TWILIO_FROM_NUMBER = "+15550199";

    public OutboundCallController() {
        // Initialize Twilio instance context
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
    }

    /**
     * Triggers an asynchronous bridged voice call sequence between employee and
     * target contact.
     */
    @PostMapping("/call")
    @PreAuthorize("hasRole('ROLE_INTERNAL_EMPLOYEE') or @contactSecurity.isInternalEmployee()")
    public ResponseEntity<String> initiateBridgedCall(
            @RequestParam("employeePhone") String employeePhone,
            @RequestParam("contactPhone") String contactPhone) {

        String tenantId = TenantContextFilter.CURRENT_TENANT.get();

        // Instruct Twilio to ring the internal employee first.
        // Once they answer, Twilio hits the URL payload to dynamically execute the
        // second leg of the call.
        Call call = Call.creator(
                new PhoneNumber(employeePhone), // Leg A: Destination (The Employee)
                new PhoneNumber(TWILIO_FROM_NUMBER), // Source Platform Caller ID
                new URI("http://your-app-domain.com/api/v1/telephony/twiml/connect?to=" + contactPhone) // Leg B webhook
        ).create();

        return ResponseEntity.ok("Call initiated. Tracking Session Reference SID: " + call.getSid());
    }

    /**
     * Webhook invoked by Twilio when the employee answers, generating the TwiML XML
     * template instructions to bridge the second number.
     */
    @PostMapping(value = "/twiml/connect", produces = "application/xml")
    public String generateTwiMLConnectInstructions(@RequestParam("to") String targetContactPhone) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Response>" +
                "    <Say>Connecting you to your client profile record now.</Say>" +
                "    <Dial>" +
                "        <Number>" + targetContactPhone + "</Number>" +
                "    </Dial>" +
                "</Response>";
    }
}