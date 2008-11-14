package com.sun.enterprise.v3.common;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Writes command output to a json stream
 *
 */
@Service(name="json")
@Scoped(PerLookup.class)
public class JsonActionReporter extends ActionReporter {
    
    public void writeReport(OutputStream os) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
