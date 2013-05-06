package org.glassfish.tests.ejb.autoclose;

import javax.ejb.Stateless;

/**
 * @author Marina Vatkina
 */
@Stateless
public class SimpleEjb { //implements Simple {

    java.util.ArrayList x = null;

    public String saySomething(java.util.ArrayList a) {
        x = a;
        return "hello";
    }

    @javax.annotation.PreDestroy
    private void onDestroy() {
        x.add("=====destroyed=====");
    }
    
}
