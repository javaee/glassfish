/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Random;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/* Quicklooks the administrative dotted names.
 */
public class DottedNamesTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private static final String DNONS = "com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
    private static final String ADMIN_LOG_SERVICE_DN = "server.log-service.module-log-levels.admin";
    private static final String ADMIN_LISTENER_NAME = "admin-listener"; //this is the default name
    private static final String HTTP_LISTENER_LIST_DN = "server.http-service.http-listener";
    private long start, end;
    public DottedNamesTest() {
    }
    public String test() {
        String status = null;
        try {
            start = System.currentTimeMillis();
            testLogLevelDottedName();
            testListHTTPListeners();
            status = SimpleReporterAdapter.PASS;
        } catch(final Exception e) {
            status = SimpleReporterAdapter.FAIL;
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
        return ( status ) ;
    }

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String getName() {
        final String name = this.getClass().getName() + ": Testing admin dotted names";
        return (name);
    }
    
    private void testLogLevelDottedName() throws RuntimeException {
        TestLogLevel set = selectLogLevel();
        System.out.println("Selected value randomly to set: " + set.name());
        TestLogLevel get = null;
        try {
            setLogLevel(set);
            get = getLogLevel();
            System.out.println("Received value from backend: " + set.name());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (!set.equals(get)) {
            throw new RuntimeException("The values don't match: " + "set: " + set + ", got: " + get);
        }
    }
    private TestLogLevel selectLogLevel() {
        final int h = TestLogLevel.values().length - 1;
        final Random r = new Random();
        final int index = r.nextInt(h);
        return ( TestLogLevel.values()[index] );
    }
    private void setLogLevel(final TestLogLevel level) throws Exception {
        final ObjectName on         = new ObjectName(DNONS);
        final String op             = "dottedNameSet";
        final String[] sign         = new String[]{"java.lang.String"};
        final String nv             = ADMIN_LOG_SERVICE_DN + "=" + level.name();
        final Object[] params       = new String[]{nv};
        mbsc.invoke(on, op, params, sign);
    }
    private TestLogLevel getLogLevel() throws Exception {
        final ObjectName on         = new ObjectName(DNONS);
        final String op             = "dottedNameGet";
        final String[] sign         = new String[]{"java.lang.String"};
        final Object[] params       = new String[]{ADMIN_LOG_SERVICE_DN};
        final String value          = (String)((Attribute)mbsc.invoke(on, op, params, sign)).getValue();
        return ( TestLogLevel.valueOf(value) );
    }
    private void testListHTTPListeners() throws Exception {
        final String[] dns          = getListeners();
        boolean exists              = false;
        for (String s : dns) {
            System.out.println("Listener name = " + s);
            if (s.endsWith(ADMIN_LISTENER_NAME)) {
                exists = true;
                //break;
            }
        }
        if (!exists) 
            throw new RuntimeException("The admin listener does not exist, this is not possible");
    }
    private String[] getListeners() throws Exception {
        final ObjectName on         = new ObjectName(DNONS);
        final String op             = "dottedNameList";
        final String san            = (new String[]{}).getClass().getName();
        final String[] sign         = new String[]{san};
        final Object[] params       = new Object[]{new String[]{HTTP_LISTENER_LIST_DN}};
        final String[] values       = (String[])mbsc.invoke(on, op, params, sign);
        return ( values );
    }

    public long getExecutionTime() {
        return ( end - start );
    }
}

enum TestLogLevel {
    OFF,
    INFO,
    WARNING,
    SEVERE
}
