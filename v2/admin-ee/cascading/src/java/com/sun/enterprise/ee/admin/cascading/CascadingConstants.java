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

package com.sun.enterprise.ee.admin.cascading;

import java.util.*;

import com.sun.appserv.management.base.AMX;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants;

/**
 * Constants used for cascading like
 * filters, which are used to cascade particular type(s) of mBeans
 * 
 * @author Sreenivas Munnangi
 */

public interface CascadingConstants {

    // cascading filters
    public static final String runtimeFilter = ":category=runtime,*";
    public static final String runtimeServerFilter = ":category=runtime,j2eeType=J2EEServer,*";
    public static final String monitoringFilter = ":category=monitor,*";
    public static final String webmoduleFilter = ":j2eeType=WebModule,*";
    public static final String servletFilter = ":j2eeType=Servlet,*";
    
    public static final String amxFilter    = AMX.JMX_DOMAIN + ":*";

    public static final String customMBeansFilter = 
		CustomMBeanConstants.CUSTOM_MBEAN_DOMAIN + ":*";

    public static final String jbiFilter    = "com.sun.jbi:ControlType=Custom,*"; 
    public static final String jbiLogFilter    = "com.sun.jbi:ControlType=Logger,*"; 

    // DottedName related changes
    public static final String MONITOR_PROPERTY_NAME = "category";
    public static final String MONITOR_PROPERTY_VAL = "monitor";
    public static final String JNDI_PROPERTY_NAME = "type";
    public static final String JNDI_PROPERTY_VAL = "jndi";
    public static final String SERVER_PROPERTY_NAME = "name";


}
