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

import com.sun.enterprise.admin.event.AdminEvent;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Base deployment event. A deployment event is one of - deployed, undeployed,
 * redeployed, enabled or disabled on J2ee application, standalone J2EE
 * module or application server resource. The word deploy is used in two
 * meanings here - one to denote the group of actions that are related to
 * deployment and another to denote the specific action of initial deployment
 * of application, module or resource.
 */
public class BaseDeployEvent extends AdminEvent implements Cloneable{

    /**
     * Constant denoting J2EE application. 
     */
    public static final String APPLICATION = "application";

    /**
     * Constant denoting standalone J2EE module.
     */
    public static final String MODULE = "module";

    /**
     * Constant denoting application server resource.
     */
    public static final String RESOURCE = "resource";

    /**
     * Constant denoting deployment action
     */
    public static final String DEPLOY = "deploy";

    /**
     * Constant denoting undeployment action
     */
    public static final String UNDEPLOY = "undeploy";

    /**
     * Constant denoting redeployment action
     */
    public static final String REDEPLOY = "redeploy";

    /**
     * Constant denoting enable action
     */
    public static final String ENABLE = "enable";

    /**
     * Constant denoting disable action
     */
    public static final String DISABLE = "disable";

    /**
     * Constant denoting add reference action
     */
    public static final String ADD_REFERENCE = "reference-added";

    /**
     * Constant denoting remove reference action
     */
    public static final String REMOVE_REFERENCE = "reference-removed";
    
    /**
     * Application deploy event
     */
    public static final int APPLICATION_DEPLOYED       = 1;
    
    /**
     * Application undeploy event
     */
    public static final int APPLICATION_UNDEPLOYED     = 2;

    /**
     * Application redeploy event
     */
    public static final int APPLICATION_REDEPLOYED     = 3;
    
    /**
     * Module deploy event
     */
    public static final int MODULE_DEPLOYED            = 4;
    
    /**
     * Module undeploy event
     */
    public static final int MODULE_UNDEPLOYED          = 5;

    /**
     * Module redeploy event
     */
    public static final int MODULE_REDEPLOYED          = 6;

    /**
     * Module enable event
     */
    public static final int MODULE_ENABLE          = 7;

    /**
     * Module disable event
     */
    public static final int MODULE_DISABLE          = 8;
    
    /**
     * Application enable event
     */
    public static final int APPLICATION_ENABLE          = 9;

    /**
     * Application disable event
     */
    public static final int APPLICATION_DISABLE          = 10;

    /**
     * Application reference event
     */
    public static final int APPLICATION_REFERENCED          = 11;
    public static final int APPLICATION_UNREFERENCED          = 12;
    
    /**
     * Event type
     */
    static final String eventType = BaseDeployEvent.class.getName();

    protected String j2eeComponentType;
    protected String j2eeComponentName;
    protected String actionName;
    protected boolean cascade = false;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( BaseDeployEvent.class );

    /**
     * Create a new BaseDeployEvent.
     * @param instance name of the instance to which the deployment event
     *        applies
     * @param componentType type of the component on which event happened. One
     *        of BaseDeployEvent.APPLICATION, BaseDeployEvent.MODULE or
     *        BaseDeployEvent.RESOURCE
     * @param componentName name of the component on which event happened, this
     *        is either a J2EE application name or a standalone J2EE module
     *        name or an application server resource.
     * @param action deployment action - one of BaseDeployEvent.DEPLOY,
     *        BaseDeployEvent.UNDEPLOY, BaseDeployEvent.REDEPLOY,
     *        BaseDeployEvent.ENABLE, BaseDeployEvent.DISABLE
     * @throws IllegalArgumentException if specified action is not valid
     */
    public BaseDeployEvent(String instance, String componentType,
            String componentName, String action) {
        this(eventType, instance, componentType, componentName, action);
    }

    /**
     * Create a new BaseDeployEvent.
     * @param type event type, a string representation for the event
     * @param instance name of the instance to which the deployment event
     *        applies
     * @param componentType type of the component on which event happened. One
     *        of BaseDeployEvent.APPLICATION, BaseDeployEvent.MODULE or
     *        BaseDeployEvent.RESOURCE
     * @param componentName name of the component on which event happened, this
     *        is either a J2EE application name or a standalone J2EE module
     *        name or an application server resource.
     * @param action deployment action - one of BaseDeployEvent.DEPLOY,
     *        BaseDeployEvent.UNDEPLOY, BaseDeployEvent.REDEPLOY,
     *        BaseDeployEvent.ENABLE, BaseDeployEvent.DISABLE
     * @throws IllegalArgumentException if specified action is not valid
     */
    public BaseDeployEvent(String type, String instance, String componentType,
            String componentName, String action) {
        this(type, instance, componentType, componentName, action, false);
    }
    
    public BaseDeployEvent(String type, String instance, String componentType,
            String componentName, String action,  boolean cascade) {
        super(type, instance);
        j2eeComponentType = componentType;
        j2eeComponentName = componentName;
        setAction(action);
        setCascade(cascade);
    }

    public BaseDeployEvent(String type, Object source, 
                           long seqNumber,long time) {
        super(type, source, seqNumber, time);
    }

    /**
     * Get type of component on which this event happened.
     * @return one of BaseDeployEvent.APPLICATION, BaseDeployEvent.MODULE or
     *     BaseDeployEvent.RESOURCE
     */
    public String getJ2EEComponentType() {
        return j2eeComponentType;
    }

    /**
     * Get name of the component on which this event happened. This is either
     * application name, module name or resource name.
     */
    public String getJ2EEComponentName() {
        return j2eeComponentName;
    }

    /**
     * Get deployment action for this event.
     */
    public String getAction() {
        return actionName;
    }

    /**
     * Set action to specified value. If action is not one of DEPLOY, UNDEPLOY,
     * REDEPLOY, ENABLE or DISABLE then IllegalArgumentException is thrown.
     * @throws IllegalArgumentException if action is invalid
     */
    protected void setAction(String action) {
        boolean valid = false;
        if (DEPLOY.equals(action) || UNDEPLOY.equals(action)
                || REDEPLOY.equals(action) || ENABLE.equals(action)
                || DISABLE.equals(action) || ADD_REFERENCE.equals(action) 
                || REMOVE_REFERENCE.equals(action)) {
            valid = true;
        }
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_action", action );
            throw new IllegalArgumentException( msg );
        }
        this.actionName = action;
    }

    /**
     *
     */
    public boolean getCascade() {
        return cascade;
    }
    
    /**
     *
     */
    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
