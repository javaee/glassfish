/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2;

import org.jvnet.hk2.annotations.Contract;

/**
 * A module defines the binding for a {@link Services} instance. The instance will
 * be registered under the name provided by the implementations of this contract
 * by using the annotation value as described in {@link org.jvnet.hk2.annotations.Service#name}
 *
 * <p/>
 * Each module is isolated from each other, so modules that need to access services
 * offered by other modules must inject these {@link Module} instances in order
 * to lookup their services. Using injection ensures that modules dependencies are
 * managed.
 *
 * </p>
 * Each {@link Module} must implement the {@link Module#configure(BinderFactory)} method
 * to configure its services.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 */
@Contract
public interface Module {

    /**
     * Main configuration hook for modules. Modules should use the {@link BinderFactory} methods
     * like {@link BinderFactory#bind(String)} or {@link BinderFactory#bind(Class, Class[])} to
     * add services through the programmatic DSL.
     *
     * @param binderFactory factory for adding services to the {@link Services} instance
     */
    void configure(BinderFactory binderFactory);
    
    // TODO: nice-to-have a callback when a service, any service, in this module has been activated -- jtrent
    // And if activation fails, the services from it should be unavailable.
//    void onActivated();
    
}
