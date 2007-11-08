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

//NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference.

/*
 * Filename: FastStack.java	
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
 
/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/collection/FastStack.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:11 $
 */
 
package com.sun.enterprise.util.collection;

import java.util.ArrayList;


/**
* The Stack class represents a last-in-first-out (LIFO) stack of objects. The implementation is backed by
* java.util.ArrayList. Unlike java.util.Stack, it does not extend class Vector and hence 
* all methods in the stack are unsynchronized. The usual push and pop operations are provided, as 
* well as a method to peek at the top item on the stack,  a method to test for whether the stack is empty. 
* When a stack is first created, it contains no items. 
*/
public class FastStack {
    
    private ArrayList	stack;
    
    /**
    * Create a stack with no elements in it.
    */
    public FastStack() {
    	stack = new ArrayList(4);
    }
    
    /**
    * Tests if this stack is empty.
    * @return true if and only if this stack contains no items; false otherwise.
    */
    public boolean empty() {
    	return (stack.size() == 0);
    }
    
    /**
    * Tests if this stack is empty.
    * @return true if and only if this stack contains no items; false otherwise.
    */
    public boolean isEmpty() {
    	return (stack.size() == 0);
    }
    
    /**
    * Looks at the object at the top of this stack without removing it from the stack.
    * @return the object at the top of this stack (the last item of the Vector object).
    * @exception java.util.EmptyStackException if the stack is empty.
    */
    public Object peek() {
    	if (stack.size() == 0)
    		throw new java.util.EmptyStackException();
    	return stack.get(stack.size()-1);
    }
    
    /**
    * Pushes the object at the top of this stack.
    * @param object the item to be pushed into the stack.
    */
    public void push(Object object) {
    	stack.add(object);
    }
    
    /**
    * Pops the object that is at the top of this stack.
    * @return The object at the top of the stack
    * @exception java.util.EmptyStackException if the stack is empty.
    */
    public Object pop() {
    	if (stack.size() == 0)
    		throw new java.util.EmptyStackException();
    	return stack.remove(stack.size()-1);
    }
    
    /**
    * Gets the current size of the stack.
    * @return The number of entries in the stack.
    */
    public int size() {
    	return stack.size();
    }
        
}