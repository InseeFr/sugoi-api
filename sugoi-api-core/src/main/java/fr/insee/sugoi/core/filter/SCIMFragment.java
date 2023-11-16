package fr.insee.sugoi.core.filter;

import java.util.Arrays;

public class SCIMFragment {

    private String attribute;
    private SCIMFilterOperator scimFilterOperator;

    private Object value;

    public String getAttribute() {
        return attribute;
    }

    public SCIMFilterOperator getScimFilterOperator() {
        return scimFilterOperator;
    }

    public Object getValue() {
        return value;
    }

    public SCIMFragment(String attribute, SCIMFilterOperator scimFilterOperator, Object value) {
        this.attribute = attribute;
        this.scimFilterOperator = scimFilterOperator;
        this.value = value;
    }

    public static SCIMFragment of(String scimFragmentString) {
        var scimFragmentStrings = scimFragmentString.split(" ");
        if (scimFragmentStrings.length == 3){
            return new SCIMFragment(scimFragmentStrings[0],SCIMFilterOperator.of(scimFragmentStrings[1]),scimFragmentStrings[2]);
        }
        throw new RuntimeException("Filter could not be calculated from "+scimFragmentString + " filterString");
    }
}
