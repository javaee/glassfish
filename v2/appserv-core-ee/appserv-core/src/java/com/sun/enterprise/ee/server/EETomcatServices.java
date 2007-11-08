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
package com.sun.enterprise.ee.server;

import com.sun.enterprise.server.TomcatServices;
import com.sun.enterprise.server.pluggable.InternalServicesList;
import com.sun.enterprise.admin.server.core.AdminService;

/**
 * pluggable interface (this class will provide ApplicationServer
 * with the right lifecycles array)
 */
public class EETomcatServices extends TomcatServices 
    implements  InternalServicesList
{
    private final String JESMF_LIFECYCLE_CLASS
        = "com.sun.enterprise.ee.admin.jesmf.lifecycle.JESMFLifeCycle";

    public String[][] getServicesByName() {

        final String[][] services = super.getServicesByName();
        final int size = services.length;

        int extraServices;
        try {
            Class.forName(JESMF_LIFECYCLE_CLASS);
            extraServices = 6;   //FIXME: if JESMFLifeCycle class present
        } catch (Exception ex) {
            extraServices = 5;  //Else
        }

        final String[][] result = new String[size + extraServices][];
        result[0] = services[0];//AdminService continues as first service
        //GMS is the second service
        result[1] = new String[] {"GroupManagementService",
            "com.sun.enterprise.ee.cms.lifecycle.GMSLifecycleImpl"};
        for (int i = 1; i < size; i++) {
            result[i+1] = services[i];
        }

        //EJBComponent
        result[size+1] = new String[] {"EJBComponentService", 
            "com.sun.ejb.ee.timer.lifecycle.EJBLifecycleImpl"};

        // cascading
        result[size+2] = new String[] {"Cascading",
            "com.sun.enterprise.ee.admin.cascading.CascadingLifecycleImpl"};

        // loadbalancer
        result[size+3] = new String[] {"Loadbalancer",
                "com.sun.enterprise.ee.admin.lbadmin.lifecycle.LoadbalancerAdminLifeCycle"};

        // jxta replication
        result[size+4] = new String[] {"JxtaReplication",
                "com.sun.enterprise.ee.web.initialization.ReplicationLifecycleImpl"};

        // jes-mf
        if (extraServices == 6)  //FIXME 
        {
            result[size+5] = new String[] {"JESMF",
               "com.sun.enterprise.ee.admin.jesmf.lifecycle.JESMFLifeCycle"};
        }        
        return result;
   }
}
