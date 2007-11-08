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


package com.sun.enterprise.admin.mbeans;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.common.exception.AFRuntimeException;

/**
 * The MBean is used for overriding the setAttribute functionality of the BaseConfigMBean.
 * When the monitoring-level of one of the following changes, the others are should also be
 * impacted. This is a special case that impact only the following components:
 * connector-service
 * jms-service
 * connector-connection-pool
 */
public class ModuleMonitoringLevelsMBean extends BaseConfigMBean {
    
    public static final String CONNECTOR_SERVICE_ATTR_NAME = "connector-service";
    public static final String JMS_SERVICE_ATTR_NAME = "jms-service";
    public static final String CONNECTOR_CONNPOOL_ATTR_NAME = "connector-connection-pool";
    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);

    /**
     * Creates a new instance of ModuleMonitoringLevelsMBean
     */
    public ModuleMonitoringLevelsMBean() {
    
    }
    
    public void setAttribute(Attribute attr) { 
        try {
            super.setAttribute(attr);
            String attrName = attr.getName();
            AttributeList otherAttrs = new AttributeList();
            sLogger.log(Level.FINE, "modulemonitoringlevelsmbean.attribute_name", attrName);
            if(attrName.equals(CONNECTOR_SERVICE_ATTR_NAME)) {
                
                otherAttrs.add(new Attribute(JMS_SERVICE_ATTR_NAME, attr.getValue()));
                otherAttrs.add(new Attribute(CONNECTOR_CONNPOOL_ATTR_NAME, attr.getValue()));
            }
            else if(attrName.equals(JMS_SERVICE_ATTR_NAME))
            {
                
                otherAttrs.add(new Attribute(CONNECTOR_CONNPOOL_ATTR_NAME, attr.getValue()));
                otherAttrs.add(new Attribute(CONNECTOR_SERVICE_ATTR_NAME, attr.getValue()));
            }
            else if(attrName.equals(CONNECTOR_CONNPOOL_ATTR_NAME)) 
            {
                
                otherAttrs.add(new Attribute(CONNECTOR_SERVICE_ATTR_NAME, attr.getValue()));
                otherAttrs.add(new Attribute(JMS_SERVICE_ATTR_NAME, attr.getValue()));
            }
            super.setAttributes(otherAttrs);
        } catch(AFRuntimeException afe) {
            throw afe;
        } catch(Exception e) {
            sLogger.log(Level.WARNING, "modulemonitoringlevelsmbean.set_failed");
            sLogger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
    }
    
    public AttributeList setAttributes(AttributeList attrs) {
        
        AttributeList attrList = new AttributeList();
        Iterator it = attrs.iterator();
        Attribute attr = null;
        while(it.hasNext()) {
            try {
                attr = (Attribute)it.next();
                setAttribute(attr);
            } catch(AFRuntimeException afe) {
                throw afe;
            } catch(Exception e) {
                sLogger.log(Level.WARNING, "modulemonitoringlevelsmbean.set_failed");
                sLogger.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
            attrList.add(attr);
        }
        return attrList;
    }
    
}
