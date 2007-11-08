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
 * GenericConstants.java
 */

package com.sun.jbi.jsf.framework.common;

import org.w3c.dom.Element;


/**
 *
 * @author Sun Microsystems
 */
public interface GenericConstants {
    
    // all constants are $NON-NLS$
    
    public static final String SUCCESS = "success";
    public static final String FAILIRE = "failure";

    public static final String COMPONENT_NAME = "name";
    public static final String COMPONENT_TYPE = "type";
    public static final String COMPONENT_CTYPE = "ctype";
    public static final String COMPONENT_CNAME = "cname";
    public static final String COMPONENT_PNAME = "pname";
    public static final String COMPONENT_TNAME = "tname";
    public static final String COMPONENT_SUNAME = "serviceUnitName";
    public static final String COLON_SEPARATOR = ":";
    
    public static final String BC_TYPE_MBEAN_NAME = "bindingComponents";  //$NON-NLS-1$
    public static final String SE_TYPE_MBEAN_NAME = "serviceEngines";     //$NON-NLS-1$    
    public static final String SA_TYPE_MBEAN_NAME = "serviceAssemblies";  //$NON-NLS-1$  
    public static final String SU_TYPE_MBEAN_NAME = "serviceUnits";       //$NON-NLS-1$  

    //public static final String BC_TYPE = "BC";  //$NON-NLS-1$
    //public static final String SE_TYPE = "SE";  //$NON-NLS-1$    
    //public static final String SA_TYPE = "SA";  //$NON-NLS-1$  
    //public static final String SU_TYPE = "SU";  //$NON-NLS-1$  

    public static final String BC_TYPE = "binding-component";  //$NON-NLS-1$
    public static final String SE_TYPE = "service-engine";  //$NON-NLS-1$    
    public static final String SA_TYPE = "service-assembly";  //$NON-NLS-1$  
    public static final String SU_TYPE = "service-unit";  //$NON-NLS-1$    

    public static final String AT_SEPARATOR = "_at_"; // at separator //$NON-NLS-1$
    public static final String COMPONENT_PATH_SEPARATOR = "_";        //$NON-NLS-1$
    public static final String COMMA_SEPARATOR = ",";                 //$NON-NLS-1$
    public static final String HASH_SEPARATOR = "_s_";                //$NON-NLS-1$
    public static final String AT_SIGN_SEPARATOR = "@";               //$NON-NLS-1$
    public static final String HYPHEN_SEPARATOR = "-";                //$NON-NLS-1$

    public static final String DOMAIN_SERVER = "domain";              //$NON-NLS-1$
    public static final String ADMIN_SERVER = "server";		      //$NON-NLS-1$
    
    public static final String SUN_JBI_DOMAIN_NAME = "com.sun.jbi";         //$NON-NLS-1$
    public static final String STC_EBI_DOMAIN_NAME = "com.sun.ebi";         //$NON-NLS-1$
    
    public static final String BINDING_INSTALLED_TYPE = "Binding";          //$NON-NLS-1$
    public static final String ENGINE_INSTALLED_TYPE = "Engine";            //$NON-NLS-1$
    
    /** state  Loaded status.  */
    public static final String UNKNOWN_STATE = "Unknown"; //$NON-NLS-1$
    /** Installed status */
    public static final String SHUTDOWN_STATE = "Shutdown"; //$NON-NLS-1$
    /** Stopped status  */
    public static final String STOPPED_STATE = "Stopped"; //$NON-NLS-1$
    /** Started status */
    public static final String STARTED_STATE = "Started"; //$NON-NLS-1$    
    
    public static final String LIST_BINDING_COMPONENTS_OPERATION_NAME = "listBindingComponents"; //$NON-NLS-1$
    public static final String LIST_SERVICE_ENGINES_OPERATION_NAME = "listServiceEngines"; //$NON-NLS-1$
    public static final String LIST_SHARED_LIBRARIES_OPERATION_NAME = "listSharedLibraries"; //$NON-NLS-1$
    public static final String LIST_SERVICE_ASSEMBLIES_OPERATION_NAME = "listServiceAssemblies"; //$NON-NLS-1$
    public static final String LIST_SHARED_LIBRARY_DEPENDENTS_OPERATION_NAME = "listSharedLibraryDependents"; //$NON-NLS-1$
    
    

}
