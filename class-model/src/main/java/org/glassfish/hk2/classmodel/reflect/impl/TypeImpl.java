/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.net.URI;

/**
 * Implementation of the Type abstraction.
 *
 * @author Jerome Dochez
 */
public class TypeImpl extends AnnotatedElementImpl implements Type {

    private final TypeProxy<Type> sink;
    private final List<MethodModel> methods = new ArrayList<MethodModel>();
    private final Set<URI> definingURIs= new HashSet<URI>();


    public TypeImpl(String name, TypeProxy<Type> sink) {
        super(name);
        this.sink = sink;
    }

    @Override
    public Collection<URI> getDefiningURIs() {
        return Collections.unmodifiableSet(definingURIs);
    }

    synchronized void addDefiningURI(URI uri) {
        definingURIs.add(uri);
        try {
            File file = new File(uri);
//            assert(file.exists()) : file + " does not exist";
            definingURIs.add(file.getCanonicalFile().toURI());
        } catch (IOException e) {
            // ignore, this is a safeguard for confused user's code that do not
            // deal well with file path.
        }
    }

    @Override
    public boolean wasDefinedIn(Collection<URI> uris) {
        for (URI uri : uris) {
            if (definingURIs.contains(uri)) {
                return true;
            }
        }
        return false;
    }

    synchronized void addMethod(MethodModelImpl m) {
        methods.add(m);
    }

    @Override
    public Collection<MethodModel> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    TypeProxy<Type> getProxy() {
        return sink;
    }

    @Override
    public Collection<Member> getReferences() {
        return sink.getRefs();
    }

    @Override
    protected void print(StringBuffer sb) {
        super.print(sb);    //To change body of overridden methods use File | Settings | File Templates.
        sb.append(", subclasses=[");
        for (AnnotatedElement cm : sink.getSubTypeRefs()) {
            sb.append(" ").append(cm.getName());
        }
        sb.append("]");
    }
}
