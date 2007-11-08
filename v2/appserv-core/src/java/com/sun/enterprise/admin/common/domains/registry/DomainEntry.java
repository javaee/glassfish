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
import java.io.File;
import java.io.Serializable;
import java.io.IOException;


/**
   This class represents the data required to be registered for each
   domain so that invariants between domains can be maintained, and
   communication to domains established.
   <p>
   Instances of this class are immutable - they are simply data carriers.
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */


public final class DomainEntry implements Cloneable, Serializable
{

	  /**
		 Construct an immutable instance
		 @param name the name of the domain
		 @param root the root of the domain
		 @param contactData the data used to contact the domain
		 @throws NullPointerException if any argument is null
	  */
  public DomainEntry(String name, File root, ContactDataSet contactData) throws NullPointerException {
	if (name == null || root == null || contactData == null) {
	  throw new NullPointerException();
	}
	this.name = name;
	this.root = root;
        try
        {
	    this.path = root.getCanonicalPath();
        }
        catch(IOException ioe)
        {
	    this.path = root.getAbsolutePath();
        }	
	this.contactData = contactData;
  }

	  /**
		 Get the name of the receiver
		 @return the name of the receiver
	  */
  public String getName(){
	return this.name;
  }

	  /**
		 Get the root of the receiver
		 @return the root of the receiver
	  */
  public File getRoot(){
	return this.root;
  }

	  /**
		 Get the path of the receiver
		 @return the path of the receiver
	  */
  public String getPath(){
	return this.path;
  }

	  /**
		 Get the contact data of the receiver
		 @return the contact data of the receiver
	  */
  public ContactDataSet getContactData(){
	return this.contactData;
  }

  public Object clone() {
	try {
	  return super.clone();
	}
	catch (CloneNotSupportedException c){
	  return null;
	}
  }

  public int hashCode(){
	final String s = this.name+this.root.toString();
	return s.hashCode();
  }

  public boolean equals(Object rhs){
	return rhs != null && rhs instanceof DomainEntry && this.equals((DomainEntry) rhs);
  }

  private boolean equals(DomainEntry rhs){
	return rhs != null && this == rhs ||
	(this.name.equals(rhs.name) && this.root.equals(rhs.root));
  }

  private String name;
  private File root;
  private String path;
  private ContactDataSet contactData;
  
}
