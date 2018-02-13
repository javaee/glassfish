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

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.VirtualServerConfigKeys;


/**
 */
public class CreateVirtualServerTest extends BaseTest
{
    private final Cmd target;

    static final String kName       = "myVirtualServer";
    static final String kConfigName = "server-config";
    static final String kHosts      = "${com.sun.aas.hostName}";
    static final String kState      = "on";
    static final String kDocRoot    = "${com.sun.aas.instanceRoot}/docroot";
    static final String kAccessLog  = "${com.sun.aas.instanceRoot}/logs/access";
    static final String kLogFile    = "${com.sun.aas.instanceRoot}/logs/server.log";
    static final String kHTTPListeners      = "http-listener-1";

    public CreateVirtualServerTest(final String user, 
        final String password, final String host, final int port, 
        final String vsName, final String configName, final String hosts)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final VirtualServerCmd createCmd = 
                cmdFactory.createVirtualServerCmd(vsName, configName,
                        hosts, getOptional(), VirtualServerCmd.kCreateMode);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());

        target = p2;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new CreateVirtualServerTest(
                "admin", "password", "localhost", 8686, 
                kName, kConfigName, kHosts).run();
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        optional.put(VirtualServerConfigKeys.STATE_KEY, kState);
        optional.put(VirtualServerConfigKeys.HTTP_LISTENERS_KEY, kHTTPListeners);
        //optional.put(VirtualServerConfigKeys.DOC_ROOT_KEY, kDocRoot);
        //optional.put(VirtualServerConfigKeys.LOG_FILE_KEY, kLogFile);
        //optional.put(VirtualServerConfigKeys.DOC_ROOT_PROPERTY_KEY, kDocRoot);
        //optional.put(VirtualServerConfigKeys.ACCESS_LOG_PROPERTY_KEY, kAccessLog);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private VirtualServerConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (VirtualServerConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("Name="+res.getName());
            System.out.println("State="+res.getState());
            System.out.println("HTTPListeners="+res.getHTTPListeners());
            System.out.println("Hosts="+res.getHosts());
            //System.out.println("Docroot="+res.getDocroot());
            System.out.println("LogFile="+res.getLogFile());
            System.out.println("Doc root property="+res.getPropertyValue("docroot"));
            System.out.println("Access log property="+res.getPropertyValue("accesslog"));

            return new Integer(0);
        }

    }
}
