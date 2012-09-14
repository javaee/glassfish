package com.sun.s1asdev.jdbc.flushconnectionpool.ejb;

import javax.ejb.*;
import javax.naming.*;
import java.sql.*;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.glassfish.embeddable.*;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

public class SimpleBMPBean implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;
    protected final String poolName = "jdbc-flushconnectionpool-pool";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /**
     * Acquire 4 connections, closing them at the end of the loop.
     * Cache the first connection, and before acquiring the last connection, 
     * do a flush connection pool. The 5th connection got after the 
     * flush should be different from the first connection got.
     *
     * @return boolean
     */
    public boolean test1() {
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        ds = this.ds;

        boolean passed = false;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
		//Do a flush and then get a connection for the last iteration
		if(i ==4) {
		    if(!flushConnectionPool()) {
		        break;
		    }
		}	    
                conn = ds.getConnection();
                System.out.println("********i=" + i + "conn=" + ds.getConnection(conn));

                if (i == 0) {
                    firstConnection = ds.getConnection(conn);
                } else if (i == 4) {
                    lastConnection = ds.getConnection(conn);
                }
                passed = (firstConnection != lastConnection);

            } catch (Exception e) {
                e.printStackTrace();
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return passed;
    }


    /**
     * Get connection and do not close it
     */
    public boolean test2() {
        Connection firstConnection = null;
        Connection lastConnection = null;
        com.sun.appserv.jdbc.DataSource ds = null;

        ds = this.ds;

        boolean passed = true;
	Connection con = null;
	try {
            con = ds.getConnection();
	} catch(Exception ex) {
	    passed = false;
	}
        return passed;
    }

    private boolean flushConnectionPool() throws Exception {
        Habitat habitat = Globals.getDefaultHabitat();
	GlassFish gf = habitat.getService(GlassFish.class);
	CommandRunner runner = gf.getCommandRunner();
	CommandResult res = runner.run("flush-connection-pool", poolName);
	System.out.println("res= " + res.getOutput());
	if(res.getExitStatus() == CommandResult.ExitStatus.SUCCESS) {
            return true;
	}
	return false;
    }

    private boolean amxFlushConnectionPool() throws Exception {
        final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";
        final JMXServiceURL url = new JMXServiceURL(urlStr);
	boolean result = false;

        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
	                new ObjectName("amx:pp=/ext,type=connector-runtime-api-provider");

	String[] params = {poolName};
	String[] signature = {String.class.getName()};
        Map<String,Object> flushStatus = (Map<String,Object>) connection.invoke(objectName, "flushConnectionPool", params, signature);
	if(flushStatus != null) {
	    result = (Boolean) flushStatus.get("FlushConnectionPoolKey");
	}
        return result;
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
