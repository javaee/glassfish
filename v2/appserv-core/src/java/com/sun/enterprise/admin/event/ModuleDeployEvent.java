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

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Module deployment event. This event is generated whenever a J2EE
 * module is deployed, undeployed, redeployed, enabled or disabled. 
 */
public class ModuleDeployEvent extends BaseDeployEvent {

    /**
     * Constant to denote standalone web module
     */
    public static final String TYPE_WEBMODULE = "web";

    /**
     * Constant to denote standalone ejb module
     */
    public static final String TYPE_EJBMODULE = "ejb";

    /**
     * Constant to denote connector module
     */
    public static final String TYPE_CONNECTOR = "connector";
    
    
    /**
     * Constant to denote appclient module
     */
    public static final String TYPE_APPCLIENT = "appclient";

    /**
     * Int constant to denote standalone web module
     */
    public static final int TYPE_WEBMODULE_CODE = 1;

    /**
     * Int constant to denote standalone ejb module
     */
    public static final int TYPE_EJBMODULE_CODE = 2;

    /**
     * Int constant to denote connector module
     */
    public static final int TYPE_CONNECTOR_CODE = 3;

    /**
     * Int constant to denote appclient module
     */
    public static final int TYPE_APPCLIENT_CODE = 4;
    
    /**
     * Event type
     */
    static final String eventType = ModuleDeployEvent.class.getName();

    /**
     * Module type.
     */
    private String moduleType;

    /**
     * Module type code.
     */
    private int moduleTypeCode;

    /**
     * Indicates whether the ModuleDeployEvent was forced
     */   
    private boolean forceDeploy = false;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ModuleDeployEvent.class );

    /**
     * Create a new ModuleDeployEvent for the specified instance,
     * module (name and type) and action code.
     *
     * @param instance name of the server instance to which module has
     *        been deployed, undeployed or redployed.
     * @param moduleName name of the module that has been deployed,
     *        undeployed, redeployed, enabled or disabled
     * @param moduleType type of the module - one of TYPE_WEBMODULE,
     *        TYPE_EJBMODULE, TYPE_CONNECTOR
     * @param actionCode what happened to the module, the valid values are
     *        BaseDeployEvent.DEPLOY, BaseDeployEvent.REDEPLOY,
     *        BaseDeployEvent.UNDEPLOY, BaseDeployEvent.ENABLE or
     *        BaseDeployEvent.DISABLE
     * @throws IllegalArgumentException if moduleType or actionCode is invalid
     */
    public ModuleDeployEvent(String instance, String moduleName,
            String moduleType, String actionCode) {
        super(eventType, instance, BaseDeployEvent.MODULE, moduleName, actionCode);
        setModuleType(moduleType);
    }

    /**
     * Creates a new ModuleDeployEvent for the provided instance,
     * moduleName, moduleType, actionCode and cascade
     */
    public ModuleDeployEvent(String instance, String moduleName,
            String moduleType, String actionCode, boolean cascade) {
        super(eventType, instance, BaseDeployEvent.MODULE, moduleName, actionCode, cascade);
        setModuleType(moduleType);
    }

   /**
    * Creates a new ModuleDeployEvent for the provided instance,
    * moduleName, moduleType, actionCode, cascade and
    * forceDeploy values
    */
    public ModuleDeployEvent(String instance, String moduleName,
                 String moduleType, String actionCode, 
                 boolean cascade, boolean forceDeploy) {
        super(eventType, instance, BaseDeployEvent.MODULE, moduleName, actionCode, cascade);
        setModuleType(moduleType);
        setForceDeploy(forceDeploy);
    }

    public void setForceDeploy(boolean forceDeploy) {
        this.forceDeploy = forceDeploy;
    }
    
    public boolean getForceDeploy(){
        return this.forceDeploy;
    }

    /**
     * Get name of the module that was affected by deployment action.
     */
    public String getModuleName() {
        return getJ2EEComponentName();
    }

    /**
     * Get type of the module. Possible values are TYPE_WEBMODULE, 
     * TYPE_EJBMODULE and TYPE_CONNECTOR.
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * Get module type code. Possible values are TYPE_WEBMODULE_CODE, 
     * TYPE_EJBMODULE_CODE and TYPE_CONNECTOR_CODE.
     */
    public int getModuleTypeCode() {
        return moduleTypeCode;
    }

    /**
     * Helper method to validate and set module type
     */
    private void setModuleType(String modType) {
        boolean valid = true;
        int modTypeCode = 0;
        if (TYPE_WEBMODULE.equals(modType)) {
            modTypeCode = TYPE_WEBMODULE_CODE;
        } else if (TYPE_EJBMODULE.equals(modType)) {
            modTypeCode = TYPE_EJBMODULE_CODE;
        } else if (TYPE_CONNECTOR.equals(modType)) {
            modTypeCode = TYPE_CONNECTOR_CODE;
        } else if (TYPE_APPCLIENT.equals(modType)) {
            modTypeCode = TYPE_APPCLIENT_CODE;
        } else {
            valid = false;
        }
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_module_type", modType );
            throw new IllegalArgumentException( msg );
        }
        this.moduleType = modType;
        this.moduleTypeCode = modTypeCode;
    }

    /**
     * Return a useful string representation.
     */
    public String toString() {
        return "ModuleDeployEvent -- " + getAction() + " " + moduleType + "/" + getModuleName();
    }
}
