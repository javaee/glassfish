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

package com.sun.enterprise.server.ondemand;

import java.util.*;
import java.util.logging.*;
import com.sun.logging.LogDomains;
import com.sun.enterprise.server.ondemand.entry.*;
import com.sun.enterprise.server.*;
import com.sun.enterprise.config.ConfigException;


/**
 * System apps are loaded by this class. ServiceGroups use this
 * class to load system apps that belong to them
 *
 * @author Binod PG
 * @see ServiceGroup
 */
public class SystemAppLoader {

    private Hashtable apps = new Hashtable();

    private ApplicationManager appsMgr  = ManagerFactory.getApplicationManager();
    private StandAloneEJBModulesManager ejbMgr = ManagerFactory.getSAEJBModulesManager();
    //private DummyWebModulesManager webMgr  = ManagerFactory.getSAWebModulesManager();

    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    final Object [][] ejbSGApps = { 
                           {"MEjbApp", appsMgr}, 
                           {"__ejb_container_timer_app", appsMgr}
                         };
    // All web system apps, except webstart app,
    // are loaded by PEWebContainer, when it starts.
    // So, ondemand code does not need to worry about that.
    final Object[][] webSGApps = {
                          // {"adminapp",null}, 
                          // {"com_sun_web_ui", null}, 
                          // {"admingui", null},
                           {"__JWSappclients", appsMgr}
                         };
  
    /**
     * Constructs a datastructure that holds all system app info.
     */
    public SystemAppLoader() throws ConfigException {

        for (int i=0; i < ejbSGApps.length; i++) {
            apps.put(ejbSGApps[i][0], ejbSGApps[i][1]);
        }

        /** Web container is started by default. So, dont 
            control these system apps
        for (int i=0; i < webSGApps.length; i++) {
            apps.put(webSGApps[i][0], webSGApps[i][1]);
        }
        */
    }

    public ArrayList getEjbServiceGroupSystemApps() {
        return createArrayList(ejbSGApps);
    }

    public ArrayList getWebServiceGroupSystemApps() {
        return createArrayList(webSGApps);
    }

    public ArrayList getResourcesServiceGroupSystemApps() {
        return null;
    }

    private ArrayList createArrayList(Object[][] objArray) {
        ArrayList list = new ArrayList();
        for (int i=0; i < objArray.length; i ++) {
            list.add(objArray[i][0]);
        }       
        return list;
    }

    public void loadSystemApps(ArrayList ids) {
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            Object appName = it.next();
            if (_logger.isLoggable(Level.INFO)) {
                _logger.log(Level.INFO, "About to load the system app: " + appName);
            }
            if (apps.containsKey(appName)) {
                AbstractManager mgr = (AbstractManager) apps.get(appName);           
                mgr.loadOneSystemApp((String) appName, true);
            }
        }
    }


    public boolean isOnDemandSystemApp(String id) {
        return apps.containsKey(id);
    }

}
