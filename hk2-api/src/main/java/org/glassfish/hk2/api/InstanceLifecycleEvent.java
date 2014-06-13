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

import java.util.Map;

/**
 * This object contains information about a lifecycle
 * event.  Not all fields are valid for all 
 * lifecycle event types
 * 
 * @author jwells
 *
 */
public interface InstanceLifecycleEvent {
    /**
     * Gets the type of event this describes.  The values may be:<UL>
     * <LI>PRE_PRODUCTION</LI>
     * <LI>POST_PRODUCTION</LI>
     * <LI>PRE_DESTRUCTION</LI>
     * </UL>
     * 
     * @return The type of event being described
     */
    public InstanceLifecycleEventType getEventType();
    
    /**
     * The active descriptor that is being used for the operation.
     * For PRE_PRODUCTION and POST_PRODUCTION this is the descriptor that
     * will create or that created the object.  For PRE_DESTRUCTION this is the
     * descriptor that will be used to destroy the object
     * 
     * @return The descriptor associated with this event
     */
    public ActiveDescriptor<?> getActiveDescriptor();
    
    /**
     * The object that is being described by this event.  In the
     * POST_PRODUCTION case this is the object that was just produced.
     * In the PRE_DESTRUCTION case this is the object that will be
     * destroyed.  Will be null in the PRE_PRODUCTION case
     * 
     * @return The object that was produced or will be destroyed.  Will
     * be null in the PRE_PRODUCTION case
     */
    public Object getLifecycleObject();
    
    /**
     * A map from the Injectee to the object actually used
     * in the production, if known.  This will return null
     * in the PRE_DESTRUCTION case.  In the PRE_PRODUCTION and
     * POST_PRODUCTION cases this will return non-null if the
     * system knows the objects that will be or were injected into
     * the produced object.  If this method returns null in the PRE_PRODUCTION or
     * POST_PRODUCTION case then the system does not know what objects
     * were injected into the produced object, which happens in the case
     * of objects created by a {@link Factory} or objects created by
     * third-party (pre-reified) ActiveDescriptors.  If this
     * method returns an empty map then the system knows that
     * nothing will be or was injected into to produced object.
     * 
     * @return The known map of injection point to injected object,
     * if that information is known.  Will be null in the
     * PRE_DESTRUCTION case and in the case where the system does
     * not know the values.
     */
    public Map<Injectee, Object> getKnownInjectees();

}
