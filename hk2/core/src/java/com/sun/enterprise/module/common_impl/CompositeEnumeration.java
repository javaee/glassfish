/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.module.common_impl;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * We need a compound enumeration so that we can aggregate the results from
 * various delegates.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class CompositeEnumeration implements Enumeration<URL> {
    /*
     * TODO(Sahoo): Merge with FlattenIterator.
     */

    Enumeration<URL>[] enumerators;
    int index = 0; // current position, lazily initialized

    public CompositeEnumeration(List<Enumeration<URL>> enumerators) {
        this.enumerators = enumerators.toArray(new Enumeration[enumerators.size()]);
    }

    public boolean hasMoreElements() {
        Enumeration<URL> current = getCurrent();
        return (current!=null) ? true : false;
    }

    public URL nextElement() {
        Enumeration<URL> current = getCurrent();
        if (current != null) {
            return current.nextElement();
        } else {
            throw new NoSuchElementException("No more elements in this enumeration");
        }
    }

    private Enumeration<URL> getCurrent() {
        for (int start = index; start < enumerators.length; start++) {
            Enumeration<URL> e = enumerators[start];
            if (e.hasMoreElements()) {
                index = start;
                return e;
            }
        }
        // no one has any elements, set the index to max and return null
        index = enumerators.length;
        return null;
    }
}
