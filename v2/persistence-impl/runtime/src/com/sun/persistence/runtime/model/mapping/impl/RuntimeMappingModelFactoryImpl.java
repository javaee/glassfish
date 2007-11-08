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
 * RuntimeMappingModelFactoryImpl.java
 *
 */

package com.sun.persistence.runtime.model.mapping.impl;

import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.persistence.impl.model.mapping.MappingModelFactoryImplDynamic;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModelFactory;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public class RuntimeMappingModelFactoryImpl
    extends MappingModelFactoryImplDynamic
    implements RuntimeMappingModelFactory {
    
    /** The singleton RuntimeMappingModelFactory instance. */
    private static final RuntimeMappingModelFactory mappingModelFactory = 
        new RuntimeMappingModelFactoryImpl();

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Creates new RuntimeMappingModelFactory. This constructor should not be
     * called directly; instead, the singleton access method
     * {@link #getInstance} should be used.
     */
    protected RuntimeMappingModelFactoryImpl() {}

    /** 
     * Get an instance of RuntimeMappingModelFactory.
     * @return an instance of RuntimeMappingModelFactory
     */    
    public static RuntimeMappingModelFactory getInstance() {
        return mappingModelFactory;
    }

    // </editor-fold>

    // <editor-fold desc="//======================= model handling ============================">

    /** @inheritDoc */
    public RuntimeMappingModel createMappingModel(
        JDOModel jdoModel, Object key) {
        return new RuntimeMappingModelImpl(jdoModel);
    }

   /** @inheritDoc */
    public RuntimeMappingModel getMappingModel(JDOModel jdoModel, Object key) {
        return (RuntimeMappingModel) super.getMappingModel(jdoModel, key);
    }
    
    // </editor-fold>
}
