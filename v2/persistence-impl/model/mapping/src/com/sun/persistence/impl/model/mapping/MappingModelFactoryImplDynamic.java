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
 * MappingModelFactoryImplDynamic.java
 *
 */

package com.sun.persistence.impl.model.mapping;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.persistence.api.model.mapping.MappingModel;
import com.sun.persistence.api.model.mapping.MappingModelFactory;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingModelFactoryImplDynamic extends MappingElementImpl
    implements MappingModelFactory {

    /** Map of MappingModel instances, key is the JDOModel. */
    private final Map modelCache = new HashMap();

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Constructor.
     */
    protected MappingModelFactoryImplDynamic() { }

    // </editor-fold>

    // <editor-fold desc="//======================= model handling ============================">

    /**
     * Creates a new empty MappingModel instance.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     */
    public MappingModel createMappingModel(JDOModel jdoModel, Object key) {
        return new MappingModelImplDynamic(jdoModel);
    }

    /**
     * Returns the MappingModel instance for the specified jdoModel and key.
     * @param jdoModel this, together with the specified key, is used to cache
     * the returned MappingModel instance.
     * @param key this, together with the specified jdoModel, is used to cache
     * the returned MappingModel instance.
     * @return a MappingModel instance for the specified jdoModel and key
     */
    public MappingModel getMappingModel(JDOModel jdoModel, Object key) {
        synchronized (modelCache) {
            Object mappingModelKey = calculateMappingModelKey(jdoModel, key);
            MappingModel mappingModel = 
                (MappingModel) modelCache.get(mappingModelKey);
            
            if (mappingModel == null) {
                // create new model and store it using the calculated key
                mappingModel = createMappingModel(jdoModel, key);
                modelCache.put(mappingModelKey, mappingModel);
            }
            return mappingModel;
        }
    }

    /**
     * Removes the specified mappingModel from the MappingModel cache. 
     * Note, if there are multiple entries in the cache with the specified
     * mappingModel as value, then all of them get removed. The method does
     * not have an effect, if this factory does not have the specified
     * mappingModel.
     * @param mappingModel the MappingModel to be removed.
     */
    public void removeMappingModel(MappingModel mappingModel) {
        if (mappingModel == null) {
            // nothing to be removed => return
            return;
        }

        synchronized (modelCache) {
            for (Iterator i = modelCache.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                Object value = entry.getValue();
                if (mappingModel.equals(value)) {
                    // found mappingModel => remove the entry
                    i.remove();
                }
            }
        }
    }

    /**
     * Removes the MappingModel for the specified jdoModel and key from the
     * MappingModel cache. The method does not have an effect, if this
     * factory does not manage a MappingModel for the the specified jdoModel
     * and key.
     * @param jdoModel this, together with the specified key, is used to find
     * the MappingModel instance to be removed.
     * @param key this, together with the specified jdoModel, is used to find
     * the returned MappingModel instance to be removed.
     */
    public void removeMappingModel(JDOModel jdoModel, Object key) {
        synchronized (modelCache) {
            Object mappingModelKey = calculateMappingModelKey(jdoModel, key);
            modelCache.remove(mappingModelKey);
        }
    }

    // </editor-fold>

    // <editor-fold desc="//======================= internal helper methods ============================">

    /** 
     * Caclulates a key for caching mapping model instances based on the
     * specified jdoModel instance and the specified environement specific
     * key.
     * <p>
     * This implementation uses the specified jdoModel as key.
     * @param jdoModel 
     * @param key
     * @return a key for caching mapping model instances
     */
    protected Object calculateMappingModelKey (JDOModel jdoModel, Object key) {
        return jdoModel;
    }
    
    // </editor-fold>
    
}
