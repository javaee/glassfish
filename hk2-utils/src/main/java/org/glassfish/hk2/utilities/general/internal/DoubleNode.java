/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.general.internal;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Used for doubly linked lists with weak keys
 * 
 * @author jwells
 *
 * @param <K> key
 * @param <V> value
 */
public class DoubleNode<K, V> {
    private final WeakReference<K> weakKey;
    private final V value;
    private DoubleNode<K, V> previous;
    private DoubleNode<K, V> next;
    private K hardenedKey;
    
    public DoubleNode(K key, V value, ReferenceQueue<? super K> queue) {
        weakKey = new WeakReference<K>(key, queue);
        this.value = value;
    }

    /**
     * @return the previous
     */
    public DoubleNode<K, V> getPrevious() {
        return previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(DoubleNode<K, V> previous) {
        this.previous = previous;
    }

    /**
     * @return the next
     */
    public DoubleNode<K, V> getNext() {
        return next;
    }

    /**
     * @param next the next to set
     */
    public void setNext(DoubleNode<K, V> next) {
        this.next = next;
    }

    /**
     * @return the weakKey
     */
    public WeakReference<K> getWeakKey() {
        return weakKey;
    }

    /**
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * @return the hardenedKey
     */
    public K getHardenedKey() {
        return hardenedKey;
    }

    /**
     * @param hardenedKey the hardenedKey to set
     */
    public void setHardenedKey(K hardenedKey) {
        this.hardenedKey = hardenedKey;
    }
    
    
    
    
}