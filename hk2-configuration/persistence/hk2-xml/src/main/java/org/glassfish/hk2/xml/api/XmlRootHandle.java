/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.xml.api;

import java.net.URI;

/**
 * This represents XML data and a JavaBean tree
 * 
 * @author jwells
 *
 */
public interface XmlRootHandle<T> {
    /**
     * Gets the root of the JavaBean tree
     * 
     * @return The root of the JavaBean tree.  Will
     * only return null if the tree has not yet
     * been created
     */
    public T getRoot();
    
    /**
     * Returns the root interface of this handle
     * 
     * @return The root class or interface of this
     * handle.  Will not return  null
     */
    public Class<T> getRootClass();
    
    /**
     * Represents the original URI from which this
     * tree was parsed (or null if this tree did not
     * come from a URI)
     * 
     * @return The original URI from which this tree
     * was parsed, or null if this tree did not come
     * from a URI
     */
    public URI getURI();
    
    /**
     * Returns true if this handles root and children
     * are advertised in it service locator
     * 
     * @return true if the root and children are
     * advertised in the associated service locator
     */
    public boolean isAdvertisedInLocator();
    
    /**
     * Returns true if this handles root and children
     * are advertised in the {@link org.glassfish.hk2.configuration.hub.api.Hub}
     * 
     * @return true if the root and children are
     * advertised in the associated
     * {@link org.glassfish.hk2.configuration.hub.api.Hub}
     */
    public boolean isAdvertisedInHub();
    
    /**
     * Creates a copy of this tree that is not advertised.
     * Modifications can be made to this copy and then
     * merged back into the parent in a single transaction
     * <p>
     * There is no requirement to call {@link XmlRootCopy#merge()}
     * since the parent keeps no track of children.  However,
     * the {@link XmlRootCopy#merge()} method will fail if
     * a modification has been made to the parent since the
     * time the copy was created 
     * 
     * @return A non-null copy of this root that can be modified
     * and then merged back in a single transaction
     */
    public XmlRootCopy<T> getXmlRootCopy();
    
    /**
     * This method overlays the current root and children with
     * the root and children from newRoot.  newRoot must
     * have the same rootClass and must NOT be advertised
     * in either the locator or the hub.  The system will
     * calculate the set of changes needed to convert this
     * root into the root from newRoot.
     * <p>
     * All nodes that are at the same spot in the tree (have the same
     * xpath and same instance name) will not be modified, but will
     * instead have attributes changed.  All nodes present in newRoot
     * but not in this root will be considered adds.  All nodes
     * not present in newRoot but in this root will be considered deletes
     * <p>
     * The URI will not be modified by this call, nor will the
     * state of advertisement
     * 
     * @param newRoot The non-null root that will be overlayed
     * onto this handle
     */
    public void overlay(XmlRootHandle<T> newRoot);
    
    /**
     * This creates an instance of the root bean
     * of the tree with no fields of the root filled
     * in.  Use this API if the root has required
     * fields that must be filled in prior to being
     * validated.  The instance created by this
     * API will NOT become the root of the tree
     * until it is added with {@link #addRoot(Object)}.
     * There is no requirement that the object
     * created with this API is ever set as the
     * root of this handle
     * 
     * @return An instance of the root bean with
     * no properties set
     */
    public T createRoot();
    
    /**
     * If this handle does not already have a
     * root bean this method will add the one
     * given
     * 
     * @param root The non-null instance of the
     * root type of this handle
     * @throws IllegalStateException if this handle
     * already has a root
     */
    public void addRoot(T root);
    
    /**
     * This method can be used if the root of the
     * tree has no required fields, and is the
     * combination of {@link #createRoot()}
     * and {@link #addRoot(Object)}.  This method
     * will throw an exception from the validator
     * (if validation is enabled) if the root type
     * has required fields or fails other validation
     */
    public void createAndAddRoot();
    
    /**
     * If this handle has a root this method
     * will delete it and all children, leaving
     * the root of this tree null
     * 
     * @return The root that was deleted, or null
     * if the root was already null
     */
    public T deleteRoot();
}
