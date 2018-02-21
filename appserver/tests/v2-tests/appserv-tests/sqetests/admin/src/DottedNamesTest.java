/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
