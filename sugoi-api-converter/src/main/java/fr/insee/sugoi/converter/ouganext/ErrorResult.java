package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for ErrorResultType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ErrorResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Exception" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@JsonPropertyOrder({ "exception", "message" })
@JacksonXmlRootElement(localName = "ErrorResult", namespace = Namespace.ANNUAIRE)
public class ErrorResult {

  @JacksonXmlProperty(localName = "Exception", namespace = Namespace.ANNUAIRE)
  protected String exception;
  @JacksonXmlProperty(localName = "Message", namespace = Namespace.ANNUAIRE)
  protected String message;

  public String getException() {
    return exception;
  }

  public void setException(String value) {
    this.exception = value;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String value) {
    this.message = value;
  }

}
