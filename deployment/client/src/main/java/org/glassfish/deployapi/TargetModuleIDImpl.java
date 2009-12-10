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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.deployapi;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 * Implements the {@link TargetModuleID} interface from JSR-88, representing the
 * presence of a given module on a given {@link Target}.
 * <p>
 * This implementation is independent of the {@link TargetOwner} that owns the
 * corresponding Target.
 * 
 * @author tjquinn
 */
public class TargetModuleIDImpl implements TargetModuleID {

    private TargetImpl target;
    private String moduleID;
    private TargetModuleIDImpl parent;
    private TargetModuleIDImpl[] children = new TargetModuleIDImpl[0];
    private ModuleType moduleType;
    
    /**
     * Creates a new implementation object of TargetModuleID.
     * <p>
     * Normally this constructor should be used only by implementations of
     * TargetOwner.  Other code will normally retrieve TargetModuleID objects
     * from other methods that create them as part of their work (such as 
     * deployment, for example).
     * @param target the target on which the module resides
     * @param moduleID the name of the module
     * @param parent the higher-level TargetModuleIDImpl (if this object represents
     * a submodule of a module that is deployed to a Target)
     * @param children TargetModuleIDImpl objects representing the submodules 
     * of this module as deployed to the Target
     */
    public TargetModuleIDImpl(
            TargetImpl target, 
            String moduleID, 
            TargetModuleIDImpl parent, 
            TargetModuleIDImpl[] children) {
        this.target = target;
        this.moduleID = moduleID;
        this.parent = parent;
        this.children = children;
    }
    
    /**
     * Creates a new implementation object of TargetModuleId with no parent
     * and no children.
     * @param target the target on which the module resides
     * @param moduleID the name of the module
     */
    public TargetModuleIDImpl(TargetImpl target, String moduleID) {
        this(target, moduleID, null, new TargetModuleIDImpl[0]);
    }
    /**
     * Returns the Target on which the module is deployed.
     * @return the Target
     */
    public Target getTarget() {
        return target;
    }

    public TargetImpl getTargetImpl() {
        return target;
    }
    
    /**
     * Returns the name of the module that is deployed to a given Target.
     * @return the module name
     */
    public String getModuleID() {
        return moduleID;
    }

    /**
     * Returns the URL for running the Web module, if this TargetModuleID 
     * represents a Web module or submodule on a Target.
     * @return the URL
     */
    public String getWebURL() {
        return target.getOwner().getWebURL(this);
    }

    /**
     * Sets the URL for running the Web module, if this TargetModuleID
     * represents a Web module or submodule on a Target.
     * @param the webURL
     */
    public void setWebURL(String webURL) {
        target.getOwner().setWebURL(this, webURL);
    }


    /**
     * Returns the TargetModuleID for the containing module on the Target, if
     * this TargetModuleID represents a submodule.
     * @return the parent TargetModuleID
     */
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }

    /**
     * Returns the TargetModuleIDs representing submodules of this module 
     * deployed to the Target.
     * @return the child TargetModuleID objects
     */
    public TargetModuleID[] getChildTargetModuleID() {
        return children;
    }

    /**
     * Add a child TargetModuleID to this TargetModuleID
     */
    public void addChildTargetModuleID(TargetModuleIDImpl child) {
        TargetModuleIDImpl[] newChildren = 
            new TargetModuleIDImpl[children.length+1];

        System.arraycopy(children, 0, newChildren, 0, children.length);

        newChildren[children.length] = child;

        children = newChildren;

        child.setParentTargetModuleID(this);
    }

    /**
     * Sets the parent TargetModuleID
     */
    public void setParentTargetModuleID(TargetModuleIDImpl parent) {
        this.parent = parent;
    }

    /**
     * Sets the module type for this deployed module
     * @param the module type
     */
    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * @return the module type of this deployed module
     */
    public ModuleType getModuleType() {
        return moduleType;
    }

}
