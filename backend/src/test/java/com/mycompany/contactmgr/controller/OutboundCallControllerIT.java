package com.mycompany.contact_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.contact_app.entity.TenantSettings;
import com.mycompany.contact_app.repository.TenantSettingsRepository;
import com.mycompany.contact_app.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OutboundCallControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantSettingsRepository tenantSettingsRepository;

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @WithMockUser(roles = "INTERNAL_EMPLOYEE")
    public void initiateCall_WithValidTenantOverride_ShouldResolveAndReturnOk() throws Exception {
        UUID tenantUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // Mocking the custom telemetry configuration map for this tenant partition
        TenantSettings mockSettings = new TenantSettings();
        mockSettings.setBusinessUnitId(tenantUuid);
        mockSettings.setTelephonyProvider("TWILIO");
        mockSettings.setTelephonyCredentials(Map.of(
                "accountSid", "ACmockedTenantSid1234567890",
                "authToken", "mockedTenantAuthToken1234567890",
                "fromNumber", "+15550199"));

        Mockito.when(tenantSettingsRepository.findByBusinessUnitId(tenantUuid))
                .thenReturn(Optional.of(mockSettings));

        mockMvc.perform(post("/api/v1/telephony/call")
                .header("X-BU-ID", tenantUuid.toString())
                .param("employeePhone", "+15550100")
                .param("contactPhone", "+15550200")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "INTERNAL_EMPLOYEE")
    public void initiateCall_MissingTenantHeader_ShouldFallbackToGlobalConfiguration() throws Exception {
        // Leaving out the X-BU-ID header should drop straight through to global
        // environment fallback configuration paths
        mockMvc.perform(post("/api/v1/telephony/call")
                .param("employeePhone", "+15550100")
                .param("contactPhone", "+15550200")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EXTERNAL_CUSTOMER")
    public void initiateCall_InvalidRoleScope_ShouldReturnForbiddenStatusCode() throws Exception {
        // Asserting that non-internal employee personas cannot execute outbound
        // telephony integrations
        mockMvc.perform(post("/api/v1/telephony/call")
                .header("X-BU-ID", UUID.randomUUID().toString())
                .param("employeePhone", "+15550100")
                .param("contactPhone", "+15550200")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }
}