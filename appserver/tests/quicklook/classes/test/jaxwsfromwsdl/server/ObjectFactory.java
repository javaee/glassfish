
package jaxwsfromwsdl.server;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the jaxwsfromwsdl.server package. 
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
public class ObjectFactory {

    private final static QName _AddNumbersResponse_QNAME = new QName("http://example.org", "addNumbersResponse");
    private final static QName _AddNumbers_QNAME = new QName("http://example.org", "addNumbers");
    private final static QName _AddNumbersFault_QNAME = new QName("http://example.org", "AddNumbersFault");
    private final static QName _OneWayInt_QNAME = new QName("http://example.org", "oneWayInt");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: jaxwsfromwsdl.server
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddNumbersResponse }
     * 
     */
    public AddNumbersResponse createAddNumbersResponse() {
        return new AddNumbersResponse();
    }

    /**
     * Create an instance of {@link AddNumbers }
     * 
     */
    public AddNumbers createAddNumbers() {
        return new AddNumbers();
    }

    /**
     * Create an instance of {@link AddNumbersFault }
     * 
     */
    public AddNumbersFault createAddNumbersFault() {
        return new AddNumbersFault();
    }

    /**
     * Create an instance of {@link OneWayInt }
     * 
     */
    public OneWayInt createOneWayInt() {
        return new OneWayInt();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddNumbersResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AddNumbersResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://example.org", name = "addNumbersResponse")
    public JAXBElement<AddNumbersResponse> createAddNumbersResponse(AddNumbersResponse value) {
        return new JAXBElement<AddNumbersResponse>(_AddNumbersResponse_QNAME, AddNumbersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddNumbers }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AddNumbers }{@code >}
     */
    @XmlElementDecl(namespace = "http://example.org", name = "addNumbers")
    public JAXBElement<AddNumbers> createAddNumbers(AddNumbers value) {
        return new JAXBElement<AddNumbers>(_AddNumbers_QNAME, AddNumbers.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddNumbersFault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AddNumbersFault }{@code >}
     */
    @XmlElementDecl(namespace = "http://example.org", name = "AddNumbersFault")
    public JAXBElement<AddNumbersFault> createAddNumbersFault(AddNumbersFault value) {
        return new JAXBElement<AddNumbersFault>(_AddNumbersFault_QNAME, AddNumbersFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OneWayInt }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link OneWayInt }{@code >}
     */
    @XmlElementDecl(namespace = "http://example.org", name = "oneWayInt")
    public JAXBElement<OneWayInt> createOneWayInt(OneWayInt value) {
        return new JAXBElement<OneWayInt>(_OneWayInt_QNAME, OneWayInt.class, null, value);
    }

}
