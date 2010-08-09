/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * TokenValueSet.java
 *
 * Created on March 6, 2003, 12:00 PM
 */

package com.sun.enterprise.admin.util;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import com.sun.enterprise.admin.util.TokenValue;

/** Represents the Set of TokenValue objects.
 *
 * @author  kedar
 */

public class TokenValueSet implements Cloneable {
    
    private final Set<TokenValue> values;
    
    public TokenValueSet() {
        values = new HashSet<TokenValue>();
    }
    
    public TokenValueSet(final Set<TokenValue> values) {
        //if (!isTokenValueSet(values)) {
          //  throw new IllegalArgumentException("Invalid set");
        //}
        this.values = new HashSet<TokenValue>();
        this.values.addAll(values);
    }
    
    public void add(final TokenValue tokenValue) {
        boolean added = this.values.add(tokenValue);
    }
    
    public void addAll(final Set<TokenValue> more) {
        this.values.addAll(more);
    }
    
    public void remove(final TokenValue tokenValue) {
        this.values.remove(tokenValue);
    }
    
    public void clear() {
        this.values.clear();
    }
    
    public Iterator<TokenValue> iterator() {
        return ( this.values.iterator() );
    }
    
    public boolean isEmpty() {
        return ( this.values.isEmpty() );
    }
    
    public Object[] toArray() {
        return ( this.values.toArray() );
    }
    
    public int size() {
        return ( this.values.size() );
    }
    
    public Object clone() throws CloneNotSupportedException {
        return ( super.clone() );
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator<TokenValue> iter = this.iterator();
        while(iter.hasNext()) {
            TokenValue tv = iter.next();
            buf.append(tv.toString());
            buf.append(System.getProperty("line.separator"));
        }
        return ( buf.toString() );
    }
}
