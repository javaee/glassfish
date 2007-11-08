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

package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.*;
import java.io.*;
import java.util.Comparator;
import java.util.Arrays;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.config.ConfigException;

public class DiskAction implements NotificationListener, com.sun.enterprise.ee.selfmanagement.actions.DiskActionMBean {

    private final String JMX_MONITOR_COUNTER_THRESHOLD = "jmx.monitor.counter.threshold";
    private boolean serverLogFilter = false;
    private boolean serverAccessLogFilter = false;
    private boolean asadminAccessLogFilter = false;

    public DiskAction() { 
    }
    
    public void setServerLogFilter(boolean fil) {
        this.serverLogFilter = fil;
    }

    public void setAsadminAccessLogFilter(boolean fil) {
        this.asadminAccessLogFilter = fil;
    }

    public void setServerAccessLogFilter(boolean fil) {
        this.serverAccessLogFilter = fil;
    }

    public synchronized void handleNotification(Notification notification,
        Object handback) {
        try {
            boolean result = false;
            if(notification != null) {
	        if(notification.getType().equals(JMX_MONITOR_COUNTER_THRESHOLD)) {
                    doFunction();
                }
            } 
        } catch (Exception ex) { }
    }

    private void doFunction() {
        ConfigContext ctx = ApplicationServer.getServerContext().getConfigContext();
        Domain svr = null;
        try {
            svr = ServerBeansFactory.getDomainBean(ctx);
        } catch(ConfigException ex) { }
        String logsDir = svr.getLogRoot();
        if(logsDir == null){
            logsDir = FileUtil.getAbsolutePath(".."+File.separator+"logs");
        }
        String accessLogs = FileUtil.getAbsolutePath(logsDir+File.separator+"access");
        File logFileDir = new File(logsDir);
        File accessLogDir = new File(accessLogs);
        boolean logDeleted = false;
        if(this.serverLogFilter) {
            FileFilter serverLogFil = new FileFilter(){
                public boolean accept(File pathname) {
                    if(!(pathname.isDirectory())) {
                        String name = pathname.getName().toLowerCase();
                        return name.startsWith("server.log");
                    } else {
                        return false;
                    }
                }
            };
            File[] logFiles = getSortedFileList(logFileDir.listFiles(serverLogFil));
            logDeleted = false;
            if(logFiles != null && logFiles.length > 0) {
                for(int i=0; i<((logFiles.length)-1); i++) {
                    logDeleted = logFiles[i].delete();
                }
            }

        }
        if(this.asadminAccessLogFilter) {
            FileFilter asadminAccessLogFil = new FileFilter() {
                public boolean accept(File pathname) {
                    if(!(pathname.isDirectory())) {
                        String name = pathname.getName().toLowerCase();
                        return name.startsWith("__asadmin_access");
                    } else {
                        return false;
                    }
                }
            };
            File[] asadminAccessLogs = getSortedFileList(accessLogDir.listFiles(asadminAccessLogFil));
            logDeleted = false;
            if(asadminAccessLogs != null && asadminAccessLogs.length > 0) {
                for(int j=0; j<((asadminAccessLogs.length)-1); j++) {
                    logDeleted = asadminAccessLogs[j].delete();
                }
            }

        }
        if(this.serverAccessLogFilter) {
            FileFilter serverAccessLogFil = new FileFilter() {
                public boolean accept(File pathname) {
                    if(!(pathname.isDirectory())) {
                        String name = pathname.getName().toLowerCase();
                        return name.startsWith("server_access");
                    } else {
                        return false;
                    }
                }
            };
            File[] serverAccessLogs = getSortedFileList(accessLogDir.listFiles(serverAccessLogFil));
            logDeleted = false;
            if(serverAccessLogs != null && serverAccessLogs.length > 0) {
                for(int j=0; j<((serverAccessLogs.length)-1); j++) {
                    logDeleted = serverAccessLogs[j].delete();
                }
            }
        }
    }
    private File[] getSortedFileList(File[] files)
    {
        Arrays.sort(files, new Comparator() {
            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                return (int) (f1.lastModified() - f2.lastModified());
            }
        });
        return files;
    }
   
}


