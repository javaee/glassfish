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

package com.sun.enterprise.update.client.api;

import com.sun.enterprise.update.beans.Module;
import java.util.List;

/**
 * This API is used to list either installed modules, available new modules or
 * new updates.
 *
 * @author Satish Viswanatham
 */
public class Modules {
    
    /**
     *  This method is used to get only selected modules/updates from a catelog
     *
     *  @return Module[] - selected modules
     */
    public Module[] getModules(int startIndex, int size) {return null;}

    /**
     *  This method is used to get all modules/updates from a catelog
     *
     *  @return Module[] - all the modules
     */
    public Module[] getAllModules() {return null;}

    /**
     *  This method is used to get ID of the catelog
     *
     *  @return ID of the catelog 
     */
    public String getCatelogID() {return catelogID;}

    public Modules() {}

    public Modules(String cID) { catelogID = cID;}

    protected String catelogID = null;
}
