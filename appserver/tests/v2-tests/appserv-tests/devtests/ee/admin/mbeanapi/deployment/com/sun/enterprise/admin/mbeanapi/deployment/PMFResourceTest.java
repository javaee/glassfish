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

import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;
import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfigKeys;


/**
 */
public class PMFResourceTest extends BaseTest
{
    private final Cmd target;

    static final String kJNDIName       = "myPMFResource";
    static final String kJDBCResourceJNDIName = "jndi/myJdbcResource";
    static final String kFactoryClass   = "a.b.c";
    static final String kObjectType     = "user";

    public PMFResourceTest(final String user, 
        final String password, final String host, final int port, 
        final String jndiName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreatePMFResourceCmd createCmd = 
                cmdFactory.createCreatePMFResourceCmd(jndiName, getOptional());

        final DeletePMFResourceCmd deleteCmd = 
                cmdFactory.createDeletePMFResourceCmd(jndiName);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());
        final PipeCmd p3 = new PipeCmd(connectCmd, deleteCmd);

        final CmdChainCmd chainCmd = new CmdChainCmd();
        chainCmd.addCmd(p2);
        chainCmd.addCmd(p3);

        target = chainCmd;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new PMFResourceTest("admin", "password", "localhost", 8686, 
                kJNDIName).run(); 
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        //optional.put(CreateResourceKeys.RESOURCE_OBJECT_TYPE_KEY, 
                //kObjectType);
        optional.put(PersistenceManagerFactoryResourceConfigKeys.JDBC_RESOURCE_JNDI_NAME_KEY, 
                kJDBCResourceJNDIName);
        optional.put(PersistenceManagerFactoryResourceConfigKeys.FACTORY_CLASS_KEY, 
                kFactoryClass);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private PersistenceManagerFactoryResourceConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (PersistenceManagerFactoryResourceConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("JNDIName="+res.getJNDIName());
            System.out.println("FactoryClass="+res.getFactoryClass());
            System.out.println("ObjectType="+res.getObjectType());
            System.out.println("JDBCResourceJNDIName="+res.getJDBCResourceJNDIName());
            System.out.println("Enabled="+res.getEnabled());

            return new Integer(0);
        }

    }
}
