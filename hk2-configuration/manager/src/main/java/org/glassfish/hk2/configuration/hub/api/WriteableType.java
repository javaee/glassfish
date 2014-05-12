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

/**
 * @author jwells
 *
 */
public interface WriteableType extends Type {
    /**
     * Adds the instance with the given key to the type
     * 
     * @param key A non-null name for this bean
     * @param bean The non-null bean to add
     */
    public void addInstance(String key, Object bean);
    
    /**
     * Removes the instance with the given key from the type
     * @param key A non-null name for this bean
     * @return The possibly null bean that was removed.  If null
     * then no bean was found with the given name
     */
    public Object removeInstance(String key);
    
    /**
     * Modifies the instance with the given key
     * 
     * @param key A non-null name or key for the bean to modify
     * @param newBean The new bean to use with this key
     * @param changes The full set of changes from the previous version.  If this
     * is a zero-length array then the system will attempt to automatically determine
     * the changes made to this type and will generate the list of PropertyChangeEvent
     * to be associated with this modification
     * @return If changes has length greater than zero then this simply returns changes.
     * If changes is zero length then this will return the set of changes automatically
     * determined by the system
     */
    public PropertyChangeEvent[] modifyInstance(String key, Object newBean, PropertyChangeEvent... changes);

}
