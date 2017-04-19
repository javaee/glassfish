package org.glassfish.tests.ejb.sample;

/**
 * @author mvatkina
 */
public class SimpleBase implements SimpleInterface {

    public String saySomething() {
        return "in SimpleBase";
    }

    public String testJPA() {
        throw new IllegalStateException("Should not be called in base class!!!!");
    }

}
