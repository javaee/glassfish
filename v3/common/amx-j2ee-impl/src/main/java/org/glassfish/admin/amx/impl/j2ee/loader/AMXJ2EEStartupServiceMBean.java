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
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.amx.impl.j2ee.loader;

import javax.management.ObjectName;

import org.glassfish.admin.amx.core.Util;
import org.glassfish.api.amx.AMXLoader;

/**
    MBean responsible for starting AMX configuration support.
    
    @see AMXConfigStartupService
 */
public interface AMXJ2EEStartupServiceMBean extends AMXLoader
{
    
    /** ObjectName of the MBean which actually loads AMX MBeans; that MBean references this constant */
    public static final ObjectName OBJECT_NAME = Util.newObjectName( LOADER_PREFIX + "j2ee" );
    
    public ObjectName getJ2EEDomain();
}




