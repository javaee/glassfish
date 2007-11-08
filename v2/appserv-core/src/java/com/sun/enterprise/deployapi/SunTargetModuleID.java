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

package com.sun.enterprise.deployapi;

import java.io.Serializable;
import java.util.Vector;
import java.util.Set;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;


/**
 *
 * @author Jerome Dochez
 */
public class SunTargetModuleID extends SunTarget implements TargetModuleID, Serializable {
    
    private String moduleID;
    private ModuleType moduleType;
    private boolean initialized=false;
    private SunTargetModuleID parent = null;
    private Vector children = null;
    private String webUrl=null;
    
    /** Creates a new instance of SunTargetModuleID */
    public SunTargetModuleID(String moduleID, SunTarget target) {
        super(target);
        this.moduleID = moduleID;
    }
    
    /** Retrieve a list of identifiers of the children
     * of this deployed module.
     *
     * @return a list of TargetModuleIDs identifying the
     *         childern of this object. A <code>null</code>
     *         value means this module has no childern
     */
    public TargetModuleID[] getChildTargetModuleID() {
        if (children==null)
            return null;
        
        TargetModuleID[] list = new TargetModuleID[children.size()];
        children.copyInto(list);
        return list;
    }
    
    /** Retrieve the id assigned to represent
     * the deployed module.
     */
    public String getModuleID() {
        return moduleID;
    }
    
    /** Retrieve the identifier of the parent
     * object of this deployed module. If there
     * is no parent then this is the root object
     * deployed.  The root could represent an EAR
     * file or it could be a stand alone module
     * that was deployed.
     *
     * @return the TargetModuleID of the parent
     *         of this object. A <code>null</code>
     *         value means this module is the root
     *         object deployed.
     */
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }
    
    /** Retrieve the name of the target server.
     * this module was deployed to.
     *
     * @return Target an object representing
     *         a server target.
     */
    public Target getTarget() {
        return this;
    }
    
    /** If this TargetModulID represents a web
     * module retrieve the URL for it.
     *
     * @return the URL of a web module or null
     *         if the module is not a web module.
     */
    public String getWebURL() {
        return webUrl;
    }
    
    /** 
     * set the URL of a web module if the module is a web module.
     */
    public void setWebURL(String webUrl) {
        this.webUrl = webUrl;
    }    
    
    /**
     * Add a child TargetModuleID to this TargetModuleID
     */
    public void addChildTargetModuleID(SunTargetModuleID child) {
        if (children==null) {
            children = new Vector();
        }
        child.setParentTargetModuleID(this);
        children.add(child);
    }
    
    /**
     * Sets the parent TargetModuleID
     */
    public void setParentTargetModuleID(SunTargetModuleID parent) {
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
    
    /**
     * @return a meaningful string for myself
     */
    public String toString() {
        return moduleID + "_" + super.toString(); 
    }

    
    /**
     * @return a meaningful string for myself
     */
    public String debugString() {
        String s = "TargetModuleID type " + getModuleType() +  " moduleID " + toString() + " on target = " + super.toString(); 
        if (ModuleType.WAR.equals(moduleType)) {
            s = s + " at " + getWebURL();
        } 
        return s;
    }
        
    /**
     * @return true if I am the equals to the other object
     */
    public boolean equals(Object other) {
        if (other instanceof SunTargetModuleID) {
            SunTargetModuleID theOther = (SunTargetModuleID) other;
            return (moduleID.equals(theOther.moduleID) && super.equals(theOther));
        }
        return false;
    }    
    
    /**
     * @return hashCode based on values used in equals
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + ((moduleID == null) ? 0 : moduleID.hashCode());
        return result;
    }
}
