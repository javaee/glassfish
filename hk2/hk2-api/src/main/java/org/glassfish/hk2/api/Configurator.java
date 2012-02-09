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

/**
 * An instance of this class is given to the {@link Module} configure
 * method and can be used to establish progromatic bindings of services
 * and contracts and other extensions to the hk2 environment.  The methods
 * on this class should not be used outside of the {@link Module} configure
 * method
 * 
 * @author jwells
 */
public interface Configurator {
	/**
	 * This method will bind the given Provider to the given key
	 * in the database.  The Descriptor in the
	 * BoundEntry will not directly use the passed in Descriptor,
	 * and will have the id field filled in.  If this provider
	 * is not an instanceof ExtendedProvider then this provider
	 * will be wrapped in an ExtendedProvider and put into the
	 * BoundEntry.
	 * 
	 * @param keys May not be null.  Will be used to derive the various
	 * key fields associated with the given provider.
	 * @param provider A user supplied provider of the service.
	 * @return The entry as added to the service registry, with fields
	 * of the Descriptor filled in by the system as appropriate
	 * @throws IllegalArgumentException if there is an error in the key or
	 * the provider
	 *
	 * JRW after thinking about this, allowing the user to have his own provider
	 * here may be a new feature.  I am removing this API until there is
	 * a call for it.  On the other hand, it is a nice way to do the "constant"
	 * provider.  In other words, you can use this to bind to a specific class
	 *
	public <T> BoundEntry<T> bind(Descriptor keys, Provider<T> provider);
	 */
	
	/**
	 * This method will bind the given descriptor to this Module
	 * 
	 * @param keys May not be null.  Will be used to derive the various
	 * key fields associated with the given provider
	 * @return The entry as added to the service registry, with fields
	 * of the Descriptor filled in by the system as appropriate
	 * @throws IllegalArgumentException if there is an error in the key or
	 * the provider
	 */
	public Descriptor bind(Descriptor key);
	
	/**
	 * This method removes a given descriptor from the registry.
	 * 
	 * @param key A description of the descriptor to remove
	 * @return a list of entries removed.  Will never return null,
	 * but may return an empty list if no entries matched the
	 * filter
	 */
	public List<Descriptor> unbind(Filter<Descriptor> key);
}
