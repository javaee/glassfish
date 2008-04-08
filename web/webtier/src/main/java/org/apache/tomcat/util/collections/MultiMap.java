

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

import org.apache.tomcat.util.buf.MessageBytes;
import java.io.*;
import java.util.*;
import java.text.*;

// Originally MimeHeaders

/**
 * An efficient representation for certain type of map. The keys 
 * can have a single or multi values, but most of the time there are
 * single values.
 *
 * The data is of "MessageBytes" type, meaning bytes[] that can be
 * converted to Strings ( if needed, and encoding is lazy-binded ).
 *
 * This is a base class for MimeHeaders, Parameters and Cookies.
 *
 * Data structures: each field is a single-valued key/value.
 * The fields are allocated when needed, and are recycled.
 * The current implementation does linear search, in future we'll
 * also use the hashkey.
 * 
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public class MultiMap {

    protected Field[] fields;
    // fields in use
    protected int count;

    /**
     * 
     */
    public MultiMap(int initial_size) {
	fields=new Field[initial_size];
    }

    /**
     * Clears all header fields.
     */
    public void recycle() {
	for (int i = 0; i < count; i++) {
	    fields[i].recycle();
	}
	count = 0;
    }

    // -------------------- Idx access to headers ----------
    // This allows external iterators.
    
    /**
     * Returns the current number of header fields.
     */
    public int size() {
	return count;
    }

    /**
     * Returns the Nth header name
     * This may be used to iterate through all header fields.
     *
     * An exception is thrown if the index is not valid ( <0 or >size )
     */
    public MessageBytes getName(int n) {
	// n >= 0 && n < count ? headers[n].getName() : null
	return fields[n].name;
    }

    /**
     * Returns the Nth header value
     * This may be used to iterate through all header fields.
     */
    public MessageBytes getValue(int n) {
	return fields[n].value;
    }

    /** Find the index of a field with the given name.
     */
    public int find( String name, int starting ) {
	// We can use a hash - but it's not clear how much
	// benefit you can get - there is an  overhead 
	// and the number of headers is small (4-5 ?)
	// Another problem is that we'll pay the overhead
	// of constructing the hashtable

	// A custom search tree may be better
        for (int i = starting; i < count; i++) {
	    if (fields[i].name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Find the index of a field with the given name.
     */
    public int findIgnoreCase( String name, int starting ) {
	// We can use a hash - but it's not clear how much
	// benefit you can get - there is an  overhead 
	// and the number of headers is small (4-5 ?)
	// Another problem is that we'll pay the overhead
	// of constructing the hashtable

	// A custom search tree may be better
        for (int i = starting; i < count; i++) {
	    if (fields[i].name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the field at the specified position.  
     *
     * MultiMap will preserve the order of field add unless remove()
     * is called. This is not thread-safe, and will invalidate all
     * iterators. 
     *
     * This is not a frequent operation for Headers and Parameters -
     * there are better ways ( like adding a "isValid" field )
     */
    public void remove( int i ) {
	// reset and swap with last header
	Field mh = fields[i];
	// reset the field
	mh.recycle();
	
	fields[i] = fields[count - 1];
	fields[count - 1] = mh;
	count--;
    }

    /** Create a new, unitialized entry. 
     */
    public int addField() {
	int len = fields.length;
	int pos=count;
	if (count >= len) {
	    // expand header list array
	    Field tmp[] = new Field[pos * 2];
	    System.arraycopy(fields, 0, tmp, 0, len);
	    fields = tmp;
	}
	if (fields[pos] == null) {
	    fields[pos] = new Field();
	}
	count++;
	return pos;
    }

    public MessageBytes get( String name) {
        for (int i = 0; i < count; i++) {
	    if (fields[i].name.equals(name)) {
		return fields[i].value;
	    }
	}
        return null;
    }

    public int findFirst( String name ) {
        for (int i = 0; i < count; i++) {
	    if (fields[i].name.equals(name)) {
		return i;
	    }
	}
        return -1;
    }

    public int findNext( int startPos ) {
	int next= fields[startPos].nextPos;
	if( next != MultiMap.NEED_NEXT ) {
	    return next;
	}

	// next==NEED_NEXT, we never searched for this header
	MessageBytes name=fields[startPos].name;
        for (int i = startPos; i < count; i++) {
	    if (fields[i].name.equals(name)) {
		// cache the search result
		fields[startPos].nextPos=i;
		return i;
	    }
	}
	fields[startPos].nextPos= MultiMap.LAST;
        return -1;
    }

    // workaround for JDK1.1.8/solaris
    static final int NEED_NEXT=-2;
    static final int LAST=-1;

    // -------------------- Internal representation --------------------
    final class Field {
	MessageBytes name;
	MessageBytes value;

	// Extra info for speed
	
	//  multiple fields with same name - a linked list will
	// speed up multiple name enumerations and search.
	int nextPos;

	Field() {
	    nextPos=MultiMap.NEED_NEXT;
	}
	
	void recycle() {
	    name.recycle();
	    value.recycle();
	    nextPos=MultiMap.NEED_NEXT;
	}
    }
}
