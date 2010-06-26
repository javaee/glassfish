package org.glassfish.tests.ejb.sample;

import javax.ejb.Stateless;

/**
 */
@Stateless
public class OtherEjb {

    public String other() {
        return "I'm Other";
    }
}
