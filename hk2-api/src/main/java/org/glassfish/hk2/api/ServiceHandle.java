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
 * This service handle can be used to get a specific instance
 * of a service, and can be used to destroy that service as well
 * 
 * @author jwells
 * @param <T> The type of the service that can be returned
 *
 */
public interface ServiceHandle<T> {
    /**
     * Gets the underlying service object
     * @return May return null (if the backing ActiveDescriptor returned null)
     * @throws MultiException if there was an error creating the service
     * @throws IllegalStateException if the handle was previously destroyed
     */
    public T getService();
    
    /**
     * Returns the ActiveDescriptor associated with this service handle
     * 
     * @return The ActiveDescriptor associated with this handle. Can return
     * null in cases where the Handle describes a service not associated with
     * an hk2 service, such as a constant service
     */
    public ActiveDescriptor<T> getActiveDescriptor();
    
    /**
     * This returns true if the underlying service has already been
     * created
     * 
     * @return true if the underlying service has been created
     */
    public boolean isActive();
    
    /**
     * Will destroy this object and all PerLookup instances created
     * because of this service
     */
    public void destroy();
    
    /**
     * Service data can be set on a service handle.  If the service
     * data is set prior to the services associated Context has
     * created an instance then this service data can be used
     * to influence the context's creation of the service.  The
     * service data is associated with a handle, not with
     * the service itself
     * 
     * @param serviceData Sets the serviceData for the handle
     * (may be null)
     */
    public void setServiceData(Object serviceData);
    
    /**
     * Service data can be set on a service handle.  If the service
     * data is set prior to the services associated Context has
     * created an instance then this service data can be used
     * to influence the context's creation of the service.  The
     * service data is associated with a handle, not with
     * the service itself
     * 
     * @return The service data for this service handle
     * (may return null)
     */
    public Object getServiceData();
}
