package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ServerRequestInfo;

import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 17, 2009
 */
@Contract
public interface GlassFishORBFactory {
    // This is ORBConstants.ENV_IS_SERVER_PROPERTY. We cannot
    // reference ORBConstants from this class.
    public static final String ENV_IS_SERVER_PROPERTY = 
        "com.sun.corba.ee.ORBEnvironmentIsGlassFishServer" ;

    public ORB createORB(Properties props);

    public int getOTSPolicyType();

    public int getCSIv2PolicyType();

    public Properties getCSIv2Props();

    public void setCSIv2Prop(String name, String value);

    public int getORBInitialPort();

    public String getORBHost(ORB orb);

    public int getORBPort(ORB orb); 

    public boolean isEjbCall (ServerRequestInfo sri);
}
