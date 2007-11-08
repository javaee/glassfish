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
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.model.jdo.caching;

import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOModelFactory;

import com.sun.org.apache.jdo.impl.model.jdo.JDOModelFactoryImplDynamic;

/**
 * Factory for caching JDOModel instances.
 *
 * @author Michael Bouschen
 * @since 1.1
 * @version 1.1
 */
public class JDOModelFactoryImplCaching extends JDOModelFactoryImplDynamic {

    /** The singleton JDOModelFactory instance. */    
    private static JDOModelFactory jdoModelFactory = 
        new JDOModelFactoryImplCaching();

    /**
     * Creates new JDOModelFactoryImplCaching. This constructor
     * should not be called directly; instead, the singleton access
     * method  {@link #getInstance} should be used.
     */
    protected JDOModelFactoryImplCaching() {}

    /** 
     * Get an instance of JDOModelFactoryImpl.
     * @return an instance of JDOModelFactoryImpl
     */    
    public static JDOModelFactory getInstance() {
        return jdoModelFactory;
    }
    
    /**
     * Creates a new empty JDOModel instance. 
     * The returned JDOModel instance uses the specified flag
     * <code>loadXMLMetadataDefault</code> to set the default behavior 
     * for the creation of new JDOClass instances  using methods 
     * {@link JDOModel#createJDOClass(String)} and 
     * {@link JDOModel#getJDOClass(String)} for which the caller doesn't 
     * explicitly specify whether to read XML metatdata or not.
     * @param loadXMLMetadataDefault the default setting for whether to 
     * read XML metatdata in JDOModel's methods for JDOClass creation.
     */
    public JDOModel createJDOModel(JavaModel javaModel,
                                   boolean loadXMLMetadataDefault) {
        return new JDOModelImplCaching(javaModel, loadXMLMetadataDefault);
    }

}
