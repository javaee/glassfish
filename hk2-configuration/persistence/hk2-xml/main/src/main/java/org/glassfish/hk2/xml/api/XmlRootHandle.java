/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

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
     * This method returns a read-only copy of the existing
     * tree.  Any method that would change anything in this
     * tree will fail.  Methods that are customized will also
     * fail
     * <p>
     * If representsDefaults is true then getters for unset
     * fields will return the default value.  If representsDefaults
     * is false then getters for unset fields will return null
     * (or 0 (and false) for scalars).  Setting representsDefault
     * to false is useful if this tree is to be used to marshall
     * back to XML, since JAXB will then not write the values back
     * out to the file
     * 
     * @param representDefaults If true getters will return default values,
     * if false getters will return null (or zero/false for scalars)
     * @return A read-only copy of this xml tree or null if there is
     * no current root for this handle
     */
    public T getReadOnlyRoot(boolean representDefaults);
    
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
     * combination of {@link XmlService#createBean(Class)}
     * and {@link #addRoot(Object)}.  This method
     * will throw an exception from the validator
     * (if validation is enabled) if the root type
     * has required fields or fails other validation
     */
    public void addRoot();
    
    /**
     * If this handle has a root this method
     * will delete it and all children, leaving
     * the root of this tree null
     * 
     * @return The root that was deleted, or null
     * if the root was already null
     */
    public T removeRoot();
    
    /**
     * Adds a change listener to be invoked before any property is
     * set or a bean is added.  The listener must be suitable for
     * storage in a HashSet.
     * <p>
     * Rules for change listener:
     * <UL>
     * <LI>Adds of beans with children will get called back for each child and sub-child added</LI>
     * <LI>Add listener callbacks will happen parent first but children at the same level are handled in any order</LI>
     * <LI>When adding a bean the following events will be generated:<OL>
     *   <LI>An event with an empty string as the propertyName, oldValue null, newValue and source set to the added bean</LI>
     *   <LI>An event for the parent being added to with propertyName, oldValue the old list or array or direct child, new value the new list or array
     *       or direct child</LI>
     *   </OL>
     * <LI>Remove listener callbacks will happen child first but children at the same level are handled in any order</LI>
     * <LI>When removing a bean the following events will be generated:<OL>
     *   <LI>An event with an empty string as the propertyName, oldValue the removed bean, newValue null, and source set to the removed bean</LI>
     *   <LI>An event for the parent being removed from with propertyName, oldValue the old list or array or direct child, new value the new list or array
     *       or null for the direct child</LI>
     *   </OL>
     * <LI>Listeners are run in the order in which they are added</LI>
     * <LI>If a listener throws a PropertyVetoException subsequent listeners are NOT called and the update will not happen</LI>
     * <LI>If a listener throws any exception other than PropertyVertoException subsequent listeners WILL be called and the update will not happen</LI>
     * <LI>If any exceptions happen in any listeners they will end up all being reported in a MultiException</LI>
     * </UL>
     * 
     * @param listeners non-null listeners to be called whenever
     * a property is changed in any bean in this root.  Must be
     * suitable for storage in a HashSet
     */
    public void addChangeListener(VetoableChangeListener... listeners);
    
    /**
     * Removes a change listener.  The listener must be suitable for
     * lookup in a HashSet
     * 
     * @param listener non-null listeners to be removed. Must be
     * suitable for lookup in a HashSet
     */
    public void removeChangeListener(VetoableChangeListener... listeners);
    
    /**
     * Gets the current list of change listeners
     * 
     * @return The non-null but possibly empty list of change
     * listeners to be called when a bean is added, removed
     * or modified
     */
    public List<VetoableChangeListener> getChangeListeners();
    
    /**
     * This method will lock the bean tree represented by
     * this XmlRootHandle and start a transaction.  Any changes
     * made while this transaction is in-flight will not be
     * visible to any other thread until this transaction is
     * committed.  This transaction MUST be committed or abandoned
     * before any other thread can get access to any beans in
     * this tree.
     * <p>
     * In particular, the code using this transaction should logically
     * behave like the following code:
     * <pre>
     * XmlHandleTransaction<Foo> transaction = root.lockForTransaction();
     * try {
     *   // Do many many edits all at once
     * }
     * finally {
     *   transaction.commit();
     * }
     * </code>
     * 
     * @return The never null transaction object that must either be abandoned
     * or committed before the write lock on this bean tree is released
     * @throws IllegalStateException if this bean tree is not allowed to have
     * a transaction started on it
     */
    public XmlHandleTransaction<T> lockForTransaction() throws IllegalStateException;
    
    /**
     * Does javax validation on the root bean from the root.  This will
     * cause every change hereafter to be validated
     * 
     * @throws ConstraintViolationException
     */
    public void startValidating();
    
    /**
     * Stops this root handle from doing javax validation
     * on modifications
     */
    public void stopValidating();
    
    /**
     * True if this handle is currently validating
     * changes
     */
    public boolean isValidating();
    
    /**
     * Will marshal this tree into the given stream.  Will hold the read
     * lock of this tree while it does so that the tree cannot change
     * underneath while it is being written out.  It will use a basic
     * indentation and new-line scheme
     * 
     * @param outputStream A non-closed output stream.  This method will
     * not close the output stream
     * @throws IOException On any exception that might happen
     */
    public void marshal(OutputStream outputStream) throws IOException;
}
