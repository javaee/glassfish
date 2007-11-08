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
import java.lang.ClassNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.io.FileInputStream;
import java.io.EOFException;


class Unlocked implements LockingStore
{

  private PersistentStore storeImpl;
  
  Unlocked(PersistentStore s){
	if (s == null) {
	  throw new NullPointerException("store is null");
	}
	
	storeImpl = s;
  }

  public synchronized Object readObject() throws IOException, TimeoutException, ClassNotFoundException {
	ObjectInputStream in = null;
	Object o = null;
	try{
	  in = getIn();
	  o = (in != null ? in.readObject() : null) ;
	}
	catch (EOFException e){
	  o = null;					// this occurs if file is empty
	}
	finally {
	  if (in != null) {
		in.close();
	  }
	}
	return o;
  }

  public long lastModified(){ return 0L ;}
  public void unlock(){}
  

  public void writeObject(Object o) throws IllegalStateException, IOException, TimeoutException {
	throw new IllegalStateException("Unlocked state - writeObject() not allowed");
	
  }

  public void lock() throws TimeoutException, IOException {
	final RandomAccessFile raf = new RandomAccessFile(storeImpl.getLock(),
													  "rws");
	getWriteLock(raf.getChannel());
	storeImpl.setState(new Locked(storeImpl, raf));
  }

  private ObjectInputStream getIn() throws IOException {
	final FileInputStream fis = new FileInputStream(storeImpl.getStore());
	getReadLock(fis.getChannel());
	if (fis.available() > 0){
	  return new ObjectInputStream(fis);
	} else {
	  fis.close();
	  return null;
	}
  }

  private FileLock getReadLock(FileChannel ch) throws TimeoutException, IOException {
	return obtainLock(ch, true);
  }

	  /**
		 get a write lock on the given channel
	  */
  private FileLock getWriteLock(FileChannel ch) throws TimeoutException, IOException {
	return obtainLock(ch, false);
  }

    private FileLock obtainLock(FileChannel channel, boolean shared) throws TimeoutException, IOException{
	FileLock lock = null;
	int attempts = 0;
	try{
/** Using 0x7fffffff in place of Long.MAX_VALUE. Bug # 4719967,4719605 **/
	  while ((lock = channel.tryLock(0L, 0x7fffffff, shared)) == null && attempts++ < ATTEMPTS){
		Thread.currentThread().sleep(TIMEOUT);
	  }
	}
	catch (InterruptedException e){
	  throw new TimeoutException();
	}
	
	if (lock == null) {
	  throw new TimeoutException();
	}
	return lock;
  }

  	  // # of milliseconds to wait between attempts to obtain the file
	  // # lock
  private static final int TIMEOUT = 3; // 3 X 10^^-3 = 0.003 S
  

	  //# of times to attempt to obtain the file lock
  private static final int ATTEMPTS = 50;

}


  
