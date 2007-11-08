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
 * LowMemoryAlertAction.java
 *
 *
 */

package com.sun.enterprise.ee.selfmanagement.actions;

import java.lang.management.*;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;



/**
 *
 * @author Sun Micro Systems, Inc
 */
public class LowMemoryAlertAction implements MBeanRegistration, LowMemoryAlertActionMBean, NotificationListener {
    
    private ObjectName myObjectName = null;
    private String memoryPoolNames = "Perm Gen, Tenured Gen";
    private String listeners = null;
    private int threshold = 80;
    private int offset = 5;
    private String  mailRecipients;
    private String  mailResource;

    private List<String> listenersList = new ArrayList(2);
    private List<String> poolNamesList = new ArrayList(2);
    static StringManager sm = StringManager.getManager(LowMemoryAlertAction.class);
    static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    
    public LowMemoryAlertAction() {
    }
    
    public void postRegister(Boolean registrationDone) {
        if (registrationDone) {
            if(_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, " LowMemoryAlertAction : postRegister registration Done " );
        }
    }
    
    public javax.management.ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        if(_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " LowMemoryAlertAction : preRegister ObjectName " + name);
        myObjectName = name;
        return name;
    }
    
    public void preDeregister() throws Exception {
        if(_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " LowMemoryAlertAction : preDeRegister " );
    }
    
    public void postDeregister() {
        if(_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, " LowMemoryAlertAction : postDeRegister " );
    }
    
    public void handleNotification(javax.management.Notification notification, Object handback) {
	String notifType = notification.getType();
	if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
	    sendMailAlert(notification);
	    if (offset != 0)
                setThresholds();
	    return;
	}
	else if(_logger.isLoggable(Level.INFO))
            _logger.log(Level.INFO, sm.getString("actions.activated", "Low Memory Alert", notification));
	registerForMemoryThresholdNotifications();
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setMailRecipients(String recipients) {
        mailRecipients = recipients;
    }

    public void setMailResource(String mailRes) {
        mailResource = mailRes;
    }

    public void setListeners(String listeners) {
        if (listeners != null) {
            this.listeners = listeners;
	    StringTokenizer tokenizer = new StringTokenizer(listeners, ",");
	    while( tokenizer.hasMoreTokens()) {
                listenersList.add(tokenizer.nextToken());
	    }
        }
    }

    public void setMemoryPoolNames(String poolNames) {
        if (poolNames != null) {
            memoryPoolNames =  poolNames;
	    StringTokenizer tokenizer = new StringTokenizer(poolNames, ",");
	    while( tokenizer.hasMoreTokens()) {
                poolNamesList.add(tokenizer.nextToken());
	    }
        }

    }

   
    void registerForMemoryThresholdNotifications() {
	    MemoryMXBean memMXBean = ManagementFactory.getMemoryMXBean();
	    NotificationEmitter emitter = (NotificationEmitter) memMXBean;
	    emitter.addNotificationListener(this, null, null);
	    for (String listener : listenersList) {
		    try {
		        emitter.addNotificationListener(
			    (NotificationListener)Class.forName(listener).newInstance(),
			    null, null);
		    } catch (Exception ex) {
                        _logger.log(Level.WARNING, " Error in loading class " + listener, ex) ;
		    }
	    }
	    setThresholds();
    }

    private void setThresholds() {
	    threshold += offset;
	    List<MemoryPoolMXBean> memPoolsList = 
		    ManagementFactory.getMemoryPoolMXBeans();
	    for (MemoryPoolMXBean memPoolBean : memPoolsList) {
		    if (poolNamesList.contains(memPoolBean.getName())) {
			    if(memPoolBean.isUsageThresholdSupported()) {
			        MemoryUsage usage = memPoolBean.getUsage();
			        long thrsh = (long)((usage.getMax() * threshold)/100);
			        memPoolBean.setUsageThreshold(thrsh);
		            }
	            }
            }
    }
    private void sendMailAlert(Notification notif) {
        MailAlert mAlert = new MailAlert();
	mAlert.setRecipients(mailRecipients);
	mAlert.setMailResource(mailResource);
	CompositeData cd = (CompositeData) notif.getUserData();
	MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
	String alertMsg =  "Number of times the memory usage crossed the threshold = " + info.getCount() + " for the memory pool " + info.getPoolName() +  " Memory Usage is " + info.getUsage();
	Notification n1 = new Notification(notif.getType(), notif.getSource(),notif.getSequenceNumber(),alertMsg);
        mAlert.notification(n1, null);
    }
}
