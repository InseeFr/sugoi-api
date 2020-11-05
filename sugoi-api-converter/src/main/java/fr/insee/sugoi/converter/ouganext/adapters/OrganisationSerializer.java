package fr.insee.sugoi.converter.ouganext.adapters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fr.insee.sugoi.converter.ouganext.Organisation;

public class OrganisationSerializer extends StdSerializer<Organisation> {

    public OrganisationSerializer() {
        this(null);
    }

    public OrganisationSerializer(Class<Organisation> t) {
        super(t);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(Organisation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getIdentifiant());
    }

}
