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

package com.sun.enterprise.update.config.api;

/**
 * API to get the client configuration for Update Center module.
 *
 * @author Satish Viswanatham
 */
public interface UpdateCenterConfig {
    
    /**
     *  This method returns the full URL of the update center server
     *  An example would be https://download.sun.com/glass_fish_v2_catalog.xml
     *
     *  @return String[] Update Center server's URLs for all the catelogs
     */
    public String[] getServerURL(); 

    /**
     *  This method returns the proxy server host name
     *
     *  @return String Host name of the proxy server
     */
    public String getProxyHost(); 

    /**
     *  This method returns the proxy server port number
     *
     *  @return int port number of the proxy server
     */
    public int getProxyPort(); 

}
