/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.tomcat.util.digester;


import org.xml.sax.Attributes;


/**
 * <p>Abstract base class for <code>ObjectCreationFactory</code>
 * implementations.</p>
 */
abstract public class AbstractObjectCreationFactory implements ObjectCreationFactory {


    // ----------------------------------------------------- Instance Variables


    /**
     * The associated <code>Digester</code> instance that was set up by
     * {@link FactoryCreateRule} upon initialization.
     */
    protected Digester digester = null;


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Factory method called by {@link FactoryCreateRule} to supply an
     * object based on the element's attributes.
     *
     * @param attributes the element's attributes
     *
     * @throws Exception any exception thrown will be propagated upwards
     */
    public abstract Object createObject(Attributes attributes) throws Exception;


    /**
     * <p>Returns the {@link Digester} that was set by the
     * {@link FactoryCreateRule} upon initialization.
     */
    public Digester getDigester() {

        return (this.digester);

    }


    /**
     * <p>Set the {@link Digester} to allow the implementation to do logging,
     * classloading based on the digester's classloader, etc.
     *
     * @param digester parent Digester object
     */
    public void setDigester(Digester digester) {

        this.digester = digester;

    }


}
