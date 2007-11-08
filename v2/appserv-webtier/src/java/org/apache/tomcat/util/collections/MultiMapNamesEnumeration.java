

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

/** Enumerate the distinct header names.
    Each nextElement() is O(n) ( a comparation is
    done with all previous elements ).

    This is less frequesnt than add() -
    we want to keep add O(1).
*/
public final class MultiMapNamesEnumeration implements Enumeration {
    int pos;
    int size;
    String next;
    MultiMap headers;

    // toString and unique options are not implemented -
    // we allways to toString and unique.
    
    /** Create a new multi-map enumeration.
     * @param  headers the collection to enumerate 
     * @param  toString convert each name to string 
     * @param  unique return only unique names
     */
    MultiMapNamesEnumeration(MultiMap headers, boolean toString,
			     boolean unique) {
	this.headers=headers;
	pos=0;
	size = headers.size();
	findNext();
    }

    private void findNext() {
	next=null;
	for(  ; pos< size; pos++ ) {
	    next=headers.getName( pos ).toString();
	    for( int j=0; j<pos ; j++ ) {
		if( headers.getName( j ).equalsIgnoreCase( next )) {
		    // duplicate.
		    next=null;
		    break;
		}
	    }
	    if( next!=null ) {
		// it's not a duplicate
		break;
	    }
	}
	// next time findNext is called it will try the
	// next element
	pos++;
    }
    
    public boolean hasMoreElements() {
	return next!=null;
    }

    public Object nextElement() {
	String current=next;
	findNext();
	return current;
    }
}
