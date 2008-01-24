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
 
package com.sun.appserv.management.ext.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Base interface implemented by external modules.
 * All the extended AMX Support Features can be added here.
 * This interface is <b>not</b> for public consumption. 
 *
 * @author: Yamini K B
 */

public interface ExtendedAMX {

    /**
     * Get all external AMX interfaces.
     *
     * @return Class[] containing the interfaces.
     */ 
    public Class[] getExternalMbeanInterfaces();

    /**
     * Get all the new config types.
     *
     * @return String[] containing the new config types. 
     */
    public String[] getAllConfigTypes();

    /**
     * Get the child types for a config type.
     *
     * @return Object[] containing all the child types.
     */
    public Object[] getAllChildTypes();

    /**
     * Get all misc child types for a config type.
     *
     * @return String[][] containing all the misc child types.
     */
    public String[][] getAllMiscChildTypes();

    /**
     * Get the old config types.
     *
     * @return Map containing the old config types.
     */
    public Map<String,String> getOldConfigTypes();

    /**
     * Get the interface implementation classes
     *
     * @return Map conating the interface implementations.
     */
    public Map<String,String> getInterfaceImpls();

    /**
     * Get the old monitor types.
     *
     * @return Map containing the old monitor types.
     */
    public Map<String,String> getOldMonitorTypes();

    /**
     * Get all the new monitor types.
     *
     * @return String[] containing the new monitor types. 
     */
    public String[] getAllServerRootMonitorTypes();

    /**
     * Get the child types for a monitor type.
     *
     * @return Object[] containing all the monitor child types.
     */
    public Object[] getAllMonitorChildTypes();

}
