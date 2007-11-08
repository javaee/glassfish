/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import com.sun.logging.LogDomains;


import java.util.logging.Logger;
import java.util.logging.Level;

public class RestartInstance implements Runnable {
    
    /** Logger for self management service */
    private static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);

    /** Object name of the MBean in DAS that maintains the servers */
    private final String mbeanName = "com.sun.appserv:type=servers,category=config";

    /** Stop Instance operation name */
    private final String stopOpName = "stopServerInstance";

    /** Start instance operation name */
    private final String startOpName = "startServerInstance";
    
    /** Object name type for the given name of DAS Mbean for servers */
    private ObjectName objName;

    /** Server instance to check */
    private Instance instance = null;

    /** MBean server of the DAS */
    private MBeanServer mbeanServer = null;

    /** Server name */
    private String instanceName = null;
 
    /** Cluster name, if part of a cluster */
    private String clusterName = null;
    
        
    public RestartInstance(Instance value, MBeanServer server) {
        try {
            instance = value;
            mbeanServer = server;
            instanceName = instance.getServer().getName();
            clusterName = instance.getClusterName();
            objName = new ObjectName(mbeanName);
        } catch (Exception ex) {
            //nop
        }
    }

   private boolean stopInstance() {
       boolean stopped = true;
       try {
           //try stopping first
           Object[] params = new Object[]{instanceName};
           String[] types = new String[]{java.lang.String.class.getName()};

           if (clusterName==null) {
               _logger.log(Level.INFO,"sgmt.instancehang_prestopinstance", instanceName);
           } else {
               _logger.log(Level.INFO,"sgmt.instancehang_prestopclinstance",
                           new Object[]{instanceName, clusterName});
           }

           mbeanServer.invoke(objName, stopOpName, params, types);
           if (clusterName == null) {
               _logger.log(Level.INFO,"sgmt.instancehang_poststopinstance",instanceName);
           } else {
               _logger.log(Level.INFO,"sgmt.instancehang_poststopclinstance",
                           new Object[]{instanceName,clusterName});
           }
       } catch(Exception ex) {
          if (clusterName ==null) {
              _logger.log(Level.INFO,"sgmt.instancehang_errstopinstance",instanceName);
          } else {
              _logger.log(Level.INFO,"sgmt.instancehang_errstopclinstance",
                          new Object[]{instanceName,clusterName});
          }
          stopped = false;
       }

       return stopped;

    }

   private void startInstance() {
       try {
           //try starting 
           Object[] params = new Object[]{instanceName};
           String[] types = new String[]{java.lang.String.class.getName()};

           if (clusterName==null) {
               _logger.log(Level.INFO,"sgmt.instancehang_prestartinstance", instanceName);
           } else {
               _logger.log(Level.INFO,"sgmt.instancehang_prestartclinstance",
                           new Object[]{instanceName, clusterName});
           }

           mbeanServer.invoke(objName, startOpName, params, types);

           if (clusterName == null) {
               _logger.log(Level.INFO,"sgmt.instancehang_poststartinstance",instanceName);
           } else {
               _logger.log(Level.INFO,"sgmt.instancehang_poststartclinstance",
                           new Object[]{instanceName,clusterName});
           }
       } catch(Exception ex) {
          if (clusterName ==null) {
              _logger.log(Level.INFO,"sgmt.instancehang_errststartinstance",instanceName);
          } else {
              _logger.log(Level.INFO,"sgmt.instancehang_errstartclinstance",
                          new Object[]{instanceName,clusterName});
          }
       }

    }
   
    
    public void run() {
        boolean isStopped = stopInstance();
        if (isStopped) {
            startInstance();
        }
    }
}
