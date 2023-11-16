package fr.insee.sugoi.core.filter;

public enum SCIMFilterOperator {
    /**
     * The filter type for AND filters.
     */
    AND("and"),



    /**
     * The filter type for OR filters.
     */
    OR("or"),



    /**
     * The filter type for equality filters.
     */
    EQUALITY("eq");

    /**
     * lower case string value for this filter type.
     */
    private String stringValue;



    /**
     * Creates a new filter type with the provided string value.
     *
     * @param  stringValue  The lower case string value for this filter type.
     */
    private SCIMFilterOperator(final String stringValue)
    {
        this.stringValue = stringValue;
    }



    /**
     * Retrieves the lower case string value for this filter type.
     *
     * @return  The lower case string value for this filter type.
     */
    public String getStringValue()
    {
        return stringValue;
    }



    /**
     * Retrieves a string representation of this filter type.
     *
     * @return  A string representation of this filter type.
     */
    @Override
    public String toString()
    {
        return getStringValue();
    }

    public static SCIMFilterOperator of(String stringValue){
        for (SCIMFilterOperator b : SCIMFilterOperator.values()) {
            if (b.stringValue.equalsIgnoreCase(stringValue)) {
                return b;
            }
        }
        return null;
    }
}
