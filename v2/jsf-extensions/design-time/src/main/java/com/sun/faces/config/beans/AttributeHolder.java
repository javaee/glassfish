/*
 * $Id: AttributeHolder.java,v 1.1 2005/09/20 21:11:24 edburns Exp $
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
 * of attributes.</p>
 */

public interface AttributeHolder {


    // ----------------------------------------------------------------- Methods


    /**
     * <p>Add the specified attribute descriptor, replacing any existing
     * descriptor for this attribute name.</p>
     *
     * @param descriptor Descriptor to be added
     */
    public void addAttribute(AttributeBean descriptor);


    /**
     * <p>Return the attribute descriptor for the specified attribute name,
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param name Name of the attribute for which to retrieve a descriptor
     */
    public AttributeBean getAttribute(String name);


    /**
     * <p>Return the descriptors of all attributes for which descriptors have
     * been registered, or an empty array if none have been registered.</p>
     */
    public AttributeBean[] getAttributes();


    /**
     * <p>Deregister the specified attribute descriptor, if it is registered.
     * </p>
     *
     * @param descriptor Descriptor to be removed
     */
    public void removeAttribute(AttributeBean descriptor);


}
