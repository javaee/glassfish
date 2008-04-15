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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.resource;

import java.util.Iterator;
import java.util.Properties;
import javax.management.AttributeList;
import javax.management.Attribute;

import static com.sun.enterprise.resource.ResourceConstants.*;

/**
 * Class which represents the Resource.
 */
public class Resource
{
    protected static final String CUSTOM_RESOURCE          = "custom-resource";
    protected static final String JDBC_CONNECTION_POOL     = "jdbc-connection-pool";
    protected static final String CONNECTOR_RESOURCE       = "connector-resource";
    protected static final String ADMIN_OBJECT_RESOURCE    = "admin-object-resource";
    protected static final String JDBC_RESOURCE            = "jdbc-resource";
    protected static final String RESOURCE_ADAPTER_CONFIG  = "resource-adapter-config";
    protected static final String MAIL_RESOURCE            = "mail-resource";
    protected static final String EXTERNAL_JNDI_RESOURCE   = "external-jndi-resource";
    protected static final String CONNECTOR_CONNECTION_POOL = "connector-connection-pool";
    protected static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "persistence-manager-factory-resource";
    protected static final String CONNECTOR_SECURITY_MAP    = "security-map";
    
    private String resType;
    private AttributeList attrList = new AttributeList();
    private Properties props = new Properties();
    private String sDescription = null;

    public Resource()
    {
    }
    
    public Resource(String type)
    {
       resType = type;
    }
    
    public String getType()
    {
        return resType;
    }
    
    public void setType(String type)
    {
        resType = type;
    }
    
    public AttributeList getAttributes()
    {
        return attrList;
    }
    
    public void setAttribute(String name, String value)
    {
        attrList.add(new Attribute(name, value));
    }
    
    public void setAttribute(String name, String[] value)
    {
        attrList.add(new Attribute(name, value));
    }

    public void setDescription(String sDescription)
    {
        this.sDescription = sDescription;
    }
    
    public String getDescription()
    {
       return sDescription;
    }
    
    public void setProperty(String name, String value)
    {
        props.setProperty(name, value);
    }

    public void setProperty(String name, String value, String desc)
    {
        // TO DO: 
    }

    public Properties getProperties()
    {
        return props;
    }
    
    //Used to figure out duplicates in a List<Resource>
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ( !(obj instanceof Resource) ) return false;
        
        Resource r = (Resource)obj;
        return r.getType().equals(this.getType()) && 
                r.getDescription().equals(this.getDescription()) &&
                r.getProperties().equals(this.getProperties()) &&
                r.getAttributes().equals(this.getAttributes());
    }
    
    //when a class overrides equals, override hashCode as well. 
    @Override
    public int hashCode() {
        return this.getAttributes().hashCode() + 
        this.getProperties().hashCode() + 
        this.getType().hashCode() + 
        this.getDescription().hashCode();
    }
    
    //Used to figure out conflicts in a List<Resource>
    //A Resource is said to be in conflict with another Resource if the two 
    //Resources have the same Identity [attributes that uniquely identify a Resource]
    //but different properties
    public boolean isAConflict(Resource r) {
        //If the two resources are equal [duplicates], then there is no 
        //conflict
        if (r.equals(this)) return true;
        
        //If the two resource have the same identity 
        if (hasSameIdentity(r)) {
            //and the properties or attributes of the two resources 
            //are different, then we have a conflict
            boolean propsNotEqual = (!(this.getProperties().equals(
                                                    r.getProperties())));
            boolean attrsNotEqual = (!(this.getAttributes().equals(
                                                    r.getAttributes())));
            if (propsNotEqual || attrsNotEqual) return true;
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
        
        //For all resources, their identity is their "attributes" 
        if (rType.equals(CUSTOM_RESOURCE)|| rType.equals(EXTERNAL_JNDI_RESOURCE)
             || rType.equals(JDBC_RESOURCE)|| rType.equals(PERSISTENCE_MANAGER_FACTORY_RESOURCE)
             || rType.equals(CONNECTOR_RESOURCE)|| rType.equals(ADMIN_OBJECT_RESOURCE)) {
            return r.getAttributes().equals(this.getAttributes());
        }
        
        //For pools/mail resource and RA config, the identity is limited to 
        //a few attributes.
        if (rType.equals(JDBC_CONNECTION_POOL)) {
            return isEqualAttribute(r, CONNECTION_POOL_NAME) &&
            isEqualAttribute(r, DATASOURCE_CLASS) 
            && isEqualAttribute(r, RES_TYPE);
        }
        
        if (rType.equals(CONNECTOR_CONNECTION_POOL)) {
            return isEqualAttribute(r, CONNECTION_POOL_NAME) &&
            isEqualAttribute(r, RESOURCE_ADAPTER_CONFIG_NAME) 
            && isEqualAttribute(r, CONN_DEF_NAME);
        }
        
        if (rType.equals(MAIL_RESOURCE)) {
            return isEqualAttribute(r, JNDI_NAME);
        }
        
        if (rType.equals(RESOURCE_ADAPTER_CONFIG)) {
            return isEqualAttribute(r, RES_ADAPTER_NAME) && 
            isEqualAttribute(r, RES_ADAPTER_CONFIG);
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
        for (Iterator iter = r.getAttributes().iterator(); iter.hasNext();) {
            Attribute elt = (Attribute) iter.next();
            if (elt.getName().equals(name)) return elt;
        }
        return null;
    }
    
}
