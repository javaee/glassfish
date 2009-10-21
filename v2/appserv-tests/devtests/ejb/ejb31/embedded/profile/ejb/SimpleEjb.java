package org.glassfish.tests.ejb.profile;

import javax.ejb.Stateless;

/**
 * @author Marina Vatkina
 */
@Stateless
public class SimpleEjb {

    public String saySomething() {
        return "hello";
    }
}
