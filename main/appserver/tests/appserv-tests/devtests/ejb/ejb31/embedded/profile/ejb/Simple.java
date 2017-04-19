package org.glassfish.tests.ejb.profile;

import javax.ejb.Local;

/**
 * @author Marina Vatkina
 */
@Local
public interface Simple {

    public String saySomething() ;
}
