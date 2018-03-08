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

package org.apache.naming;

import java.util.Iterator;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;

/**
 * Naming enumeration implementation.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:29:04 $
 */

public class NamingContextBindingsEnumeration 
    implements NamingEnumeration<Binding> {


    // ----------------------------------------------------------- Constructors


    public NamingContextBindingsEnumeration(Iterator<NamingEntry> entries,
            Context ctx) {
        iterator = entries;
        this.ctx = ctx;
    }


    // -------------------------------------------------------------- Variables


    /**
     * Underlying enumeration.
     */
    protected Iterator<NamingEntry> iterator;


    /**
     * The context for which this enumeration is being generated.
     */
    private Context ctx;


    // --------------------------------------------------------- Public Methods


    /**
     * Retrieves the next element in the enumeration.
     */
    public Binding next()
        throws NamingException {
        return nextElementInternal();
    }


    /**
     * Determines whether there are any more elements in the enumeration.
     */
    public boolean hasMore()
        throws NamingException {
        return iterator.hasNext();
    }


    /**
     * Closes this enumeration.
     */
    public void close()
        throws NamingException {
    }


    public boolean hasMoreElements() {
        return iterator.hasNext();
    }


    public Binding nextElement() {
        try {
            return nextElementInternal();
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private Binding nextElementInternal() throws NamingException {
        NamingEntry entry = iterator.next();
        Object value;
        
        // If the entry is a reference, resolve it
        if (entry.type == NamingEntry.REFERENCE
                || entry.type == NamingEntry.LINK_REF) {
            try {
                value = ctx.lookup(new CompositeName(entry.name));
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                NamingException ne = new NamingException(e.getMessage());
                ne.initCause(e);
                throw ne;
            }
        } else {
            value = entry.value;
        }
        
        return new Binding(entry.name, value.getClass().getName(), value, true);
    }
}

