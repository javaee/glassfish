

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package org.apache.tomcat.util.collections;

import java.util.Hashtable;

/**
 * This class implements a Generic LRU Cache
 *
 *
 * @author Ignacio J. Ortega
 *
 */

public class LRUCache
{
    class CacheNode
    {

        CacheNode prev;
        CacheNode next;
        Object value;
        Object key;

        CacheNode()
        {
        }
    }


    public LRUCache(int i)
    {
        currentSize = 0;
        cacheSize = i;
        nodes = new Hashtable(i);
    }

    public Object get(Object key)
    {
        CacheNode node = (CacheNode)nodes.get(key);
        if(node != null)
        {
            moveToHead(node);
            return node.value;
        }
        else
        {
            return null;
        }
    }

    public void put(Object key, Object value)
    {
        CacheNode node = (CacheNode)nodes.get(key);
        if(node == null)
        {
            if(currentSize >= cacheSize)
            {
                if(last != null)
                    nodes.remove(last.key);
                removeLast();
            }
            else
            {
                currentSize++;
            }
            node = new CacheNode();
        }
        node.value = value;
        node.key = key;
        moveToHead(node);
        nodes.put(key, node);
    }

    public Object remove(Object key) {
        CacheNode node = (CacheNode)nodes.get(key);
        if (node != null) {
            if (node.prev != null) {
                node.prev.next = node.next;
            }
            if (node.next != null) {
                node.next.prev = node.prev;
            }
            if (last == node)
                last = node.prev;
            if (first == node)
                first = node.next;
        }
        return node;
    }

    public void clear()
    {
        first = null;
        last = null;
    }

    private void removeLast()
    {
        if(last != null)
        {
            if(last.prev != null)
                last.prev.next = null;
            else
                first = null;
            last = last.prev;
        }
    }

    private void moveToHead(CacheNode node)
    {
        if(node == first)
            return;
        if(node.prev != null)
            node.prev.next = node.next;
        if(node.next != null)
            node.next.prev = node.prev;
        if(last == node)
            last = node.prev;
        if(first != null)
        {
            node.next = first;
            first.prev = node;
        }
        first = node;
        node.prev = null;
        if(last == null)
            last = first;
    }

    private int cacheSize;
    private Hashtable nodes;
    private int currentSize;
    private CacheNode first;
    private CacheNode last;
}
