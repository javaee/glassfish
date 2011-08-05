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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.glassfish.hk2.Descriptor;
import org.glassfish.hk2.Scope;

/**
 * A simple Descriptor.  FOR INTERNAL USE ONLY.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 */
public class DescriptorImpl implements Descriptor {

    private final LinkedHashSet<String> names;
    private final String typeName;
    private final LinkedHashSet<String> qualifiers;
    private final LinkedHashSet<String> contracts;
    private final MultiMap<String, String> metadata;
    private final Scope scope;
    
    private boolean readOnly;

    public DescriptorImpl(String name, String typeName) {
        this(name, typeName, null, null);
    }
    
    public DescriptorImpl(String name, String typeName, MultiMap<String, String> metadata, Scope scope) {
        this(null, typeName, metadata, scope, null, null);
        if (null != name) {
            addName(name);
        }
    }
    
    public DescriptorImpl(Descriptor other) {
        this(other, false);
    }
    
    public DescriptorImpl(Descriptor other, boolean readOnly) {
        this.names = new LinkedHashSet<String>(other.getNames());
        this.typeName = other.getTypeName();
        this.scope = (Scope) other.getScope();
        this.qualifiers = new LinkedHashSet<String>(other.getQualifiers());
        this.contracts = new LinkedHashSet<String>(other.getContracts());
        if (null == other.getMetadata() || MultiMap.emptyMap() == (Object)other.getMetadata()) {
            this.metadata = MultiMap.emptyMap();
        } else {
            this.metadata = new MultiMap<String, String>(other.getMetadata());
        }
        
        if (readOnly) {
            setReadOnly();
        }
    }
    
