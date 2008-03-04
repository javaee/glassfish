

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.catalina.security;

import java.security.Security;
import org.apache.catalina.startup.CatalinaProperties;

/**
 * Util class to protect Catalina against package access and insertion.
 * The code are been moved from Catalina.java
 * @author the Catalina.java authors
 * @author Jean-Francois Arcand
 */
public final class SecurityConfig{
    private static SecurityConfig singleton = null;

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( SecurityConfig.class );

    
    private final static String PACKAGE_ACCESS =  "sun.,"
                                                + "org.apache.catalina." 
                                                + ",org.apache.jasper."
                                                + ",org.apache.coyote."
                                                + ",org.apache.tomcat.";
    
    private final static String PACKAGE_DEFINITION= "java.,sun."
                                                + ",org.apache.catalina." 
                                                + ",org.apache.coyote."
                                                + ",org.apache.tomcat."
                                                + ",org.apache.jasper.";
    /**
     * List of protected package from conf/catalina.properties
     */
    private String packageDefinition;
    
    
    /**
     * List of protected package from conf/catalina.properties
     */
    private String packageAccess; 
    
    
    /**
     * Create a single instance of this class.
     */
    private SecurityConfig(){  
        try{
            packageDefinition = CatalinaProperties.getProperty("package.definition");
            packageAccess = CatalinaProperties.getProperty("package.access");
        } catch (java.lang.Exception ex){
            if (log.isDebugEnabled()){
                log.debug("Unable to load properties using CatalinaProperties", ex); 
            }            
        }
    }
    
    
    /**
     * Returns the singleton instance of that class.
     * @return an instance of that class.
     */
    public static SecurityConfig newInstance(){
        if (singleton == null){
            singleton = new SecurityConfig();
        }
        return singleton;
    }
    
    
    /**
     * Set the security package.access value.
     */
    public void setPackageAccess(){
        // If catalina.properties is missing, protect all by default.
        if (packageAccess == null){
            setSecurityProperty("package.access", PACKAGE_ACCESS);   
        } else {
            setSecurityProperty("package.access", packageAccess);   
        }
    }
    
    
    /**
     * Set the security package.definition value.
     */
     public void setPackageDefinition(){
        // If catalina.properties is missing, protect all by default.
         if (packageDefinition == null){
            setSecurityProperty("package.definition", PACKAGE_DEFINITION);
         } else {
            setSecurityProperty("package.definition", packageDefinition);
         }
    }
     
     
    /**
     * Set the proper security property
     * @param properties the package.* property.
     */
    private final void setSecurityProperty(String properties, String packageList){
        if (System.getSecurityManager() != null){
            String definition = Security.getProperty(properties);
            if( definition != null && definition.length() > 0 ){
                definition += ",";
            }

            Security.setProperty(properties,
                // FIX ME package "javax." was removed to prevent HotSpot
                // fatal internal errors
                definition + packageList);      
        }
    }
    
    
}




