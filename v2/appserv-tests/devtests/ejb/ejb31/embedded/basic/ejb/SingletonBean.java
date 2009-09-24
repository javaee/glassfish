package org.glassfish.tests.ejb.sample;

import javax.ejb.Singleton; 
import javax.ejb.Startup; 
import javax.annotation.PreDestroy;

/**
 * @author Marina Vatkina
 */
@Singleton 
@Startup
public class SingletonBean {

    public String foo() {
        return "called";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("SingletonBean :: In PreDestroy()");
    }
}
