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

package com.sun.enterprise.admin.common.domains.registry;
import java.util.HashSet;
import java.util.Collection;
/**
 * This class represents a collection of {@link ContactData}
 * elements. It is a set that guarantees that its elements are
 * instances of {@link ContactData}
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version 1.0
 */
public class ContactDataSet extends HashSet
{

  public ContactDataSet(){
	super();
  }


	  /**
		 Construct a new instance, adding all the members from the
		 given collection.
		 <p>
		 Precondition - all members of the collection are instances of
		 the {@link ContactData} class
		 <p>
		 postcondition - the returned instance contains all the
		 members of the given collection, and only those members
		 @param c the given collection
		 @throws NullPointerException if c is null
		 @throws ClassCastException if one of the members of c is not
		 an instance of the {@link ContactData} class
	  */
  public ContactDataSet(Collection c) throws NullPointerException, ClassCastException{
	super(c);
  }

  public ContactDataSet(int initialCapacity){
	super(initialCapacity);
  }

  public ContactDataSet(int initialCapacity, float loadFactor){
	super(initialCapacity, loadFactor);
  }

	  /**
		 Add the given object to the receiver.
		 <p>
		 precondition - the given object is an instance of the {@link
		 ContactData} class
		 <p>
		 postcondition - the receiver contains the given object
		 @param o the object to be added
		 @return true iff the receiver changed as a result of adding
		 this object
		 @throws NullPointerException if the object is null
		 @throws ClassCastException if the object is not an instance
		 of {@link ContactData} class
	  */
  public boolean add(Object o) throws NullPointerException, ClassCastException{
	return super.add((ContactData) o);
  }
  
}
