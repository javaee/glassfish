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

import java.util.ArrayList;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.config.ConfigChange;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Resource deployment event. This event is generated whenever a application
 * server resource is deployed, undeployed, redeployed, enabled or disabled.
 *
 */
public class ResourceDeployEvent extends BaseDeployEvent implements Cloneable {

    /**
     * Constant to denote custom resource type.
     */
    public static final String RES_TYPE_CUSTOM = "custom";

    /**
     * Constant to denote external jndi resource type.
     */
    public static final String RES_TYPE_EXTERNAL_JNDI = "external-jndi";

    /**
     * Constant to denote jdbc resource type.
     */
    public static final String RES_TYPE_JDBC = "jdbc";

    /**
     * Constant to denote mail resource type.
     */
    public static final String RES_TYPE_MAIL = "mail";

    /**
     * Constant to denote jms resource type.
     */
    public static final String RES_TYPE_JMS = "jms";

    /**
     * Constant to denote persistence manager factory resource type.
     */
    public static final String RES_TYPE_PMF = "pmf";

    /**
     * Constant to denote jdbc connection pool resource type.
     */
    public static final String RES_TYPE_JCP = "jcp";

    /**
     * Constant to denote admin object resource type.
     */
    public static final String RES_TYPE_AOR = "aor";
    
    /**
     * Constant to denote connector connection pool  resource type.
     */
    public static final String RES_TYPE_CCP = "ccp";
    
    /**
     * Constant to denote connector resource type.
     */
    public static final String RES_TYPE_CR = "cr";   
    
    /**
     * Constant to denote resource adapter config type.
     */
    public static final String RES_TYPE_RAC = "rac";   
    
    /**
     * Event type
     */
    static final String eventType = ResourceDeployEvent.class.getName();

    /**
     * Resource type. The valid values are Custom, external
     */
    private String resourceType;

    /**
     * Resource exists. This is set to true if the event is created with
     * action code of REDEPLOY, UNDEPLOY, ENABLE OR DISABLE. Otherwise, it
     * is set to false.
     */
    private boolean resourceExists = true;

    /**
     *
     */
    private boolean noOp = false;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ResourceDeployEvent.class );

    /**
     * Create a new ResourceDeployEvent for the specified instance,
     * resource (type and name) and action code.
     *
     * @param instance name of the server instance to which resource has
     *        been deployed, undeployed or redployed.
     * @param resourceName name of the resource that has been deployed,
     *        undeployed, redeployed, enabled or disabled
     * @param resourceType of the resource, one of RES_TYPE_CUSTOM,
     *        RES_TYPE_EXTERNAL_JNDI, RES_TYPE_JDBC, RES_TYPE_MAIL,
     *        RES_TYPE_JMS, RES_TYPE_PMF, RES_TYPE_JCP.
     * @param actionCode what happened to the resource, the valid values are
     *        BaseDeployEvent.DEPLOY, BaseDeployEvent.REDEPLOY,
     *        BaseDeployEvent.UNDEPLOY, BaseDeployEvent.ENABLE or
     *        BaseDeployEvent.DISABLE
     * @throws IllegalArgumentException if resourceType or actionCode is invalid
     */
    public ResourceDeployEvent(String instance, String resourceName,
            String resourceType, String actionCode) {
        super(eventType, instance, BaseDeployEvent.RESOURCE, resourceName,
                actionCode);
        if(resourceType!=null)
            setResourceType(resourceType);
        if (DEPLOY.equals(actionCode)) {
            resourceExists = false;
        }
    }

    private ResourceDeployEvent(String type, Object source, 
                                 long seqNumber,long time) {
        super(type, source, seqNumber, time);
        setResourceType(type);
        this.j2eeComponentType = BaseDeployEvent.RESOURCE;

        // WARNING: actionName & j2eeComponentName is not set
    }
    
    /**
     * Get name of the resource that was affected by deployment action.
     * @return name of the resource
     */
    public String getResourceName() {
        return getJ2EEComponentName();
    }

    /**
     * Get resource type - one of RES_TYPE_CUSTOM, RES_TYPE_EXTERNAL_JNDI,
     * RES_TYPE_JDBC, RES_TYPE_MAIL, RES_TYPE_JMS, RES_TYPE_PMF, RES_TYPE_JCP.
     * @return resource type
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Helper method to validate and set resource type.
     */
    public void setResourceType(String resType) {
        boolean valid = false;
        if (RES_TYPE_CUSTOM.equals(resType)
                || RES_TYPE_EXTERNAL_JNDI.equals(resType)
                || RES_TYPE_JDBC.equals(resType)
                || RES_TYPE_MAIL.equals(resType)
                || RES_TYPE_JMS.equals(resType)
                || RES_TYPE_PMF.equals(resType)
                || RES_TYPE_JCP.equals(resType)
                || RES_TYPE_AOR.equals(resType)
                || RES_TYPE_CCP.equals(resType)
                || RES_TYPE_CR.equals(resType)
                || RES_TYPE_RAC.equals(resType)) {
            valid = true;
        }
        if (!valid) {
			String msg = localStrings.getString( "admin.event.invalid_resource_type", resType );
            throw new IllegalArgumentException( msg );
        }
        this.resourceType = resType;
    }

