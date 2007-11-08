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

package com.sun.enterprise.update.registry.api;

import com.sun.enterprise.update.beans.ModuleGroup;
import java.util.Date;

/**
 * API to get installed modules and their information in the client. 
 *
 * @author Satish Viswanatham
 */
public interface ModuleRegistry {
    
    /**
     *  This method returns the catalog file name for this registry
     *
     *  @return name of the catalog file name 
     */
    public String getCatalogFileName();

    /**
     *  This method returns the timestamp for the last update for this catalog
     *
     *  @return the last update timestamp
     */
    public Date getLastUpdatedTime();

    /**
     *  This method returns the installed module groups' name
     *
     *  @return String[] module groups' names
     */
    public String[] getInstalledModuleGroup(); 

    /**
     *  This method checks for a particular installed module group
     *
     *  @return true, if the module group is installed, false otherwise 
     */
    public boolean isModuleGroupInstalled(); 

    /**
     *  This method returns the proxy server port number
     *
     *  @return int port number of the proxy server
     */
    public ModuleGroup getModuleGroup(); 

}
