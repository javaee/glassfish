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
	 * Returns all of the contracts that this
	 * service describes
	 *   
	 * @return Will never return null, but
	 * may return an empty set.  The returned
	 * strings are the fully qualified class
	 * names of contracts the predicate
	 * describes 
	 */
	public Set<String> getContracts();
	
	/**
	 * Returns all of the implementation
	 * classes that this object can
	 * be looked up or registered as
	 * <p>
	 * Note that at the current time an object
	 * in the registry may only have one concrete
	 * implementation, so the returned set must
	 * only have size zero or one
	 * 
	 * @return May not return null, but
	 * may return an empty set.  Returns
	 * the fully qualified class names of
	 * the object described
	 */
	public Set<String> getImplementations();
	
	/**
	 * Returns all of the scopes that this
	 * object should be registered with
	 * or looked up by
	 * <p>
	 * Note that at the current time an object
	 * in the registry may only have one scope,
	 * so the returned set must only have size
	 * zero or one
	 * 
	 * @return Never returns null but may return
	 * an empty set.  The set of fully qualified
	 * class names which implement Scope that
	 * this object should be registered with
	 * or looked up by
	 */
	public Set<String> getScopes();
	
	/**
	 * Returns all of the names that this
	 * objects should be registered with or
	 * looked up by
	 * <p>
	 * Note that at the current time an object
	 * in the registry may only have one name,
	 * so the returned set must only have size
	 * zero or one
	 * 
	 * @return Never returns null but may return
	 * an empty set.  The set of names that this
	 * object should be registered with or looked
	 * up by
	 */
	public Set<String> getNames();
	
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
}
