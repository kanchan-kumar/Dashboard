//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.08.08 at 06:26:00 PM IST
//


package pac1.Bean.VUserTrace;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.io.Serializable;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}SessionTime"/>
 *         &lt;element ref="{}SessionStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionTime",
    "sessionStatus"
})
@XmlRootElement(name = "EndSession")
public class EndSession implements Serializable{

    @XmlElement(name = "SessionTime", required = true)
    protected String sessionTime;
    @XmlElement(name = "SessionStatus", required = true)
    protected String sessionStatus;

    /**
     * Gets the value of the sessionTime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSessionTime() {
        return sessionTime;
    }

    /**
     * Sets the value of the sessionTime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSessionTime(String value) {
        this.sessionTime = value;
    }

    /**
     * Gets the value of the sessionStatus property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSessionStatus() {
        return sessionStatus;
    }

    /**
     * Sets the value of the sessionStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSessionStatus(String value) {
        this.sessionStatus = value;
    }

}