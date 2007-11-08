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
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


package com.sun.xml.registry.common.util;

/**
 * 
 *
 * Provides some version utilities.
 */

public final class VersionUtil implements Version {
    
    /**
     * GetJAX-R full version, like: "JAX-R Reference Implementation 1.0.5 JAXR_RI_JWSDP13_SCF_b03 "
     * 
     * Method getJAXRCompleteVersion.
     * @return String
     */
    public static String getJAXRCompleteVersion() {
        return PRODUCT_NAME + " Version " + VERSION_NUMBER + " Build " + BUILD_TAG_NUMBER;

    }

    /**
     * Method getJAXRVersion.
     * @return String
     */
    public static String getJAXRVersion() {
        return VERSION_NUMBER;
    }

    /**
     * Method getJAXRBuildNumber.
     * @return String
     */
    public static String getJAXRBuildNumber() {
        return BUILD_TAG_NUMBER;
    }

    /**
     * Method getJAXRProductName.
     * @return String
     */
    public static String getJAXRProductName() {
        return PRODUCT_NAME;
    }

   
   
    
}
