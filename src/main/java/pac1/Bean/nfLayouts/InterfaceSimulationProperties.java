//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.03 at 10:21:16 AM IST 
//


package pac1.Bean.nfLayouts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}xAxisInterfaceSimulationProp"/>
 *         &lt;element ref="{}yAxisInterfaceSimulationProp"/>
 *         &lt;element ref="{}widthInterfaceSimulationProp"/>
 *         &lt;element ref="{}heightInterfaceSimulationProp"/>
 *         &lt;element ref="{}bgColorInterfaceSimulationProp"/>
 *         &lt;element ref="{}fgColorInterfaceSimulationProp"/>
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
    "xAxisInterfaceSimulationProp",
    "yAxisInterfaceSimulationProp",
    "widthInterfaceSimulationProp",
    "heightInterfaceSimulationProp",
    "bgColorInterfaceSimulationProp",
    "fgColorInterfaceSimulationProp"
})
@XmlRootElement(name = "interfaceSimulationProperties")
public class InterfaceSimulationProperties implements java.io.Serializable{

    @XmlElement(required = true)
    protected String xAxisInterfaceSimulationProp;
    @XmlElement(required = true)
    protected String yAxisInterfaceSimulationProp;
    @XmlElement(required = true)
    protected String widthInterfaceSimulationProp;
    @XmlElement(required = true)
    protected String heightInterfaceSimulationProp;
    @XmlElement(required = true)
    protected String bgColorInterfaceSimulationProp;
    @XmlElement(required = true)
    protected String fgColorInterfaceSimulationProp;

    /**
     * Gets the value of the xAxisInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXAxisInterfaceSimulationProp() {
        return xAxisInterfaceSimulationProp;
    }

    /**
     * Sets the value of the xAxisInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXAxisInterfaceSimulationProp(String value) {
        this.xAxisInterfaceSimulationProp = value;
    }

    /**
     * Gets the value of the yAxisInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYAxisInterfaceSimulationProp() {
        return yAxisInterfaceSimulationProp;
    }

    /**
     * Sets the value of the yAxisInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYAxisInterfaceSimulationProp(String value) {
        this.yAxisInterfaceSimulationProp = value;
    }

    /**
     * Gets the value of the widthInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidthInterfaceSimulationProp() {
        return widthInterfaceSimulationProp;
    }

    /**
     * Sets the value of the widthInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidthInterfaceSimulationProp(String value) {
        this.widthInterfaceSimulationProp = value;
    }

    /**
     * Gets the value of the heightInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeightInterfaceSimulationProp() {
        return heightInterfaceSimulationProp;
    }

    /**
     * Sets the value of the heightInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeightInterfaceSimulationProp(String value) {
        this.heightInterfaceSimulationProp = value;
    }

    /**
     * Gets the value of the bgColorInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBgColorInterfaceSimulationProp() {
        return bgColorInterfaceSimulationProp;
    }

    /**
     * Sets the value of the bgColorInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBgColorInterfaceSimulationProp(String value) {
        this.bgColorInterfaceSimulationProp = value;
    }

    /**
     * Gets the value of the fgColorInterfaceSimulationProp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFgColorInterfaceSimulationProp() {
        return fgColorInterfaceSimulationProp;
    }

    /**
     * Sets the value of the fgColorInterfaceSimulationProp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFgColorInterfaceSimulationProp(String value) {
        this.fgColorInterfaceSimulationProp = value;
    }

}
