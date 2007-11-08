/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * RuntimeMappingModelFactory.java
 *
 * Created on April 14, 2005, 10:41 AM
 *
 */

package com.sun.persistence.runtime.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.persistence.api.model.mapping.MappingModelFactory;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public interface RuntimeMappingModelFactory extends MappingModelFactory {

    // <editor-fold desc="//======================= model handling ============================">

    /**
     * Creates a new empty RuntimeMappingModel instance.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     * @throws ModelException if impossible
     */
    public RuntimeMappingModel createMappingModel(JDOModel jdoModel,
            Object key) throws ModelException;

    /**
     * Returns the RuntimeMappingModel instance for the specified jdoModel and
     * key.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     * @throws ModelException if impossible
     */
    public RuntimeMappingModel getMappingModel(JDOModel jdoModel, Object key);

    // </editor-fold>
}
