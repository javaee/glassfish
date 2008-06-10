/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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



package org.apache.tomcat.util.collections;

import java.util.logging.*;

/**
 * Simple object pool. Based on ThreadPool and few other classes
 *
 * The pool will ignore overflow and return null if empty.
 *
 * @author Gal Shachor
 * @author Costin Manolache
 */
public final class SimplePool  {

    private static Logger log = Logger.getLogger(SimplePool.class.getName());

    /*
     * Where the threads are held.
     */
    private Object pool[];

    private int max;
    private int last;
    private int current=-1;
    
    private Object lock;
    public static final int DEFAULT_SIZE=32;
    static final int debug=0;
    
    public SimplePool() {
	this(DEFAULT_SIZE,DEFAULT_SIZE);
    }

    public SimplePool(int size) {
	this(size, size);
    }

    public SimplePool(int size, int max) {
	this.max=max;
	pool=new Object[size];
	this.last=size-1;
	lock=new Object();
    }

    public  void set(Object o) {
	put(o);
    }

    /**
     * Add the object to the pool, silent nothing if the pool is full
     */
    public  void put(Object o) {
	synchronized( lock ) {
	    if( current < last ) {
		current++;
		pool[current] = o;
            } else if( current < max ) {
		// realocate
		int newSize=pool.length*2;
		if( newSize > max ) newSize=max+1;
		Object tmp[]=new Object[newSize];
		last=newSize-1;
		System.arraycopy( pool, 0, tmp, 0, pool.length);
		pool=tmp;
		current++;
		pool[current] = o;
	    }
	    if( debug > 0 ) log("put " + o + " " + current + " " + max );
	}
    }

    /**
     * Get an object from the pool, null if the pool is empty.
     */
    public  Object get() {
	Object item = null;
	synchronized( lock ) {
	    if( current >= 0 ) {
		item = pool[current];
		pool[current] = null;
		current -= 1;
	    }
	    if( debug > 0 ) 
		log("get " + item + " " + current + " " + max);
	}
	return item;
    }

    /**
     * Return the size of the pool
     */
    public int getMax() {
	return max;
    }

    /**
     * Number of object in the pool
     */
    public int getCount() {
	return current+1;
    }


    public void shutdown() {
    }
    
    private void log( String s ) {
        if (log.isLoggable(Level.FINE))
	    log.fine("SimplePool: " + s );
    }
}
