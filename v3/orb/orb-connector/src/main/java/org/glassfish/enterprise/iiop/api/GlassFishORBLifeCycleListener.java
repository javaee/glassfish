package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Contract;

import java.util.List;
import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 16, 2009
 */
@Contract
public interface GlassFishORBLifeCycleListener {

    public void initializeORBInitProperties(List<String> args, Properties props);

    public void orbCreated(org.omg.CORBA.ORB orb);

}
