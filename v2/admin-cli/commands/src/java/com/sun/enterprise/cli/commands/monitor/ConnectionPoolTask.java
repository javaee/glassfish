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

package com.sun.enterprise.cli.commands.monitor;

import com.sun.enterprise.cli.framework.*;
import com.sun.appserv.management.monitor.statistics.*;
import javax.management.j2ee.statistics.*;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.io.File;

abstract class ConnectionPoolTask extends MonitorTask
{
    private final String displayFormat = "%1$-8s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s "+
                                         "%7$-5s %8$-5s %9$-5s %10$-5s %11$-5s %12$-5s %13$-5s "+
                                         "%14$-5s %15$-5s %16$-5s %17$-5s %18$-5s %19$-5s" ;
    
    public ConnectionPoolTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                              final boolean verbose, final File fileName)
    {
        super(srm, filter, timer, verbose, fileName);
    }

    
    void displayData(final ConnectionPoolStats cps)
    {
        final String data = String.format(displayFormat,
                                          cps.getAverageConnWaitTime().getCount(),
                                          cps.getConnRequestWaitTime().getLowWaterMark(),
                                          cps.getConnRequestWaitTime().getHighWaterMark(),
                                          cps.getConnRequestWaitTime().getCurrent(),
                                          cps.getNumConnAcquired().getCount(),
                                          cps.getNumConnCreated().getCount(),
                                          cps.getNumConnDestroyed().getCount(),
                                          cps.getNumConnFailedValidation().getCount(),
                                          cps.getNumConnFree().getLowWaterMark(),
                                          cps.getNumConnFree().getHighWaterMark(),
                                          cps.getNumConnFree().getCurrent(),
                                          cps.getNumConnNotSuccessfullyMatched().getCount(),
                                          cps.getNumConnReleased().getCount(),
                                          cps.getNumConnSuccessfullyMatched().getCount(),
                                          cps.getNumConnTimedOut().getCount(),
                                          cps.getNumConnUsed().getLowWaterMark(),
                                          cps.getNumConnUsed().getHighWaterMark(),
                                          cps.getNumConnUsed().getCurrent(),
                                          cps.getWaitQueueLength().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s,%10$s,"+
                                              "%11$s,%12$s,%13$s,%14$s,%15$s,%16$s,%17$s,%18$s,%19$s",
                                              cps.getAverageConnWaitTime().getCount(),
                                              cps.getConnRequestWaitTime().getLowWaterMark(),
                                              cps.getConnRequestWaitTime().getHighWaterMark(),
                                              cps.getConnRequestWaitTime().getCurrent(),
                                              cps.getNumConnAcquired().getCount(),
                                              cps.getNumConnCreated().getCount(),
                                              cps.getNumConnDestroyed().getCount(),
                                              cps.getNumConnFailedValidation().getCount(),
                                              cps.getNumConnFree().getLowWaterMark(),
                                              cps.getNumConnFree().getHighWaterMark(),
                                              cps.getNumConnFree().getCurrent(),
                                              cps.getNumConnNotSuccessfullyMatched().getCount(),
                                              cps.getNumConnReleased().getCount(),
                                              cps.getNumConnSuccessfullyMatched().getCount(),
                                              cps.getNumConnTimedOut().getCount(),
                                              cps.getNumConnUsed().getLowWaterMark(),
                                              cps.getNumConnUsed().getHighWaterMark(),
                                              cps.getNumConnUsed().getCurrent(),
                                              cps.getWaitQueueLength().getCount());
            writeToFile(fileData);
        }
    }

    
    void displayHeader()
    {
        final String waitTime = localStrings.getString("commands.monitor.connection_pool_wait_time");
        final String connReq = localStrings.getString("commands.monitor.connection_pool_conn_req");
        final String connFree = localStrings.getString("commands.monitor.connection_pool_conn_free");
        final String connUsed = localStrings.getString("commands.monitor.connection_pool_conn_used");
        
        final String avg = localStrings.getString("commands.monitor.avg");
        final String low = localStrings.getString("commands.monitor.low");
        final String hi = localStrings.getString("commands.monitor.hi");
        final String cur = localStrings.getString("commands.monitor.cur");
        final String acq = localStrings.getString("commands.monitor.acq");
        final String crt = localStrings.getString("commands.monitor.crt");
        final String des = localStrings.getString("commands.monitor.des");
        final String fai = localStrings.getString("commands.monitor.fai");        
        final String rej = localStrings.getString("commands.monitor.rej");
        final String rel = localStrings.getString("commands.monitor.rel");
        final String suc = localStrings.getString("commands.monitor.suc");
        final String to = localStrings.getString("commands.monitor.to");
        final String wai = localStrings.getString("commands.monitor.wai");        
        
        final String header = String.format("%1$s %2$12s %3$41s %4$40s",
                                            waitTime, connReq, connFree, connUsed);
        final String subHeader = String.format(displayFormat,
                                               avg,low,hi,cur,acq,crt,
                                               des,fai,low,hi,cur,rej,
                                               rel,suc,to,low,hi,cur,wai);
        
        CLILogger.getInstance().printMessage(header);
        CLILogger.getInstance().printMessage(subHeader);        
        
        if (fileName != null) {
            writeToFile(localStrings.getString("commands.monitor.connection_pool_write_to_file"));
        }
    }

    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.connection_pool_detail");
        CLILogger.getInstance().printMessage(details);
    }

    
}
