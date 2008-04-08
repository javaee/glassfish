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

package com.sun.enterprise.v3.admin.adapter;

/** A package-private class that holds the state of the admin adapter.
 *  It also acts as a lock that needs to be synchronized externally.
 *  Note that this class is not thread-safe on its own.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
enum AdapterState {
    
    UNINITIAZED("State is uninitialized ..."),
    INSTALLING("Installing the application in this server ..."),
    APPLICATION_NOT_INSTALLED("Application is not yet installed ..."),
    APPLICATION_INSTALLED_BUT_NOT_LOADED("Application is already installed, but not yet loaded. ..."),
    APPLICATION_LOADED("Application is already loaded ...");
    
    private final String desc;
    
    private AdapterState(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString() {
        return (desc);
    }
}