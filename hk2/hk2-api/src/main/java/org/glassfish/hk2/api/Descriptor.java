/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Descriptor is a bean-like structure that describes a service
 * declaration in HK2.
 * 
 * <p>
 * Services have a number of optional attributes such as name and scope.
 * A service is required to minimally have a type name representing the
 * concrete (i.e., byType) definition.
 * 
 * @author Jerome Dochez, Jeff Trent, Mason Taube
 */
public interface Descriptor {
    /**
     * Returns the fully qualified class
     * name of the implementation
     * class.  If this is a factory descriptor
     * then this will return the fully
     * qualified name of the class implementing
     * the factory interface.
     *
     * @return Might return null in some cases,
     * but will usually return the fully qualified
     * class name of the implementation class or
     * of the factory class for this descriptor
     */
    public String getImplementation();

	/**
	 * Returns the base class name of the contracts that
	 * this service describes.  If the contract is a
	 * paramterized type this will return the raw class.
	 * If this service can be looked up by its implementation
	 * class then the name of the implementation class
	 * must also be found in this list
	 *   
	 * @return Will never return null, but
	 * may return an empty set.  The returned
	 * strings are the fully qualified class
	 * names of contracts the predicate
	 * describes 
	 */
	public Set<String> getAdvertisedContracts();
	
	/**
	 * Returns the fully qualified class name of
	 * the scope annotation that should be
	 * associated with this descriptor.
	 * 
	 * @return If this returns null then this
	 * descriptor is assumed to be in the
	 * default scope, which is PerLookup
	 */
	public String getScope();
	
	/**
	 * The name of this descriptor.  Note that
	 * if this returns not null then there must
	 * also be the Named qualifier in the set of
	 * qualifiers with the same value
	 * <p>
	 * 
	 * @return The name of this descriptor, or null
	 * if there is no name associated with this qualifier
	 */
	public String getName();
	
	/**
	 * Returns all of the annotation classes
	 * that this object should be registered
	 * with or looked up by
	 * 
	 * @return Never returns null, but may return
	 * an empty set.  The set of fully
	 * qualified class names that are annotations
	 * that this object must have
	 */
	public Set<String> getQualifiers();
	
	/**
	 * Returns all of the metadata associated
	 * that this object should be registered
	 * with or looked up by
	 * 
	 * @return Never returns null, but may return
	 * an empty set.  The set of metadata
	 * associated with the object.  The values
	 * in the returned map will never be null, and
	 * will always have at least one member
	 */
	public Map<String, List<String>> getMetadata();
	
	/**
	 * Returns the ranking of this descriptor.  Rankings
	 * with higher value will be considered "better" than
	 * rankings of lower value.  Descriptors with the same
	 * ranking will be returned in the reverse ServiceID order
	 * (in other words, the least service ID is considered
	 * "better" than any higher service ID).
	 * 
	 * @return the ranking that should be associated with this
	 * descriptor
	 */
	public int getRanking();
	
	/**
	 * This returns the unique identifier for this descriptor.
	 * This field will be ignored upon binding, and will then
	 * be assigned by the system.  However, this field can be
	 * set on search operations, in which case this search will
	 * match exactly one Provider in the system.
	 * 
	 * @return The service id for this object.  Note that this
	 * field may return null if this is an initial bind, as it
	 * is not the responsibility of the binder to set this
	 * value.  If this returns non-null on a bind operation the
	 * return value will be ignored
	 */
	public Long getServiceId();
	
	/**
     * This returns the id of the ServiceLocator which this descriptor
     * is registered with.  Returns null if this descriptor
     * is not yet registered with a ServiceLocator
     * 
     * @return The id of the ServiceLocator this Descriptor is registered
     * with, or null if this Descriptor is not registered with a ServiceLocator
     */
    public Long getLocatorId();
}
