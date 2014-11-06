//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.03 at 10:21:16 AM IST 
//


package pac1.Bean.nfLayouts;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the pac1.Bean.nfLayouts package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory implements java.io.Serializable{

    private final static QName _XAxisInterfaceSimulationProp_QNAME = new QName("", "xAxisInterfaceSimulationProp");
    private final static QName _LayoutDescription_QNAME = new QName("", "layoutDescription");
    private final static QName _InterfaceId_QNAME = new QName("", "interfaceId");
    private final static QName _BgColorInterfaceSimulationProp_QNAME = new QName("", "bgColorInterfaceSimulationProp");
    private final static QName _WidthLayoutProp_QNAME = new QName("", "widthLayoutProp");
    private final static QName _YAxisLayoutProp_QNAME = new QName("", "yAxisLayoutProp");
    private final static QName _ServiceName_QNAME = new QName("", "serviceName");
    private final static QName _BegCompId_QNAME = new QName("", "begCompId");
    private final static QName _HeightInterfaceSimulationProp_QNAME = new QName("", "heightInterfaceSimulationProp");
    private final static QName _FgColorInterfaceSimulationProp_QNAME = new QName("", "fgColorInterfaceSimulationProp");
    private final static QName _InterfaceCompName_QNAME = new QName("", "interfaceCompName");
    private final static QName _BgColorLayoutProp_QNAME = new QName("", "bgColorLayoutProp");
    private final static QName _YAxisInterfaceSimulationProp_QNAME = new QName("", "yAxisInterfaceSimulationProp");
    private final static QName _CanvasHeight_QNAME = new QName("", "canvas_height");
    private final static QName _ServiceMode_QNAME = new QName("", "serviceMode");
    private final static QName _WidthInterfaceSimulationProp_QNAME = new QName("", "widthInterfaceSimulationProp");
    private final static QName _EndCompId_QNAME = new QName("", "endCompId");
    private final static QName _ServiceType_QNAME = new QName("", "serviceType");
    private final static QName _LayoutCompId_QNAME = new QName("", "layoutCompId");
    private final static QName _InterfaceSimulationCompId_QNAME = new QName("", "interfaceSimulationCompId");
    private final static QName _FgColorLayoutProp_QNAME = new QName("", "fgColorLayoutProp");
    private final static QName _CanvasWidth_QNAME = new QName("", "canvas_width");
    private final static QName _InterfaceCompId_QNAME = new QName("", "interfaceCompId");
    private final static QName _XAxisLayoutProp_QNAME = new QName("", "xAxisLayoutProp");
    private final static QName _LayoutName_QNAME = new QName("", "layoutName");
    private final static QName _LayoutCompName_QNAME = new QName("", "layoutCompName");
    private final static QName _HeightLayoutProp_QNAME = new QName("", "heightLayoutProp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: pac1.Bean.nfLayouts
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link InterfaceSimulationComponent }
     * 
     */
    public InterfaceSimulationComponent createInterfaceSimulationComponent() {
        return new InterfaceSimulationComponent();
    }

    /**
     * Create an instance of {@link Layouts }
     * 
     */
    public Layouts createLayouts() {
        return new Layouts();
    }

    /**
     * Create an instance of {@link LayoutProperties }
     * 
     */
    public LayoutProperties createLayoutProperties() {
        return new LayoutProperties();
    }

    /**
     * Create an instance of {@link InterfaceSimulationProperties }
     * 
     */
    public InterfaceSimulationProperties createInterfaceSimulationProperties() {
        return new InterfaceSimulationProperties();
    }

    /**
     * Create an instance of {@link InterfaceComponent }
     * 
     */
    public InterfaceComponent createInterfaceComponent() {
        return new InterfaceComponent();
    }

    /**
     * Create an instance of {@link LayoutComponent }
     * 
     */
    public LayoutComponent createLayoutComponent() {
        return new LayoutComponent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "xAxisInterfaceSimulationProp")
    public JAXBElement<String> createXAxisInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_XAxisInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layoutDescription")
    public JAXBElement<String> createLayoutDescription(String value) {
        return new JAXBElement<String>(_LayoutDescription_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "interfaceId")
    public JAXBElement<String> createInterfaceId(String value) {
        return new JAXBElement<String>(_InterfaceId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "bgColorInterfaceSimulationProp")
    public JAXBElement<String> createBgColorInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_BgColorInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "widthLayoutProp")
    public JAXBElement<String> createWidthLayoutProp(String value) {
        return new JAXBElement<String>(_WidthLayoutProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "yAxisLayoutProp")
    public JAXBElement<String> createYAxisLayoutProp(String value) {
        return new JAXBElement<String>(_YAxisLayoutProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "serviceName")
    public JAXBElement<String> createServiceName(String value) {
        return new JAXBElement<String>(_ServiceName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "begCompId")
    public JAXBElement<String> createBegCompId(String value) {
        return new JAXBElement<String>(_BegCompId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "heightInterfaceSimulationProp")
    public JAXBElement<String> createHeightInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_HeightInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fgColorInterfaceSimulationProp")
    public JAXBElement<String> createFgColorInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_FgColorInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "interfaceCompName")
    public JAXBElement<String> createInterfaceCompName(String value) {
        return new JAXBElement<String>(_InterfaceCompName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "bgColorLayoutProp")
    public JAXBElement<String> createBgColorLayoutProp(String value) {
        return new JAXBElement<String>(_BgColorLayoutProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "yAxisInterfaceSimulationProp")
    public JAXBElement<String> createYAxisInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_YAxisInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "canvas_height")
    public JAXBElement<String> createCanvasHeight(String value) {
        return new JAXBElement<String>(_CanvasHeight_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "serviceMode")
    public JAXBElement<String> createServiceMode(String value) {
        return new JAXBElement<String>(_ServiceMode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "widthInterfaceSimulationProp")
    public JAXBElement<String> createWidthInterfaceSimulationProp(String value) {
        return new JAXBElement<String>(_WidthInterfaceSimulationProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "endCompId")
    public JAXBElement<String> createEndCompId(String value) {
        return new JAXBElement<String>(_EndCompId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "serviceType")
    public JAXBElement<String> createServiceType(String value) {
        return new JAXBElement<String>(_ServiceType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layoutCompId")
    public JAXBElement<String> createLayoutCompId(String value) {
        return new JAXBElement<String>(_LayoutCompId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "interfaceSimulationCompId")
    public JAXBElement<String> createInterfaceSimulationCompId(String value) {
        return new JAXBElement<String>(_InterfaceSimulationCompId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fgColorLayoutProp")
    public JAXBElement<String> createFgColorLayoutProp(String value) {
        return new JAXBElement<String>(_FgColorLayoutProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "canvas_width")
    public JAXBElement<String> createCanvasWidth(String value) {
        return new JAXBElement<String>(_CanvasWidth_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "interfaceCompId")
    public JAXBElement<String> createInterfaceCompId(String value) {
        return new JAXBElement<String>(_InterfaceCompId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "xAxisLayoutProp")
    public JAXBElement<String> createXAxisLayoutProp(String value) {
        return new JAXBElement<String>(_XAxisLayoutProp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layoutName")
    public JAXBElement<String> createLayoutName(String value) {
        return new JAXBElement<String>(_LayoutName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layoutCompName")
    public JAXBElement<String> createLayoutCompName(String value) {
        return new JAXBElement<String>(_LayoutCompName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "heightLayoutProp")
    public JAXBElement<String> createHeightLayoutProp(String value) {
        return new JAXBElement<String>(_HeightLayoutProp_QNAME, String.class, null, value);
    }

}
