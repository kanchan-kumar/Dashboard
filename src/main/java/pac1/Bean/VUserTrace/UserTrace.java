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
 *         &lt;element ref="{}StartSession"/>
 *         &lt;element ref="{}Pages"/>
 *         &lt;element ref="{}EndSession"/>
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
    "startSession",
    "pages",
    "endSession"
})
@XmlRootElement(name = "UserTrace")
public class UserTrace implements Serializable{

    @XmlElement(name = "StartSession", required = true)
    protected StartSession startSession;
    @XmlElement(name = "Pages", required = true)
    protected Pages pages;
    @XmlElement(name = "EndSession", required = true)
    protected EndSession endSession;

    /**
     * Gets the value of the startSession property.
     *
     * @return
     *     possible object is
     *     {@link StartSession }
     *
     */
    public StartSession getStartSession() {
        return startSession;
    }

    /**
     * Sets the value of the startSession property.
     *
     * @param value
     *     allowed object is
     *     {@link StartSession }
     *
     */
    public void setStartSession(StartSession value) {
        this.startSession = value;
    }

    /**
     * Gets the value of the pages property.
     *
     * @return
     *     possible object is
     *     {@link Pages }
     *
     */
    public Pages getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     *
     * @param value
     *     allowed object is
     *     {@link Pages }
     *
     */
    public void setPages(Pages value) {
        this.pages = value;
    }

    /**
     * Gets the value of the endSession property.
     *
     * @return
     *     possible object is
     *     {@link EndSession }
     *
     */
    public EndSession getEndSession() {
        return endSession;
    }

    /**
     * Sets the value of the endSession property.
     *
     * @param value
     *     allowed object is
     *     {@link EndSession }
     *
     */
    public void setEndSession(EndSession value) {
        this.endSession = value;
    }

}