/*
Transition for config change processing. A single processing may include
n Add and m Delete and 0 or 1 Update. n and m should differ by at most 1.

Create -> creation of event
Add, Upd, Del -> Type of config change

For any transitions not allowed by this state diagram, the setActionForXXX
methods throw IllegalStateException

INITIAL STATE: RESOURCE DOES NOT EXIST

                               +---+
                             +-|Upd|--+
                             | +---+  |
+------------+             +-+--------V-+          +-----------+
|exist=false,|  +------+   |exist=true, |  +---+   |exist=false|
|action=null,+--|Create|-->+action=depl,+--|Del|-->+action=depl|
| noop=null  |  +------+   |noop=false  |  +---+   |noop=true  |
+------------+   [Add]     +-----+------+          +-----+-----+
                                 |         +---+         |
                                 +--<------|Add|---------+
                                           +---+
INITIAL STATE: RESOURCE EXISTS

                               +------------+
                               |exist=true  |  +---+
                           +-->|action=enbl +--|Del|--->---+
                           |   |noop=false  |  +---+       |
                           |   +------------+              |
                           |                               |
                           |                               |
+------------+             |   +------------+              |
|exist=true  |  +------+   |   |exist=true  |  +---+       |
|action=null +--|Create|---+-->|action=dsbl +--|Del|--->---+
|noop=null   |  +------+   |   |noop=false  |  +---+       |
+-----+------+   [Upd]     |   +------------+              |
      |                    |                               |
      |                    |                               |
      |                    |   +------------+              |
      |                    |   |exist=true  |  +---+       |
      |                    +-->|action=rdpl +--|Del|--->---+
      |                        |noop=false  |  +---+       |
      |                        +-+--------+-+<--------+    |
      |                          | +---+  |           |    |
      |                          +-|Upd|->+           |    |
      |                            +---+              |    |
      |                                               |    |
      |                        +------------+         |    |
      |         +------+       |exist=false |  +---+  |    |
      +---------|Create|------>|action=udpl +--|Add|--+    |
                +------+       |noop=false  |  +---+       |
                 [Del]         +-----+------+              |
                                     |                     |
                                     +------<--------------+


*/
    /**
     * Set appropriate action code for a ConfigChange of type Add. This method
     * is called while processing config changes after the initial creation of
     * the event.
     * @throws IllegalStateException if Add operation is invalid in current
     *     context.
     */
    private void setActionForAdd() {
        if (resourceExists) {
            String currentAction = getAction();
            if (UNDEPLOY.equals(currentAction)) {
                setAction(REDEPLOY);
            } else {
                // throw new IllegalStateException("Existing resource with "
                //         + "action " + currentAction + ". Can not Add!");
            }
        } else {
            if (noOp) {
                setAction(DEPLOY);
                noOp = false;
            } else {
                // Add can come again for properties associated to the resource
                // So do not throw exception
                // throw new IllegalStateException("Can not add new resource "
                //        + "again!");
            }
        }
    }

    /**
     *
     * @throws IllegalStateException if Update operation is invalid in
     *     current context.
     */
    private void setActionForUpdate() {
        // FIX
        // Do Nothing??
        // String currentAction = getAction();
        // if (ENABLE.equals(currentAction) || DISABLE.equals(currentAction)) {
        //     throw new IllegalStateException("A resource can have only "
        //             + "one Config Change entry of type update!");
        // }
    }

    /**
     *
     * @throws IllegalStateException if Delete operation is invalid in
     *     current context.
     */
    private void setActionForDelete() {
        if (resourceExists) {
            String currentAction = getAction();
            if (UNDEPLOY.equals(currentAction)) {
                // throw new IllegalStateException("Resource already removed."
                //         + "Can not remove again!");
            } else {
                setAction(UNDEPLOY);
            }
        } else {
            if (noOp) {
                // throw new IllegalStateException("New resource already  "
                //         + "removed. Can not remove again!");
            } else {
                noOp = true;
            }
        }
    }

    /**
     * Set action for specified action. If action is unknown then the method
     * throws IllegalArgumentException. If the new action can not be applied in
     * current state, the method throws IllegalStateException.
     */
    void setNewAction(String newAction) {
        if (DEPLOY.equals(newAction)) {
            setActionForAdd();
        } else if (REDEPLOY.equals(newAction)) {
            setActionForUpdate();
        } else if (UNDEPLOY.equals(newAction)) {
            setActionForDelete();
        } else {
			String msg = localStrings.getString( "admin.event.illegal_new_action", newAction );
            throw new IllegalArgumentException( msg );
        }
    }

    /**
     * Is this event a no-op. An event can be no-op if a resource is created
     * and then removed without reconfiguring the instance.
     */
    boolean isNoOp() {
        return noOp;
    }

    public String toString() {
        return "ResourceDeployEvent -- " + getAction() + " " + resourceType + "/" + getJ2EEComponentName();
    }

    public Object clone() throws CloneNotSupportedException {
        ResourceDeployEvent re = (ResourceDeployEvent) super.clone();
        return re;
    }
}
