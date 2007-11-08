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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import com.sun.enterprise.admin.event.BaseDeployEvent;

/**
 * Application deployment event. This event is generated whenever a J2EE
 * application is deployed, undeployed or redeployed. 
 */
public class ApplicationDeployEvent extends BaseDeployEvent {

    /**
     * Event type
     */
    static final String eventType = ApplicationDeployEvent.class.getName();

    /**
     * Indicates whether this ApplicationDeployEvent is forced
     */
    private boolean forceDeploy = false;

    /**
     * Indicates whether what load action this ApplicationDeployEvent is for
     */
    private int loadUnloadAction;

    /**
     * Create a new ApplicationDeployEvent for the specified instance,
     * application and action code.
     *
     * @param instance name of the server instance to which application has
     *        been deployed, undeployed, redployed, enabled or disabled.
     * @param appName name of the application that has been deployed, undeployed
     *        or redeployed
     * @param actionCode what happened to the application, the valid values are
     *        BaseDeployEvent.DEPLOY, BaseDeployEvent.REDEPLOY,
     *        BaseDeployEvent.UNDEPLOY, BaseDeployEvent.ENABLE or
     *        BaseDeployEvent.DISABLE
     */
    public ApplicationDeployEvent(String instance, String appName,
            String actionCode) {
        super(eventType, instance, BaseDeployEvent.APPLICATION, appName, 
             actionCode);
    }
    
    /**
     * Creates a new ApplicationDeployEvent for the specified instance,
     * application, action code and cascade values
     */
    public ApplicationDeployEvent(String instance, String appName,
            String actionCode, boolean cascade) {
        super(eventType, instance, BaseDeployEvent.APPLICATION, appName, 
             actionCode, cascade);
    }
    /**
     * Creates a new ApplicationDeployEvent for the specified instance,
     * application, cascade and forceDeploy values
     */
    public ApplicationDeployEvent(String instance, String appName,
			String actionCode, boolean cascade, boolean forceDeploy) {
        super(eventType, instance, BaseDeployEvent.APPLICATION, appName, 
             actionCode, cascade);
        //set ForceDeploy locally
        setForceDeploy(forceDeploy);
    }

    /**
     * Creates a new ApplicationDeployEvent for the specified instance,
     * application, cascade and forceDeploy, loadUnloadAction values
     */
    public ApplicationDeployEvent(String instance, String appName,
        String actionCode, boolean cascade, boolean forceDeploy, 
        int loadUnloadAction) { 
        super(eventType, instance, BaseDeployEvent.APPLICATION, appName, 
             actionCode, cascade);
        //set loadUnloadAction locally
        setForceDeploy(forceDeploy);
        setLoadUnloadAction(loadUnloadAction);
    }

    public void setForceDeploy(boolean forceDeploy) {
        this.forceDeploy = forceDeploy;
    }

    public boolean getForceDeploy(){
        return this.forceDeploy;
    }

    public void setLoadUnloadAction(int loadUnloadAction) {
        this.loadUnloadAction = loadUnloadAction;
    }

    public int getLoadUnloadAction(){
        return this.loadUnloadAction;
    }

    /**
     * Get name of the application that was affected by deployment action.
     */
    public String getApplicationName() {
        return getJ2EEComponentName();
    }

    public String toString() {
        return "ApplicationDeployEvent -- " + this.getAction() + " " + this.getApplicationName();
    }
}
