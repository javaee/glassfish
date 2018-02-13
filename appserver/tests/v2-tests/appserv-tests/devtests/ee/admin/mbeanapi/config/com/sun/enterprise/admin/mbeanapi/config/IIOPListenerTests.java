/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.config;

import com.sun.appserv.management.config.*;
import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests the iiop listener related mbean api classes
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Sep 23, 2004
 * @version $Revision: 1.3 $
 */
public class IIOPListenerTests {
    private AMXConnector mAmxConnector;
    private IIOPServiceConfig mIIOPServiceConfig;

    private static final String STOP_DOMAIN_COMMAND = "asadmin stop-domain";
    private static final String START_DOMAIN_COMMAND = "asadmin start-domain";
    int totalTests = 4;
    int passed = 0;
    int failed = 0;
    int didNotRun = 0;

    public IIOPListenerTests(){
        setup();
    }

    private void setup() {
        try {
            mAmxConnector = new AMXConnector(getHost(),
                                            getAMXPort(),
                                            getAdminUser(),
                                            getAdminPassword(),
                                            getUseTLS());
            mIIOPServiceConfig = ((ConfigConfig)mAmxConnector.
                                                getDomainRoot().
                                                    getDomainConfig().
                                                        getConfigConfigMap().
                                                            get(getConfigName())).
                                                                getIIOPServiceConfig();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void runtest() {
        createListener();
        createSSLElement();
        stopDomain();
        startDomain();
        setup();
        removeSSLElement();
        removeListener();
        printSummary(passed, failed, didNotRun);
    }

    private void printProgress(final int i) {
        System.out.println(i +" out of "+ totalTests + " tests passed");
    }

    private void printSummary(final int passed, final int failed, final int didNotRun) {
        System.out.println("Summary Results:"+ passed+" tests passed, "+
                    failed+" tests failed, "+ didNotRun +" tests did not run" );
        System.out.println("Total Expected to Run "+totalTests);
    }

    private void createListener() {
        mIIOPServiceConfig.createIIOPListenerConfig(getListenerName(),
                                        getListenerAddress(),
                                        getOptional());
        final IIOPListenerConfig cfg = (IIOPListenerConfig) mIIOPServiceConfig.getIIOPListenerConfigMap().get(getListenerName());
        if(cfg == null) {
            System.err.println("IIOPListener was not created");
            printSummary(passed, ++failed, ++didNotRun);
            System.exit(1);
        }
        printProgress(++passed);
    }

    private void createSSLElement() {
        final IIOPListenerConfig cfg = ((IIOPListenerConfig)mIIOPServiceConfig.
                                            getIIOPListenerConfigMap().
                                                get(getListenerName()));
        cfg.createSSLConfig("s1as",null);
        final SSLConfig ssl = cfg.getSSLConfig();
        if(ssl ==null){
            System.err.println("ssl element did not get created");
            printSummary(passed, ++failed, ++didNotRun);
            System.exit(1);
        }
        printProgress(++passed);
    }

    private void stopDomain() {
        System.out.println("Stopping domain....");
        try {
            mAmxConnector.getAppserverConnectionSource().getJMXConnector(false).close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        final String command = STOP_DOMAIN_COMMAND +" "+ getDomainName();
        final String result = runCommand(command);
        if(result.indexOf(getDomainName()+" stopped")<0){
            System.out.println("Domain " + getDomainName() +" did not stop");
        }
    }

    private void startDomain() {
        System.out.println("Starting domain....");
        final String command = START_DOMAIN_COMMAND +
                    " " +getDomainName();
        final String result = runCommand(command);
        if(result.indexOf(getDomainName()+" started")<0){
            System.out.println("Domain " +getDomainName() + " did not start");
        }
    }

    private void removeSSLElement() {
        final IIOPListenerConfig cfg = ((IIOPListenerConfig)mIIOPServiceConfig.
                                            getIIOPListenerConfigMap().
                                                get(getListenerName()));
        cfg.removeSSLConfig();
        final SSLConfig ssl = cfg.getSSLConfig();
        if (ssl != null){
            System.err.println("Could not delete SSL element");
            printSummary(passed, ++failed, ++didNotRun);
        }
        else{
            printProgress(++passed);
        }
    }

    private void removeListener() {
        mIIOPServiceConfig.removeIIOPListenerConfig(getListenerName());
        final IIOPListenerConfig cfg = (IIOPListenerConfig) mIIOPServiceConfig.getIIOPListenerConfigMap().get(getListenerName());
        if(cfg != null) {
            System.err.println("Listener was not removed from http service");
            printSummary(passed, ++failed, --totalTests);
        }
        else{
            printProgress(++passed);
        }
    }

    private String getListenerName() {
        return "testListener";
    }

    private String getListenerAddress() {
        return "localhost";
    }

    private Map getOptional() {
        final Map ops  = new HashMap();
        ops.put(IIOPListenerConfigKeys.SECURITY_ENABLED_KEY, "true");
        return ops;
    }

    protected String getHost() {
        return System.getProperty("HOST", "localhost");
    }

    protected int getAMXPort() {
        return Integer.parseInt(System.getProperty("AMX_PORT","8686"));
    }

    protected int getAdminPort() {
        return Integer.parseInt(System.getProperty("ADMIN_PORT","4848"));
    }

    protected String getAdminUser() {
        return System.getProperty("ADMIN_USER", "admin");
    }

    protected String getAdminPassword() {
        return System.getProperty("ADMIN_PASSWORD", "adminadmin");
    }

    protected boolean getUseTLS() {
        return Boolean.valueOf(System.getProperty("USE_TLS", "false")).booleanValue();
    }

    private String getConfigName() {
        return System.getProperty("CONFIG_NAME","server-config");
    }

    private String getDomainName() {
        return "domain1";
    }

    private String runCommand(final String command) {
        String sb = null;
        try {
            final Process p = Runtime.getRuntime().exec(command);
            sb = convertToString(p.getInputStream());
            System.out.println("Received Process Output String: |" +sb+"|");
            if(sb.length() == 0 ){
                sb = convertToString(p.getErrorStream());
                System.out.println("Received Process Error String: |" +sb+"|");
            }
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return sb;
    }

    private String convertToString(final InputStream in) {
        final BufferedReader br = new BufferedReader
                                (new InputStreamReader(in));
        String line;
        final StringBuffer sb = new StringBuffer();
        try {
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(" ");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(final String[] args){
        final IIOPListenerTests test = new IIOPListenerTests();
        test.runtest();
    }

}
