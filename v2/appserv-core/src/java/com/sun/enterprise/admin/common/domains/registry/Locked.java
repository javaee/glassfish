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

import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ClassNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.nio.channels.FileChannel;
import java.io.EOFException;


class Locked implements LockingStore
{
  Locked(PersistentStore s, RandomAccessFile r){
	checkNotNull(s, "null store not allowed");
	checkNotNull(r, "null Random Access File not allowed");
	store = s;
	raf = r;
  }

  public long lastModified() {return 0L;}

	  /**
		 Read the object from the store.
		 <p>
		 precondition - true
		 <p>
		 postcondition - store has not been modified
		 @return the single object that was in the store (or null if
		 the store is empty). Releases all resources except
		 lock. State is unchanged.
		 @throws TimeoutException if a lock on the store couldn't be
		 obtained in a reasonable time.
		 @throws IOException if there was a problem reading from the
		 store. Release all resources. State is <code>unlocked</code>
		 @throws ClassNotFoundException if the object could not be
		 restored from the store. Release all resources. State is
		 <code>unlocked</code>
	  */
		 
  public Object readObject() throws IOException, ClassNotFoundException{
	try {
	  Object o = null;
	  ObjectInputStream ois = null;
	  FileInputStream fis = null;
	  try {
		fis = new FileInputStream(store.getStore());
		if (fis.available() > 0){
		  ois = new ObjectInputStream(fis);
		  o = ois.readObject();
		} else {
		  o = null;
		  fis.close();
		}
	  }
	  catch (EOFException e){	// this occurs if the file is empty
		o = null;
	  }
	  finally{
		if (fis != null){
		  fis.close();
		}
		if (ois != null){
		  ois.close();
		}
	  }
	  return o;
	}
	catch (IOException ioe){
	  unlock();
	  throw ioe;
	}
	catch (ClassNotFoundException cnfe){
	  unlock();
	  throw cnfe;
	}
	
  }
	  /**
		 Write the given object to the store via serialization.
		 <p>
		 Precondition - store is locked
		 <p>
		 Postcondition - store contains this object, and only this
		 object. All resources except lock are released. State is unchanged.
		 @param o the object to be put into the store. Must implement
		 {@link Serializable}
		 @throws IOException if there was a problem writing to the
		 store. All resources released. State is <code>unlocked</code>
		 @throws IllegalStateException if the store is not
		 locked. All resources released. State is <code>unlocked</code>
	  */

  public void writeObject(Object o) throws IOException, IllegalStateException{
	ObjectOutputStream oos = null;
	FileOutputStream fos = null;
	try {
	  oos = new ObjectOutputStream(new FileOutputStream(store.getStore(),
														false));
	  oos.writeObject(o);
	  oos.flush();
	}
	catch (IOException ioe){
	  this.unlock();
	  throw ioe;
	}
	catch (IllegalStateException ise){
	  this.unlock();
	  throw ise;
	}
	finally {
	  if (fos != null) fos.close();
	  if (oos != null) oos.close();
	}
  }
  
  public void lock() throws IOException, TimeoutException{}
  
  public void unlock(){
	try {
	  raf.close();
	}
	catch (IOException ioe){
		  // stomp on exceptions during closure - we don't care!
	}
	store.setState(new Unlocked(store));
  }

  protected void finalize() {
	this.unlock();
  }
  

  private void checkNotNull(Object o, String m) throws NullPointerException{
	if (o == null){
	  throw new NullPointerException(m);
	}
  }

  private PersistentStore store;
  private RandomAccessFile raf;
  
}



  
