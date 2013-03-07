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
 * This processor is called for certain events in the lifecycle of instances
 * of services.
 * <p>
 * This listener is concerned with instances of services, whereas the
 * {@link ValidationService} is concerned with the descriptors for services.
 * 
 * @author jwells
 */
@Contract
public interface InstanceLifecycleListener {
    /**
     * This returns a filter that tells the system whether a particular descriptor should be handled by this lifecycle
     * listener.  The filter can be called at any time
     * 
     * @return The filter that tells the system if this listener applies to this descriptor.  If this returns null then
     * this Listener will apply to ALL descriptors.
     */
    public Filter getFilter();
    
    /**
     * This method will be called when any lifecycle event occurs.  The currently supported
     * lifecycle events are PRE_PRODUCTION, POST_PRODUCTION and PRE_DESTRUCTION.  Code should be written to
     * allow for future events to be generated.  This method should not throw exceptions
     * 
     * @param lifecycleEvent The event that has occurred, will not be null
     */
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent);
}
