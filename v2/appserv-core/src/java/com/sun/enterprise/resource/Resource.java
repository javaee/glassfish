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

package com.sun.enterprise.resource;

import java.util.Iterator;
import java.util.Properties;
import javax.management.AttributeList;
import javax.management.Attribute;

import com.sun.enterprise.config.serverbeans.ServerTags;

import static com.sun.enterprise.resource.ResourceConstants.*;

/**
 * Class which represents the Resource.
 */
public class Resource {
    protected static final String CUSTOM_RESOURCE          = ServerTags.CUSTOM_RESOURCE;
    protected static final String JDBC_CONNECTION_POOL     = ServerTags.JDBC_CONNECTION_POOL;
    protected static final String CONNECTOR_RESOURCE       = ServerTags.CONNECTOR_RESOURCE;
    protected static final String ADMIN_OBJECT_RESOURCE    = ServerTags.ADMIN_OBJECT_RESOURCE;
    protected static final String JDBC_RESOURCE            = ServerTags.JDBC_RESOURCE;
    protected static final String RESOURCE_ADAPTER_CONFIG  = ServerTags.RESOURCE_ADAPTER_CONFIG;
    protected static final String MAIL_RESOURCE            = ServerTags.MAIL_RESOURCE;
    protected static final String EXTERNAL_JNDI_RESOURCE   = ServerTags.EXTERNAL_JNDI_RESOURCE;
    protected static final String CONNECTOR_CONNECTION_POOL = ServerTags.CONNECTOR_CONNECTION_POOL;
    protected static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
    protected static final String CONNECTOR_SECURITY_MAP    = ServerTags.SECURITY_MAP;
    
    private String resType;
    private AttributeList attrList = new AttributeList();
    private Properties props = new Properties();
    private String sDescription = null;

    public Resource(String type) {
        resType = type;
    }

    public String getType() {
        return resType;
    }

//Commented from 9.1 as it is not used
/*
    public void setType(String type) {
        resType = type;
    }
*/

    public AttributeList getAttributes() {
        return attrList;
    }

    public void setAttribute(String name, String value) {
        attrList.add(new Attribute(name, value));
    }

    public void setAttribute(String name, String[] value) {
        attrList.add(new Attribute(name, value));
    }

    public void setDescription(String sDescription) {
        this.sDescription = sDescription;
    }

    public String getDescription() {
        return sDescription;
    }

    public void setProperty(String name, String value) {
        props.setProperty(name, value);
    }

//Commented from 9.1 as it is not used
 /*   public void setProperty(String name, String value, String desc) {
        // TO DO: 
    }*/

    public Properties getProperties() {
        return props;
    }
    
    //Used to figure out duplicates in a List<Resource>
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ( !(obj instanceof Resource) ) return false;
        Resource r = (Resource)obj;
        return r.getType().equals(this.getType()) && 
                //No need to compare description for equality
                //r.getDescription().equals(this.getDescription()) &&
                r.getProperties().equals(this.getProperties()) &&
                r.getAttributes().equals(this.getAttributes());
    }
    
    //when a class overrides equals, override hashCode as well. 
    @Override
    public int hashCode() {
        return this.getAttributes().hashCode() + 
        this.getProperties().hashCode() + 
        this.getType().hashCode();
        //description is not used to generate hashcode
        //this.getDescription().hashCode();
    }
    
    //Used to figure out conflicts in a List<Resource>
    //A Resource is said to be in conflict with another Resource if the two 
    //Resources have the same Identity [attributes that uniquely identify a Resource]
    //but different properties
    public boolean isAConflict(Resource r) {
        //If the two resource have the same identity 
        if (hasSameIdentity(r)) {
            //If the two resources are not equal, then there is no 
            //conflict
            if (!r.equals(this)) 
                return true;
        }
        return false;
    }

    /**
     * Checks if the specified resource has the same identity as
     * this resource.
     */
    private boolean hasSameIdentity(Resource r) {
        //For two resources to have the same identity, atleast their types should match
        if (r.getType() != this.getType()) {
            return false;
        }
        String rType = r.getType();
        
        //For all resources, their identity is their jndi-name
        if (rType.equals(CUSTOM_RESOURCE)|| rType.equals(EXTERNAL_JNDI_RESOURCE)
             || rType.equals(JDBC_RESOURCE)|| rType.equals(PERSISTENCE_MANAGER_FACTORY_RESOURCE)
             || rType.equals(CONNECTOR_RESOURCE)|| rType.equals(ADMIN_OBJECT_RESOURCE) || rType.equals(MAIL_RESOURCE)) {
            return isEqualAttribute(r, JNDI_NAME);
        }
        
        //For pools the identity is limited to pool name
        if (rType.equals(JDBC_CONNECTION_POOL) || rType.equals(CONNECTOR_CONNECTION_POOL)) {
            return isEqualAttribute(r, CONNECTION_POOL_NAME);
        }
        
        if (rType.equals(RESOURCE_ADAPTER_CONFIG)) {
            return isEqualAttribute(r, RES_ADAPTER_CONFIG);
        }
        
        return false;
    }
    
    /**
     * Compares the attribute with the specified name
     * in this resource with the passed in resource and checks
     * if they are <code>equal</code>
     */
    private boolean isEqualAttribute(Resource r, String name) {
        return (getAttribute(r, name).equals(getAttribute(this, name)));
    }
    
    /**
     * Utility method to get an <code>Attribute</code> of the given name
     * in the specified resource
     */
    private Attribute getAttribute(Resource r, String name) {
        for (Iterator<Attribute> iter = r.getAttributes().iterator(); iter.hasNext();) {
            Attribute elt =  iter.next();
            if (elt.getName().equals(name)) return elt;
        }
        return null;
    }
    
    @Override
    public String toString(){
        
        String rType = getType();
        String identity = "";
        if (rType.equals(CUSTOM_RESOURCE)|| rType.equals(EXTERNAL_JNDI_RESOURCE)
             || rType.equals(JDBC_RESOURCE)|| rType.equals(PERSISTENCE_MANAGER_FACTORY_RESOURCE)
             || rType.equals(CONNECTOR_RESOURCE)|| rType.equals(ADMIN_OBJECT_RESOURCE) || rType.equals(MAIL_RESOURCE)) {
            identity =  (String) getAttribute(this, JNDI_NAME).getValue();
        }else if (rType.equals(JDBC_CONNECTION_POOL) || rType.equals(CONNECTOR_CONNECTION_POOL)) {
            identity =  (String) getAttribute(this, CONNECTION_POOL_NAME).getValue();
        }else if (rType.equals(RESOURCE_ADAPTER_CONFIG)) {
            identity =  (String) getAttribute(this, RES_ADAPTER_CONFIG).getValue();
        } 
        
        return identity + " of type " + resType;
    }
    
}
