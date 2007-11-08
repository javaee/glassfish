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
 * This interface holds version information for the whole JAX-R RI.
 *
 * @author JAX-R RI Development Team
 */

public interface Version {

    /**
     * JAX-R RI product name
     */
    public static final String PRODUCT_NAME = "JAXR Standard Implementation";

    /**
     * JAX-R RI version number
     */
    public static final String VERSION_NUMBER = "1.0.5";

    /**
     * JAX-R RI build number
     */
    public static final String BUILD_TAG_NUMBER = "JAXR_RI_JWSDP13_HCF_b07";
}
