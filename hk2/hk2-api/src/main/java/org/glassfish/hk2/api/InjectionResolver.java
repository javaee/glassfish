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

import org.jvnet.hk2.annotations.Contract;

/**
 * This class allows users to provide a custom injection target for
 * any annotation (including &#64;Inject).  The user would usually
 * only provide a resolver for &#64;Inject if it were specializing
 * the system provided resolver for &#64;Inject.  Otherwise, this
 * resolver can be used to provide injection points for any annotation.
 * <p>
 * An implementation of Context must be in the Singleton scope
 * 
 * @author jwells
 * @param <T> This must be the class of the injection annotation that this resolver
 * will handle
 */
@Contract
public interface InjectionResolver<T> {
    /** This is the name of the system provided resolver for 330 injections */
    public final static String SYSTEM_RESOLVER_NAME = "SystemInjectResolver";
    
    /**
     * This method will return the object that should be injected into the given
     * injection point.  It is the responsiblity of the implementation to ensure that
     * the object returned can be safely injected into the injection point.
     * <p>
     * This method should not do the injection themselves
     * 
     * @param injectee The injection point this value is being injected into
     * @param root The service handle of the root class being created, which should
     * be used in order to ensure proper destruction of associated &64;PerLookup
     * scoped objects.  This can be null in the case that this is being used
     * for an object not managed by HK2.  This will only happen if this
     * object is being created with the create method of ServiceLocator.
     * @return A possibly null value to be injected into the given injection point
     */
    public Object resolve(Injectee injectee, ServiceHandle<?> root);
    
    /**
     * This method should return true if the annotation that indicates that this is
     * an injection point can appear in the parameter list of a constructor.
     * 
     * @return true if the injection annotation can appear in the parameter list of
     * a constructor
     */
    public boolean isConstructorParameterIndicator();
    
    /**
     * This method should return true if the annotation that indicates that this is
     * an injection point can appear in the parameter list of a method.
     * 
     * @return true if the injection annotation can appear in the parameter list of
     * a method
     */
    public boolean isMethodParameterIndicator();

}
