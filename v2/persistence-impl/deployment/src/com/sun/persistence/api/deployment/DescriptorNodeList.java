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


package com.sun.persistence.api.deployment;

import java.util.Collection;

/**
 * This special implementation of {@link java.util.List} is used in modified
 * JAXB generated code for collection valued non-primitive properties. We only
 * implement methods that are used to mutate the list. When ever the mutator
 * methods of this list is called, it calls {@link DescriptorNode#parent(DescriptorNode)}
 * to either set parent reference or clear the parent node.
 */
class DescriptorNodeList <E extends DescriptorNode>
        extends java.util.ArrayList<E> {

    /* parent node which this list belongs to */
    private DescriptorNode parent;

    /**
     * Create a new DescriptorNodeList.
     *
     * @param parent which contains this list.
     */
    public DescriptorNodeList(DescriptorNode parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean add(E e) {
        boolean result = super.add(e);
        e.parent(parent);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean remove(Object o) {
        boolean result = super.remove(o);
        DescriptorNode.class.cast(o).parent(null);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean addAll(Collection<? extends E> c) {
        boolean result = super.addAll(c);
        for (E e : c) {
            e.parent(parent);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean addAll(int index, Collection<? extends E> c) {
        boolean result = super.addAll(index, c);
        for (E e : c) {
            e.parent(parent);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean removeAll(Collection<?> c) {
        boolean result = super.removeAll(c);
        for (Object o : c) {
            DescriptorNode.class.cast(o).parent(null);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean retainAll(Collection<?> c) {
        boolean result = false;
        for (E e : this) {
            if (!c.contains(e)) {
                result |= remove(e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void clear() {
        for (E e : this) {
            e.parent(null);
        }
        super.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override public void add(int index, E element) {
        super.add(index, element);
        element.parent(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override public E remove(int index) {
        E e = super.remove(index);
        e.parent(null);
        return e;
    }
}

