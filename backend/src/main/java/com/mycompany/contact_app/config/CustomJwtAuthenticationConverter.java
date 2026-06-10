package com.mycompany.contact_app.config;

import com.mycompany.contact_app.config.TenantContextFilter;
import com.mycompany.contact_app.service.IdentityProvisioningService;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    private final IdentityProvisioningService provisioningService;

    // Spring automatically injects the provisioning service here
    public CustomJwtAuthenticationConverter(IdentityProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Extract default scopes/roles if present
        var authorities = defaultAuthoritiesConverter.convert(jwt);

        // 2. Multi-Tenant Integration Enhancement:
        // If your authorization server embeds the business unit ID inside a custom JWT
        // claim,
        // you can extract it here and seed your ThreadLocal context automatically.
        if (jwt.hasClaim("business_unit_id")) {
            String tokenBuId = jwt.getClaimAsString("business_unit_id");
            TenantContextFilter.CURRENT_TENANT.set(tokenBuId);
        }

        // 3. CRITICAL TRIGGER: Execute the JIT check.
        // If this is their first login, their database Contact record is quietly born
        // here.
        provisioningService.ensureUserShadowRecordExists(jwt);

        // 4. Return token to the security context
        // The subject claim ('sub') automatically maps to the Authentication principal
        // name,
        // matching what your ContactSecurityEvaluator looks up via
        // findByExternalUserId(externalId)
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}