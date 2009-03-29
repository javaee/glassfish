package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;

import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Contract
public interface GlassFishORBFactory {

    public ORB createORB(Properties props);

    public String getORBHost(ORB orb);

    public int getORBPort(ORB orb);

}
