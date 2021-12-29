package fr.insee.sugoi.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MappingType {
    ORGANIZATIONMAPPING("organizationMapping"),
    USERMAPPING("userMapping"),
    GROUPMAPPING("groupMapping"),
    APPLICATIONMAPPING("applicationMapping");


    private final String type;

    MappingType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

}

