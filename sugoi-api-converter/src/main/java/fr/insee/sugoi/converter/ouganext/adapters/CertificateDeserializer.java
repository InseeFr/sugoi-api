package fr.insee.sugoi.converter.ouganext.adapters;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CertificateDeserializer extends StdDeserializer<byte[]> {

    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
    public static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CertificateDeserializer() {
        this(null);
    }

    public CertificateDeserializer(Class<byte[]> t) {
        super(t);
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        String certstring = BEGIN_CERTIFICATE
                + p.getText().replaceAll("\n", "").replaceAll(" ", "").replaceAll("\t", "") + END_CERTIFICATE;

        return certstring.getBytes();

    }

}
