package org.glassfish.tests.ejb.sample;

import javax.ejb.Singleton; 
import javax.ejb.Startup; 
import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;

/**
 * @author Marina Vatkina
 */
@Singleton 
@Startup
public class SingletonBean {

    @javax.annotation.Resource(name="jdbc/__default") javax.sql.DataSource ds;

    @PostConstruct
    private void init() {
	System.out.println("ds = " + ds);
    }

    public String foo() {

        return "called";
    }

    @PreDestroy
    public void destroy() {
        System.out.println("SingletonBean :: In PreDestroy()");
    }
}
