package com.sun.enterprise.module.bootstrap;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigParser;

import javax.xml.stream.XMLStreamException;

/**
 * Populates {@link Habitat}.
 *
 * {@link Populator} gets to run right after the {@link Habitat} is
 * created. Implementations can use this timing to introduce
 * additional inhabitants, for example by loading some config file.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
public interface Populator {
    void run(ConfigParser parser);
}
