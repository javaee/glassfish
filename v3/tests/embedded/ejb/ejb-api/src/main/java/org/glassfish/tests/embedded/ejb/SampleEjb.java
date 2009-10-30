package org.glassfish.tests.embedded.ejb;

import javax.ejb.Stateless;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SampleEjb {

    public String saySomething() {
        return "Hello World";
    }
}
