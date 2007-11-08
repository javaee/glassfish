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

package com.sun.persistence.api.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOModel;

/**
 * Factory for MappingModel instances. The factory provides a mechanism to cache
 * MappingModel per JDOModel and key.
 * 
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public interface MappingModelFactory {

    // <editor-fold desc="//======================= model handling ============================">

    /**
     * Creates a new empty MappingModel instance.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     * @throws ModelException if impossible
     */
    public MappingModel createMappingModel(JDOModel jdoModel, Object key)
            throws ModelException;

    /**
     * Returns the MappingModel instance for the specified jdoModel and key.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     * @throws ModelException if impossible
     */
    public MappingModel getMappingModel(JDOModel jdoModel, Object key);

    /**
     * Removes the specified mappingModel from the MappingModel cache.
     * Note, if there are multiple entries in the cache with the specified
     * mappingModel as value, then all of them get removed. The method does
     * not have an effect, if this factory does not have the specified
     * mappingModel.
     * @param mappingModel the MappingModel to be removed.
     */
    public void removeMappingModel(MappingModel mappingModel)
        throws ModelException;

    /**
     * Removes the MappingModel for the specified jdoModel and key from the
     * MappingModel cache. The method does not have an effect, if this
     * factory does not have a MappingModel for the the specified jdoModel
     * and key.
     * @param jdoModel this, together with the specified key, is used to find
     * the MappingModel instance to be removed.
     * @param key this, together with the specified jdoModel, is used to find
     * the returned MappingModel instance to be removed.
     */
    public void removeMappingModel(JDOModel jdoModel, Object key)
        throws ModelException;

    // </editor-fold>
}