    private DescriptorImpl(LinkedHashSet<String> names,
            String typeName,
            MultiMap<String, String> metadata,
            Scope scope,
            LinkedHashSet<String> qualifiers,
            LinkedHashSet<String> contracts) {
        this.names = (null == names) ? new LinkedHashSet<String>() : names;
        this.typeName = typeName;
        this.scope = scope;
        this.qualifiers = (null == qualifiers) ? new LinkedHashSet<String>() : qualifiers;
        this.contracts = (null == contracts) ? new LinkedHashSet<String>() : contracts;
        this.metadata = (null == metadata) ? new MultiMap<String, String>() : metadata;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb, "names", names);
        append(sb, "typeName", typeName);
        append(sb, "scope", scope);
        append(sb, "qualifiers", qualifiers);
        append(sb, "contracts", contracts);
        append(sb, "metadata", metadata);
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return (null == typeName) ? -1 : typeName.hashCode();
    }
    
    @Override
    public boolean equals(Object another) {
        if (!Descriptor.class.isInstance(another)) {
            return false;
        }

        Descriptor d = Descriptor.class.cast(another);
        
        return equals(getNames(), d.getNames()) 
            && equals(getTypeName(), d.getTypeName())
            && equals(getMetadata(), d.getMetadata())
            && equals(getScope(), d.getScope())
            && equals(getContracts(), d.getContracts())
            && equals(getQualifiers(), d.getQualifiers());
    }
    
    private void append(StringBuilder sb, String key, Object val) {
        if (null != val) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key).append("=").append(val);
        }
    }

    public void setReadOnly() {
        this.readOnly = true;
    }

    public DescriptorImpl addName(String name) {
        if (readOnly) {
            throw new IllegalStateException();
        }
        names.add(name);
        return this;
    }

    public DescriptorImpl addContract(String contractFQCN) {
        if (readOnly) {
            throw new IllegalStateException();
        }
        contracts.add(contractFQCN);
        return this;
    }

    public DescriptorImpl addQualifierType(String annotation) {
        if (readOnly) {
            throw new IllegalStateException();
        }
        qualifiers.add(annotation);
        return this;
    }

    public DescriptorImpl addMetadata(String key, String value) {
        if (readOnly) {
            throw new IllegalStateException();
        }
        metadata.add(key, value);
        return this;
    }
    
    @Override
    public Collection<String> getNames() {
        return Collections.unmodifiableSet(names);
    }

    @Override
    public org.glassfish.hk2.Scope getScope() {
        return scope;
    }

    @Override
    public MultiMap<String, String> getMetadata() {
        return metadata.readOnly();
    }

    @Override
    public Collection<String> getQualifiers() {
        return Collections.unmodifiableSet(qualifiers);
    }

    @Override
    public Collection<String> getContracts() {
        return Collections.unmodifiableSet(contracts);
    }

    @Override
    public boolean hasName(String name) {
        return names.contains(name);
    }
    
    @Override
    public boolean hasQualifier(String qualifier) {
        return qualifiers.contains(qualifier);
    }

    @Override
    public boolean hasContract(String contract) {
        return contracts.contains(contract);
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
        return matches(this, another);
    }
    
    public static boolean matches(Descriptor d1, Descriptor d2) {
        if (null == d1 && null == d2) {
            return true;
        }
        
        if (null == d1 || null == d2) {
            return false;
        }
        
        return matches(d1.getNames(), d2.getNames())
            && matches(d1.getTypeName(), d2.getTypeName())
            && matches(d1.getScope(), d2.getScope())
            && matches(d1.getQualifiers(), d2.getQualifiers())
            && matches(d1.getContracts(), d2.getContracts())
            && matches(d1.getMetadata(), d2.getMetadata());
    }

    @SuppressWarnings("unchecked")
    private static boolean matches(Object o1, Object o2) {
        if (null == o1) {
            return true;
        }
        
        if (Collection.class.isInstance(o1)) {
            Collection<?> c1 = Collection.class.cast(o1);
            if (Collection.class.isInstance(o2)) {
                return matches(c1, Collection.class.cast(o2));
            }
        } else if (MultiMap.class.isInstance(o1)
                && org.glassfish.hk2.MultiMap.class.isInstance(o2)) {
            return MultiMap.class.cast(o1).matches(org.glassfish.hk2.MultiMap.class.cast(o2));
        }
        
        return equals(o1, o2);
    }

    private static boolean matches(Collection<?> c1, Collection<?> c2) {
        if (null == c1 && null == c2) {
            return true;
        }
        
        if (null == c1 || null == c2) {
            return false;
        }
        
        if (c1.size() > c2.size()) {
            return false;
        }

        for (Object o : c1) {
            if (!c2.contains(o)) {
                return false;
            }
        }
        
        return true;
    }
    

    public static boolean isEmpty(Descriptor descriptor) {
        if (null == descriptor || EMPTY_DESCRIPTOR == descriptor) {
            return true;
        }
        
        if (!isEmpty(descriptor.getNames()) || !isEmpty(descriptor.getTypeName())) {
            return false;
        }
        
        if (!isEmpty(descriptor.getContracts()) || ! isEmpty(descriptor.getQualifiers())) {
            return false;
        }
        
        if (null != descriptor.getMetadata() && descriptor.getMetadata().size() > 0) {
            return false;
        }
        
        if (null != descriptor.getScope()) {
            return false;
        }
        
        return true;
    }
    
    static boolean isEmpty(Collection<String> coll) {
        return (null == coll || coll.isEmpty());
    }

    static boolean isEmpty(String val) {
        return (null == val || val.isEmpty());
    }


    private static boolean equals(Object o1, Object o2) {
        if (null == o1 && null == o2) {
            return true;
        }
        
        if (null == o1 || null == o2) {
            return false;
        }
        
        if (o1.getClass() != o2.getClass()) {
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
        }
        
        return o1.equals(o2);
    }
    

    public static DescriptorImpl createMerged(Descriptor d1, Descriptor d2) {
        if (null == d1 && null == d2) {
            return null;
        }
        
        if (null == d1) {
            return new DescriptorImpl(d2);
        }
        
        if (null == d2) {
            return new DescriptorImpl(d1);
        }
        
        LinkedHashSet<String> names = getMergedSet(d1.getNames(), d2.getNames());
        String typeName = getMerged(d1.getTypeName(), d2.getTypeName());
        Scope scope = (Scope) getMerged(d1.getScope(), d2.getScope());
        MultiMap<String, String> metadata = getMergedMetaData(d1.getMetadata(), d2.getMetadata());
        LinkedHashSet<String> qualifiers = getMergedSet(d1.getQualifiers(), d2.getQualifiers());
        LinkedHashSet<String> contracts = getMergedSet(d1.getContracts(), d2.getContracts());
        return new DescriptorImpl(names, typeName, metadata, scope, qualifiers, contracts);
    }
    
    private static MultiMap<String, String> getMergedMetaData(
            org.glassfish.hk2.MultiMap<String, String> m1,
            org.glassfish.hk2.MultiMap<String, String> m2) {
        if (null == m1 && null == m2) {
            return null;
        }

        MultiMap<String, String> mm = new MultiMap<String, String>();
        if (null != m1) {
            mm.mergeAll(m1);
        }

        if (null != m2) {
            mm.mergeAll(m2);
        }

        return mm;
    }

    private static LinkedHashSet<String> getMergedSet(Collection<String> s1, Collection<String> s2) {
        if (null == s1 && null == s2) {
            return null;
        }
        
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        if (null != s1) {
            set.addAll(s1);
        }

        if (null != s2) {
            set.addAll(s2);
        }
        
        return set;
    }

    static <T> T getMerged(T v1, T v2) {
        if (null == v1) {
            return v2;
        }
        
        if (null == v2) {
            return v1;
        }
        
        if (!v1.equals(v2)) {
            throw new IllegalStateException("can't merge " + v1 + " and " + v2);
        }
        
        return v1;
    }


    final static Descriptor EMPTY_DESCRIPTOR = new Descriptor() {
        @Override
        public Collection<String> getNames() {
            return Collections.emptySet();
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

        @Override
        public org.glassfish.hk2.Scope getScope() {
            return null;
        }

        @Override
        public boolean hasName(String name) {
            return false;
        }
        
        @Override
        public boolean hasQualifier(String qualifier) {
            return false;
        }

        @Override
        public boolean hasContract(String contract) {
            return false;
        }
    };

}
