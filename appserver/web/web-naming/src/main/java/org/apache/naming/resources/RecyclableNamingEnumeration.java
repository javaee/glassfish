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

package org.apache.naming.resources;

import java.util.Vector;
import java.util.Enumeration;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;

/**
 * Naming enumeration implementation.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.1.2.1 $ $Date: 2007/08/17 15:46:28 $
 */

public class RecyclableNamingEnumeration<E> 
    implements NamingEnumeration<E> {


    // ----------------------------------------------------------- Constructors


    public RecyclableNamingEnumeration(Vector<E> entries) {
        this.entries = entries;
        recycle();
    }


    // -------------------------------------------------------------- Variables


    /**
     * Entries.
     */
    protected Vector<E> entries;


    /**
     * Underlying enumeration.
     */
    protected Enumeration<E> enumeration;


    // --------------------------------------------------------- Public Methods


    /**
     * Retrieves the next element in the enumeration.
     */
    public E next()
        throws NamingException {
        return nextElement();
    }


    /**
     * Determines whether there are any more elements in the enumeration.
     */
    public boolean hasMore()
        throws NamingException {
        return enumeration.hasMoreElements();
    }


    /**
     * Closes this enumeration.
     */
    public void close()
        throws NamingException {
    }


    public boolean hasMoreElements() {
        return enumeration.hasMoreElements();
    }


    public E nextElement() {
        return enumeration.nextElement();
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Recycle.
     */
    void recycle() {
        enumeration = entries.elements();
    }


}

