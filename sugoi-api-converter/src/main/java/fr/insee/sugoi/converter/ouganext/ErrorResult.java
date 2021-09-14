/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for ErrorResultType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
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
 */
@JsonPropertyOrder({"exception", "message"})
@JacksonXmlRootElement(localName = "ErrorResult", namespace = Namespace.ANNUAIRE)
public class ErrorResult {

  @JacksonXmlProperty(localName = "Exception")
  protected String exception;

  @JacksonXmlProperty(localName = "Message")
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
