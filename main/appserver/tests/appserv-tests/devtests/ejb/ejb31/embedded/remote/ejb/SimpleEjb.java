package org.glassfish.tests.ejb.remote;

import javax.ejb.Stateless;
import javax.ejb.Remote;

/**
 * @author Marina Vatkina
 */
@Stateless
@Remote(SimpleRemote.class)
public class SimpleEjb {

    public String saySomething() {
        return "hello";
    }
}
