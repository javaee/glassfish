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
package org.glassfish.hk2.configuration.persistence.properties;

import java.io.IOException;
import java.util.Properties;

/**
 * This handle is used to read property files and put the values into the
 * HK2 configuration hub.  The readFile method can be called multiple times
 * if the file should be read again because the instances or property values
 * may have changed
 * 
 * @author jwells
 *
 */
public interface PropertyFileHandle {
    /**
     * Reads the file associated with this handle and will do the following:
     * <UL>
     * <LI>Add any type found not previously added by this handle</LI>
     * <LI>Add any instance found not previously added by this handle</LI>
     * <LI>Modify any property that has changed value</LI>
     * <LI>Remove any instance no longer seen in the file but that had previously been added</LI>
     * <UL>
     * In particular this method will NOT remove a type that was previously added but
     * which has no more instances (other files may be contributing to the same type).
     * After reaching the end of the input stream this method will close it
     * 
     * @param Properties The properties object to inspect.  May not be null
     */
    public void readProperties(Properties properties);
    
    /**
     * Returns the specific type associated with this handle
     * 
     * @return The specific type this handle is updating.  May
     * return null if this is a multi-type handle
     */
    public String getSpecificType();
    
    /**
     * Returns the default type name if the type cannot
     * be determined from the key of the property.  Will
     * return null if getSpecificType is not null
     * 
     * @return The default type name if the type cannot
     * be determined, or null if this handle has a
     * specific type
     */
    public String getDefaultType();
    
    /**
     * Gets the default instance name that will be given
     * to instances whose name cannot otherwise be determined
     * 
     * @return The default instance name.  Will not return
     * null
     */
    public String getDefaultInstanceName();
    
    /**
     * Will remove any instances added by this handle from
     * the hub, and make it such that this handle can no longer
     * be used
     */
    public void dispose();

}
