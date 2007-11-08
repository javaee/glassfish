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

package com.sun.enterprise.admin.server.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.common.constant.AdminConstants;

import java.util.Hashtable;

/**
    This class holds results for all instances in this admin server.
    The APIs in this class mirror ones in InstanceEnvironment.
    InstanceEnvironment actually checks timestamps, etc whereas this file
    gets it from cache and is significantly faster.
	@author  Sridatta
	@version 1.1
*/

public final class ManualChangeManager {
    /**
     * Logger for admin service
     */
    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    
    private static Hashtable result = new Hashtable();
            
    public static boolean hasHotChanged(String instance) {
         ManualChangeStatus mcs = (ManualChangeStatus)result.get(instance);
        if(mcs == null) return false; // not calculated yet. so return false
        
        return mcs.isChanged();
    }

    private static boolean hasHotInitChanged(String instanceName) {
        ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isInitFileChanged();
    }
    
    private static boolean hasHotRealmsKeyChanged(String instanceName) {
         ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isRealmsKeyFileChanged();
    }

    private static boolean hasHotObjectChanged(String instanceName) {
        ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isObjectFileChanged();
    }
    
    public static boolean hasHotXmlChanged(String instanceName) {
       ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isServerXmlFileChanged();
    }
    
    private static boolean hasHotMimeChanged(String instanceName) {
        ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isMimeFileChanged();
    }
    
    private static boolean hasHotVirtualServerConfFilesChanged(String instanceName) {
        ManualChangeStatus mcs = getManualChangeStatus(instanceName);
        if(mcs == null) return false;
        return mcs.isVirtualServerConfFilesChanged();
    }

    public static ManualChangeStatus getManualChangeStatus(String instance) {
        return (ManualChangeStatus) result.get(instance);
    }
    
    public static void removeManualChangeStatus(String instance) {
        result.remove(instance);
    }
    
    public static void removeServerXmlManualChangeStatus(String instance) {
        ManualChangeStatus m = getManualChangeStatus(instance);
        if(m != null) {
            m.setServerXmlFileChanged(false);
        }
    }
    
    public static void addManualChangeStatus(String instance, ManualChangeStatus mcs) {
        result.put(instance, mcs);
    }
}
