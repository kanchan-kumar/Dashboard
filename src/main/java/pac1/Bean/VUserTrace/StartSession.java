//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.07.21 at 02:18:40 PM IST
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
 *         &lt;element ref="{}StartSessionTime"/>
 *         &lt;element ref="{}SessionID"/>
 *         &lt;element ref="{}ScriptName"/>
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
    "startSessionTime",
    "sessionID",
    "scriptName"
})
@XmlRootElement(name = "StartSession")
public class StartSession implements Serializable{

    @XmlElement(name = "StartSessionTime", required = true)
    protected String startSessionTime;
    @XmlElement(name = "SessionID", required = true)
    protected String sessionID;
    @XmlElement(name = "ScriptName", required = true)
    protected String scriptName;

    /**
     * Gets the value of the startSessionTime property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStartSessionTime() {
        return startSessionTime;
    }

    /**
     * Sets the value of the startSessionTime property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStartSessionTime(String value) {
        this.startSessionTime = value;
    }

    /**
     * Gets the value of the sessionID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Sets the value of the sessionID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSessionID(String value) {
        this.sessionID = value;
    }

    /**
     * Gets the value of the scriptName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Sets the value of the scriptName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setScriptName(String value) {
        this.scriptName = value;
    }

}