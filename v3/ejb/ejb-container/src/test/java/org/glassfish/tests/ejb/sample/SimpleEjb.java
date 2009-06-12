package org.glassfish.tests.ejb.sample;

import javax.ejb.Stateless;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SimpleEjb {

    public String saySomething() {
        return "boo";
    }
}
