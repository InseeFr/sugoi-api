package fr.insee.sugoi.converter.ouganext.adapters;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class CertificateSerializer extends StdSerializer<byte[]> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CertificateSerializer() {
        this(null);
    }

    public CertificateSerializer(Class<byte[]> t) {
        super(t);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(DatatypeConverter.printBase64Binary(value));
    }

}
