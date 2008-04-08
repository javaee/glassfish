package com.sun.enterprise.v3.server;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;

import javax.xml.stream.XMLInputFactory;

/**
 * Allow people to inject {@link XMLInputFactory} via {@link Inject}.
 *
 * <p>
 * Component instantiation happens only when someone requests {@link XMLInputFactory},
 * so this is as lazy as it gets.
 *
 * <p>
 * TODO: if we need to let people choose StAX implementation, this is the place to do it. 
 *
 * @author Kohsuke Kawaguchi
 */
@Service
@FactoryFor(XMLInputFactory.class)
public class StAXParserFactory implements Factory {
    private final XMLInputFactory xif = XMLInputFactory.newInstance();

    public XMLInputFactory getObject() throws ComponentException {
        return xif;
    }
}
