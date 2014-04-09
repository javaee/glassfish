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
 * This service can be used to add validation points to Descriptors.
 * <p>
 * An implementation of ValidationService must be in the Singleton scope
 *
 * @author jwells
 *
 */
@Contract
public interface ValidationService {
    /**
     * This filter will be run at least once per descriptor at the point that the descriptor
     * is being looked up, either with the {@link ServiceLocator} API or due to
     * an &#64;Inject resolution.  The decision made by this filter will be cached and
     * used every time that Descriptor is subsequently looked up.  No validation checks
     * should be done in the returned filter, it is purely meant to limit the
     * {@link Descriptor}s that are passed into the validator.
     * <p>
     * Descriptors passed to this filter may or may not be reified.  The filter should try as
     * much as possible to do its work without reifying the descriptor.  
     * <p>
     * The filter may be run more than once on a descriptor if some condition caused
     * the cache of results per descriptor to become invalidated.
     * 
     * @return The filter to be used to determine if the validators associated with this
     * service should be called when the passed in {@link Descriptor} is looked up
     */
    public Filter getLookupFilter();
    
    /**
     * Returns the {@link Validator} that will be run whenever
     * a {@link Descriptor} that passed the filter is to be looked up with the API
     * or injected into an injection point, or on any bind or unbind operation.
     * If this method returns false then the operation will not proceed.
     * 
     * @return A non-null validator
     */
    public Validator getValidator();
}
