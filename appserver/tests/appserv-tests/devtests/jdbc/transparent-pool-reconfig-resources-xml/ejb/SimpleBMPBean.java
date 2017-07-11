/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Globals;
import org.glassfish.hk2.api.ServiceLocator;


public class SimpleBMPBean implements EntityBean {

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            if (ds == null) {
                ds = (DataSource) context.lookup("java:comp/env/DataSource");
            }
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean acquireConnectionsTest(boolean expectFailure, long sleep) {
        boolean result;
        if (expectFailure) {
            result = false;
        } else {
            result = true;
        }

        try {
            Connection cons[] = new Connection[4];
            for (int i = 0; i < 4; i++) {
                try {
                    System.out.println("[DRC-TEST] : Using data-source : " + ds);

                    cons[i] = ds.getConnection();
                    System.out.println("[DRC-TEST] : " + cons[i].getMetaData().getUserName());
                    //introduce sleep in the middle of transaction (2 connections are acquired, 2 need to be acquired)
                    if (i == 2 & sleep > 0) {
                        try {
                            System.out.println("[DRC-TEST] : sleeping for " + sleep / 1000 + " seconds");
                            Thread.currentThread().sleep(sleep);
                            System.out.println("[DRC-TEST] : wokeup after " + sleep / 1000 + " seconds");
                        } catch (Exception e) {

                        }
                    }

                } catch (Exception e) {
                    if (expectFailure) {
                        result = true;
                    } else {
                        result = false;
                        System.out.println("[DRC-TEST] : " + e.getMessage());
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                try {
                    if (cons[i] != null) {
                        cons[i].close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception sqe) {
            sqe.printStackTrace();
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

    public void setProperty(String property, String value) {
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", "applications.application.ejb-bmp-transparent_pool_reconfigApp.resources.jdbc-connection-pool.java:app/ql-jdbc-pool.property." + property + "=" + value);
        runCommand("set", params);
        System.out.println("Property set : " + property + " - value : " + value);
    }

    public void setAttribute(String attribute, String value) {
        ParameterMap params = new ParameterMap();
        params.add("DEFAULT", "applications.application.ejb-bmp-transparent_pool_reconfigApp.resources.jdbc-connection-pool.java:app/ql-jdbc-pool.property." + attribute + "=" + value);
        runCommand("set", params);
        System.out.println("attribute set : " + attribute + " - value : " + value);
    }

    private static ActionReport runCommand(String commandName, ParameterMap parameters) {
        ServiceLocator habitat = Globals.getDefaultHabitat();
        CommandRunner cr = habitat.getService(CommandRunner.class);
        ActionReport ar = habitat.getService(ActionReport.class);
        cr.getCommandInvocation(commandName, ar, null).parameters(parameters).execute();
        return ar;
    }
}
