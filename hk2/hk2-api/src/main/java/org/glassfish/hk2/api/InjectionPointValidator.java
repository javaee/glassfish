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

/**
 * This method is called when it determined that a type that is
 * annotated with a Validating annotation is to be injected into
 * any other class.
 * 
 * @author jwells
 *
 */
public interface InjectionPointValidator {
    /**
     * This method is called whenever it has been determined that a validating
     * class is to be injected into an injection point, or when a descriptor
     * is being looked up explicitly with the API.
     * <p>
     * The candidate descriptor being passed in may not have yet been reified.  If
     * possible, this method should do its work without reifying the descriptor.
     * However, if it is necessary to reify the descriptor, it should be done with
     * the ServiceLocator.reifyDescriptor method.
     * <p>
     * 
     * @param injectee The injection point to validate.  If this is null
     * then this lookup is being done directly from the API, in which case the caller
     * of the API will be on the call stack
     * @param candidate The candidate descriptor that will be responsible for creating
     * the object to be put into the injection point (or returned to the API).  This
     * descriptor may not have been reified
     * @return true if this injection should succeed, false if this candidate should not
     * be returned
     * @throws RuntimeException Any exception from this method will also cause the candidate
     * to not be available.  However, the preferred method of indicating an validation failure
     * is to return null
     */
    public boolean validateInjectionPoint(Injectee injectee, ActiveDescriptor<?> candidate);

}
