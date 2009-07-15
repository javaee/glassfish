package org.glassfish.tests.ejb.sample;

import javax.ejb.Stateless;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

/**
 * @author Jerome Dochez
 */
@Stateless
public class SimpleEjb {

@PermitAll
    public String saySomething() {
        return "boo";
    }
}
