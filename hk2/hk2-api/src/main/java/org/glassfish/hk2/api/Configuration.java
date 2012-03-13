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

import java.lang.annotation.Annotation;
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
public interface Configuration {
	
	/**
	 * This method will bind the given descriptor to this Module
	 * 
	 * @param keys May not be null.  Will be used to derive the various
	 * key fields associated with the given provider
	 * @return The entry as added to the service registry, with fields
	 * of the Descriptor filled in by the system as appropriate
	 * @throws IllegalArgumentException if there is an error in the key
	 */
	public Descriptor bind(Descriptor key);
	
	/**
	 * Adds a context implementation to the system
	 * 
	 * @param context The context to add
	 */
	public void addContext(Context context);
	
	/**
	 * This adds a custom class loader to the system.  Custom class
	 * loaders will be searched in a random order, so users should not
	 * rely on the order of invocation of loaders.  The first loader to
	 * not return null shall be the one that is used, and no further loaders
	 * will be consulted.  There is a system loader that will be consulted
	 * last which uses the context class loader in order to load the classes.
	 * 
	 * @param loader The custom loader to consult when loading classes
	 */
	public void addLoader(HK2Loader loader);
	
	/**
	 * This will add an injection resolver to the system. The system will
	 * provide a default implementation that handles Inject.  However, if the
	 * user provides a resolver for Inject then that one will be used in
	 * preference to the default system implementation
	 * 
	 * @param indicator The annotation that indicates an injection point.  Must
	 * be valid for constructors, methods and fields
	 * @param resolver The resolver to use when finding instances
	 */
	public void addInjectionResolver(Class<? extends Annotation> indicator, InjectionResolver resolver);
}
