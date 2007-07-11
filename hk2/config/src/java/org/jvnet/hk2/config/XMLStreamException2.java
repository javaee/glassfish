package org.jvnet.hk2.config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;

/**
 * To fix the problem in StAX API where exceptions are not properly chained.
 * 
 * @author Kohsuke Kawaguchi
 */
public class XMLStreamException2 extends XMLStreamException {
    public XMLStreamException2(String string) {
        super(string);
    }

    public XMLStreamException2(Throwable throwable) {
        super(throwable);
        initCause(throwable);
    }

    public XMLStreamException2(String string, Throwable throwable) {
        super(string, throwable);
        initCause(throwable);
    }

    public XMLStreamException2(String string, Location location, Throwable throwable) {
        super(string, location, throwable);
        initCause(throwable);
    }

    public XMLStreamException2(String string, Location location) {
        super(string, location);
    }
}
