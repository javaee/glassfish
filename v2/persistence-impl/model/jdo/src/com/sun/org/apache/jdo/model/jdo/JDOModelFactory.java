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

package com.sun.org.apache.jdo.model.jdo;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaModel;


/**
 * Factory for JDOModel instances. The factory provides a mechanism to cache 
 * JDOModel instances per user defined keys.
 * 
 * @author Michael Bouschen
 * @version 2.0
 */
public interface JDOModelFactory 
{
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
     * @exception ModelException if impossible
     */
    public JDOModel createJDOModel(JavaModel javaModel,
                                   boolean loadXMLMetadataDefault)
        throws ModelException;
    
    /**
     * Returns the JDOModel instance for the specified javaModel.
     * @param javaModel the javaModel used to cache the returned JDOModel
     * instance.
     */
    public JDOModel getJDOModel(JavaModel javaModel);
    
    /**
     * Returns the JDOModel instance for the specified javaModel.  
     * The returned JDOModel instance uses the specified flag
     * <code>loadXMLMetadataDefault</code> to set the default behavior 
     * for the creation of new JDOClass instances  using methods 
     * {@link JDOModel#createJDOClass(String)} and 
     * {@link JDOModel#getJDOClass(String)} for which the caller doesn't 
     * explicitly specify whether to read XML metatdata or not.
     * @param loadXMLMetadataDefault the default setting for whether to 
     * read XML metatdata in JDOModel's methods for JDOClass creation.
     */
    public JDOModel getJDOModel(JavaModel javaModel, 
                                boolean loadXMLMetadataDefault);

    /**
     * Removes the specified jdoModel from the JDOModel cache. Note, if
     * there are multiple entries in the cache with the specified jdoModel
     * as value, then all of them get removed. The method does not have an
     * effect, if this factory does not have the specified jdoModel.
     * @param jdoModel the JDOModel to be removed.
     * @since 2.0
     */
    public void removeJDOModel(JDOModel jdoModel)
        throws ModelException;

    /**
     * Removes the JDOModel for the specified javaModel from the JDOModel
     * cache. The method does not have an effect, if this factory does not
     * have a JDOModel for the the specified javaModel.
     * @param javaModel the javaModel used to find the JDOModel instance to be
     * removed.
     * @since 2.0
     */
    public void removeJDOModel(JavaModel javaModel)
        throws ModelException;

}
