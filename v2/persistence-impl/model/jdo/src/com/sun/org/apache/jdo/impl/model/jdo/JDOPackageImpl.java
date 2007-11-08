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

package com.sun.org.apache.jdo.impl.model.jdo;

import java.util.*;

import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOPackage;

/**
 * A JDOPackage instance represents the JDO package metadata.
 *
 * @author Michael Bouschen
 */
public class JDOPackageImpl 
    extends JDOElementImpl
    implements JDOPackage
{
    /** The package name. */
    private String name;

    /** Relationship JDOModel<->JDOPackage. Initialized during creation.*/
    private JDOModel declaringModel;

    /**
     * Returns the name of this JDOPackage.
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of this JDOPackage.
     * @param name the name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the declaring JDOModel of this JDOPackage.
     * @return the JDOModel that owns this JDOPackage.
     */
    public JDOModel getDeclaringModel()
    {
        return declaringModel;
    }

    /**
     * Set the declaring JDOModel for this JDOPackage.
     * @param model the declaring JDOModel of this JDOPackage.
     */
    public void setDeclaringModel(JDOModel model)
    {
        this.declaringModel = model;
    }
}
