package org.glassfish.distributions.test.ejb;

import javax.ejb.Stateless;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SampleEjb {

    public String saySomething() {
        return "boo";
    }
}