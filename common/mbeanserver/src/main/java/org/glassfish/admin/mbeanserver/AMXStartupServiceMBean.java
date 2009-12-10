/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.mbeanserver;

import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.glassfish.api.amx.AMXLoader;
import org.glassfish.external.amx.AMXUtil;

import org.jvnet.hk2.annotations.Contract;

/**
    MBean representing AMX, once started.

    @see org.glassfish.admin.amx.loader.AMXStartupService
 */
@Contract
public interface AMXStartupServiceMBean extends AMXLoader
{
    public ObjectName getDomainRoot();

    public JMXServiceURL[] getJMXServiceURLs();

    /** ObjectName of the MBean which actually laods AMX MBeans; that MBean references this constant */
    public static final ObjectName OBJECT_NAME = AMXUtil.newObjectName(LOADER_PREFIX + "startup");

}




