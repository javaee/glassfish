package org.glassfish.tests.ejb.sample;

import javax.ejb.Singleton; 
import javax.ejb.Startup; 
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

/**
 * @author Marina Vatkina
 */
@Singleton 
@Startup
public class SingletonBean {

    @PermitAll
    public String foo() {
        return "called";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("SingletonBean :: In PreDestroy()");
    }
}
