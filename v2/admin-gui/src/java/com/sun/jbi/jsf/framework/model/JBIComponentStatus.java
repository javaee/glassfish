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


/*
 * JBIComponentStatus.java
 * 
 * @author ylee
 * @author Graj
 *
 */

package com.sun.jbi.jsf.framework.model;
import java.io.Serializable;
import java.util.logging.Logger;


public class JBIComponentStatus implements Serializable {

    /** Deployment Type  */
    public static final String DEPLOYMENT_TYPE = "service-assembly";
//    public static final String DEPLOYMENT_TYPE = "Deployment";
    /** unknown type */
    public static final String UNKNOWN_TYPE = "unknown";
    /** Binding type  */
    public static final String BINDING_TYPE = "binding-component";
//    public static final String BINDING_TYPE = "Binding";
    /** Engine Type */
    public static final String ENGINE_TYPE = "service-engine";
//    public static final String ENGINE_TYPE = "Engine";
    /** Namespace Type  */
    public static final String NAMESPACE_TYPE = "shared-library";
//    public static final String NAMESPACE_TYPE = "SharedLibrary";

    /** state  Loaded state.  */
    public static final String UNKNOWN_STATE = "Unknown";
    /** state loaded */
    public static final String LOADED_STATE = "Loaded";
    /** Installed state */
//    public static final String INSTALLED_STATE = "Installed";
    public static final String INSTALLED_STATE = "Shutdown";
    /** Stopped state  */
    public static final String STOPPED_STATE = "Stopped";
    /** Started state */
    public static final String STARTED_STATE = "Started";

    protected String componentId;
    protected String state;
    protected String name;
    protected String description;
    protected String type;
    
    private Logger logger = Logger.getLogger(JBIComponentStatus.class.getName());

    /**
     *
     */
    public JBIComponentStatus() {
    }


    /**
     * @param componentId
     * @param state
     * @param name
     * @param description
     * @param type
     */
    public JBIComponentStatus(String componentId, String name, String description, String type, String state) {
        this.componentId = componentId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.state = state;
    }
    
    
    /**
     * @return Returns the componentId.
     */
    public String getComponentId() {
        return this.componentId;
    }
    
    /**
     * @param componentId The componentId to set.
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        if((description != null) && (description.length() > 0)) {
            this.description = description;
        }
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the state.
     */
    public String getState() {
        return this.state;
    }
    
    /**
     * @param state The state to set.
     */
    public void setState(String status) {
        this.state = status;
    }


    /**
     * @return Returns the type.
     */
    public String getType() {
        return this.type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    public void dump() {
        logger.info("/////////////////////////////////////////////////");
        logger.info("//  -- JBI Component --                        //");
        logger.info("/////////////////////////////////////////////////");
        //logger.info("//  componentId is: "+ this.componentId);
        logger.info("//  name is: "+ this.name);
        logger.info("//  description is: "+ this.description);
        logger.info("//  type is: "+ this.type);
        logger.info("//  state is: "+ this.state);
        logger.info("/////////////////////////////////////////////////");
    }

    public static void main(String[] args) {
    }
}

