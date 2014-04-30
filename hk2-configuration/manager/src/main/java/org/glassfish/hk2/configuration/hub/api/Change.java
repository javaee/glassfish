/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.hub.api;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * @author jwells
 *
 */
public interface Change {
    /**
     * Gets the category of change this Change object
     * represents
     * 
     * @return <UL>
     * <LI>REMOVE_TYPE</LI>
     * <LI>ADD_TYPE</LI>
     * <LI>ADD_INSTANCE</LI>
     * <LI>REMOVE_INSTANCE</LI>
     * <LI>MODIFY_INSTANCE</LI>
     * </UL>
     */
    public ChangeCategory getChangeCategory();
    
    /**
     * Gets the type of the change for all change categories.  In
     * the case of ADD_TYPE the value returned will include all
     * instances added, but there will also be an ADD_INSTANCE
     * change sent for each instance of this type that was added.
     * In the case of REMOVE_TYPE the value return will include
     * all instances still in the type at the time of removal, but
     * there will also be a REMOVE_INSTANCE change sent for each
     * instance that was in the type at the time of type removal 
     * 
     * @return The type of the change.  Will not be null
     */
    public Type getChangeType();
    
    /**
     * Returns the key of the instance that was removed, added or modified
     * for the categories ADD_INSTANCE, REMOVE_INSTANCE and MODIFY_INSTANCE
     * 
     * @return The key of the instance that was added, removed or modified.
     * Returns null for change category REMOVE_TYPE or ADD_TYPE
     */
    public Object getInstanceKey();
    
    /**
     * Returns the value of the instance that was removed, added or modified
     * for the categories ADD_INSTANCE, REMOVE_INSTANCE and MODIFY_INSTANCE
     * 
     * @return The value of the instance that was added, removed or modified.
     * Returns null for change category REMOVE_TYPE or ADD_TYPE
     */
    public Object getInstanceValue();
    
    /**
     * Returns a list of properties that were changed if the change category
     * is MODIFY_INSTANCE.
     * 
     * @return A non-null and non-empty list of modified properties that were
     * changed in the instance for change category MODIFY_INSTANCE.  Returns
     * null for all other change categories
     */
    public List<PropertyChangeEvent> getModifiedProperties();
    
    public enum ChangeCategory {
        REMOVE_TYPE,
        ADD_TYPE,
        ADD_INSTANCE,
        REMOVE_INSTANCE,
        MODIFY_INSTANCE
        
    }

}
