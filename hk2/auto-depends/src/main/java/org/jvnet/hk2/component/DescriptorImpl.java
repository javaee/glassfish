/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.glassfish.hk2.Descriptor;

/**
 * A simple Descriptor Builder.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 */
/*public*/ class DescriptorImpl implements Descriptor {

    private final String name;
    private final String typeName;
    // TODO: handle scope
//    private final Scope scope;
    private final List<String> qualifiers = new ArrayList<String>();
    private final List<String> contracts = new ArrayList<String>();
    private final MultiMap<String, String> metadata = new MultiMap<String, String>();

    public DescriptorImpl(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }
    
    public DescriptorImpl(Descriptor other) {
        this.name = other.getName();
//        this.scope = other.getScope();
        this.typeName = other.getTypeName();
        this.qualifiers.addAll(other.getQualifiers());
        this.contracts.addAll(other.getContracts());
    }
    
    void addContract(String contractFQCN) {
        contracts.add(contractFQCN);
    }

    void addQualifierType(String annotation) {
        qualifiers.add(annotation);
    }

    @Override
    public String getName() {
        return name;
    }

//    @Override
//    public Scope getScope() {
//        return scope;
//    }

    @Override
    public MultiMap<String, String> getMetadata() {
        return metadata.readOnly();
    }

    @Override
    public Collection<String> getQualifiers() {
        return Collections.unmodifiableList(qualifiers);
    }

    @Override
    public Collection<String> getContracts() {
        return Collections.unmodifiableList(contracts);
    }

    @Override
    public String getTypeName() {
        return typeName;
    }
    
    /**
     * Returns true if this instance "matches" another instance.
     * 
     * <p/>
     * Matching considers each attribute of the descriptor one by one.
     * For non-null attributes, equality checks are made. If the this
     * instance contains a null value (or empty for the case for collections)
     * then the result for that field is true as well. For non-null and non-
     * empty collections, this instance must be a proper subset of the
     * other.
     * 
     * @param another the other Descriptor to compare against
     * @return true if all fields in this instance matches another
     */
    public boolean matches(Descriptor another) {
        return matches(name, another.getName())
            && matches(typeName, another.getTypeName())
            && matches(qualifiers, another.getQualifiers())
            && matches(contracts, another.getContracts())
            && matches(metadata, another.getMetadata());
    }

    private static boolean equals(Object o1, Object o2) {
        if (null == o1 && null == o2) {
            return true;
        }
        
        if (null == o1 || null == o2) {
            return false;
        }
        
        if (o1.getClass() != o2.getClass()) {
            return false;
        }
        
        if (Collection.class.isInstance(o1)) {
            Collection<?> c1 = Collection.class.cast(o1);
            Collection<?> c2 = Collection.class.cast(o2);
            if (c1.size() != c2.size()) {
                return false;
            }
            
            Iterator<?> it1 = c1.iterator();
            Iterator<?> it2 = c2.iterator();
            while (it1.hasNext()) {
                if (!it1.next().equals(it2.next())) {
                    return false;
                }
            }
            
            return true;
        }
        
        return o1.equals(o2);
    }
    
    private static boolean matches(Object o1, Object o2) {
        if (null == o1) {
            return true;
        }
        
        if (Collection.class.isInstance(o1)) {
            Collection<?> c1 = Collection.class.cast(o1);
            if (c1.isEmpty()) {
                return true;
            }
        } else if (MultiMap.class.isInstance(o1)
                && org.glassfish.hk2.MultiMap.class.isInstance(o2)) {
            return MultiMap.class.cast(o1).matches(org.glassfish.hk2.MultiMap.class.cast(o2));
        }
        
        return equals(o1, o2);
    }

    final static Descriptor EMPTY_DESCRIPTOR = new Descriptor() {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public MultiMap<String, String> getMetadata() {
            return org.jvnet.hk2.component.MultiMap.emptyMap();
        }

        @Override
        public Collection<String> getQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Collection<String> getContracts() {
            return Collections.emptySet();
        }

        @Override
        public String getTypeName() {
            return null;
        }
    };
}
