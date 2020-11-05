package fr.insee.sugoi.converter.ouganext.adapters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import fr.insee.sugoi.converter.ouganext.Organisation;

public class OrganisationDeserializer extends StdDeserializer<Organisation> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public OrganisationDeserializer() {
        this(null);
    }

    public OrganisationDeserializer(Class<Organisation> t) {
        super(t);
    }

    @Override
    public Organisation deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        Organisation organisation = new Organisation();
        organisation.setIdentifiant(p.getText());
        return organisation;
    }

}
