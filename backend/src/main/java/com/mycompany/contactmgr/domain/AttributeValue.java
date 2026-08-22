package com.mycompany.contact_app.domain;

import lombok.Data;
import java.util.Map;

@Data
public class AttributeValue {
    private String name;
    private String dataType; // STRING, NUMBER, DATE, BOOLEAN
    private Object value;
}