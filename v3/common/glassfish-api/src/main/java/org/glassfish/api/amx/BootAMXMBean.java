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
package org.glassfish.api.amx;

import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.glassfish.api.amx.AMXLoader;
import org.glassfish.external.amx.AMXGlassfish;

/**
    MBean responsible for booting the AMX system.
 */
@org.glassfish.external.arc.Taxonomy(stability = org.glassfish.external.arc.Stability.UNCOMMITTED)
public interface BootAMXMBean 
{
    /**
    Start AMX and return the ObjectName of DomainRoot.
     */
    public ObjectName bootAMX();
    
    /** same as method above */
    public static final String BOOT_AMX_OPERATION_NAME = "bootAMX";

    public JMXServiceURL[] getJMXServiceURLs();
    
    /** ObjectName for BooterNewMBean */
    public static final ObjectName OBJECT_NAME = AMXUtil.newObjectName(AMXGlassfish.DEFAULT.amxSupportDomain(), "type=boot-amx");

}




