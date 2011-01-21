/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;

import java.util.*;

/**
 * contains all the parsed types references.
 * @author Jerome Dochez
 */
public class TypesCtr implements Types {
    
    @Override
    public Type getBy(String name) {
        for (Map<String, TypeProxy<Type>> map : storage.values()) {
            TypeProxy proxy = map.get(name);
            if (proxy!=null) {
                return proxy.get();
            }
        }
        return null;
    }

    @Override
    public <T extends Type> T getBy(Class<T> type, String name) {
        Type t = getBy(name);
        try {
            return type.cast(t);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public synchronized <T extends Type> TypeProxy<Type> getHolder(String name, Class<T> type) {
        Map<String, TypeProxy<Type>> typeStorage = storage.get(type);
        if (typeStorage==null) {
            typeStorage = new HashMap<String, TypeProxy<Type>>();
            storage.put(type, typeStorage);
        }
        TypeProxy<Type> typeProxy = typeStorage.get(name);
        if (typeProxy ==null) {
            typeProxy = new TypeProxy<Type>(null, name);
            nonVisited.push(typeProxy);
            typeStorage.put(name, typeProxy);
        }
        return typeProxy;
    }

    public interface ProxyTask {
        public void on(TypeProxy<?> proxy);
    }

    /**
     * Runs a task on each non visited types parsing discovered.
     *
     * @param proxyTask the task to run on each non visited type.
     */
    public void onNotVisitedEntries(ProxyTask proxyTask) {
        while(!nonVisited.isEmpty()) {
            TypeProxy proxy = nonVisited.pop();
            if (!proxy.isVisited()) {
                proxyTask.on(proxy);
            }
        }
    }

    @Override
    public Collection<Type> getAllTypes() {
        List<Type> allTypes = new ArrayList<Type>();
        for (Map<String, TypeProxy<Type>> map : storage.values()) {
            for (TypeProxy typeProxy : map.values()) {
                if (typeProxy.get()!=null) {
                    allTypes.add(typeProxy.get());
                }
            }
        }
        return allTypes;
    }


    private final Map<Class, Map<String, TypeProxy<Type>>> storage=new HashMap<Class, Map<String, TypeProxy<Type>>>();
    /**
     * Stack on type proxy as they have been instantiated in FILO order.
     */
    private final Stack<TypeProxy> nonVisited = new Stack<TypeProxy>();
}
