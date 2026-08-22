package com.mycompany.contactmgr.domain;

import lombok.Data;

@Data
public class AttributeValue {
    private String name;
    private String dataType; // STRING, NUMBER, DATE, BOOLEAN
    private Object value;
}