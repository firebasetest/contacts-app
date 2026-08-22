
package com.mycompany.contactmgr.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MissingTenantClaimException extends RuntimeException {
    public MissingTenantClaimException(String message) {
        super(message);
    }
}