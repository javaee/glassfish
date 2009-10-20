package org.glassfish.tests.ejb.sample;

import javax.ejb.Singleton; 
import javax.ejb.Startup; 
import javax.ejb.SessionContext; 
import javax.annotation.Resource;
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.naming.InitialContext;

/**
 * @author Marina Vatkina
 */
@Singleton 
@Startup
public class SingletonBean {

    //@Resource private SessionContext ctx;

    @PermitAll
    public String foo() {
        try {
            InitialContext ctx = new InitialContext();
            SimpleEjb ejb = (SimpleEjb)ctx.lookup("java:app/sample/SimpleEjb");
            return ("called ejb." + ejb.bar());
        } catch (Exception e) {
            throw new RuntimeException (e.toString());
        }
    }

    @PreDestroy
    public void destroy() {
        System.out.println("SingletonBean :: In PreDestroy()");
    }
}
