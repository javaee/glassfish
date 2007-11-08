/*
 * $Id: PropertyHolder.java,v 1.1 2005/09/20 21:11:28 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.beans;


/**
 * <p>Interface denoting a configuration bean that stores a named collection
 * of properties.</p>
 */

public interface PropertyHolder {


    // ----------------------------------------------------------------- Methods


    /**
     * <p>Add the specified property descriptor, replacing any existing
     * descriptor for this property name.</p>
     *
     * @param descriptor Descriptor to be added
     */
    public void addProperty(PropertyBean descriptor);


    /**
     * <p>Return the property descriptor for the specified property name,
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param name Name of the property for which to retrieve a descriptor
     */
    public PropertyBean getProperty(String name);


    /**
     * <p>Return the descriptors of all properties for which descriptors have
     * been registered, or an empty array if none have been registered.</p>
     */
    public PropertyBean[] getProperties();


    /**
     * <p>Deregister the specified property descriptor, if it is registered.
     * </p>
     *
     * @param descriptor Descriptor to be removed
     */
    public void removeProperty(PropertyBean descriptor);


}
