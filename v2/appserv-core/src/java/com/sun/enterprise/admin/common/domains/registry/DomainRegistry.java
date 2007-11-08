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

import java.util.Iterator;
import java.util.HashMap;
import java.io.Serializable;
import java.io.File;
import java.lang.UnsupportedOperationException;
import java.util.NoSuchElementException;
import java.io.IOException;
import java.io.EOFException;

/**
   This class provides a registry for domains, abstracting the
   persistent storage and coordination activities required to permit
   uniqueness invariants to be maintained between domains no matter
   which JVM they are being referred from.
   <p>
   The directory in which the persistent store is kept is denoted by
   the <code>System</code> property
   <code>com.sun.aas.admin.configRoot</code>. The name of the
   file is <code>domains.ser</code>
   <p>
   The principal invariants this registry maintains is that no two
   domains shall have the same name, and no two domains shall have the
   same location.
   <p>
   The registry can be considered to be a table whose keys are domain
   name and domain location, and whose values are the remote contact
   information by which the adminstration server within that domain
   can be contacted remotely.
   <p>
   A row in this table is abstracted into the {@link DomainEntry} class. 
   *
   * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
   * @version $Revision: 1.4 $
   */
public class DomainRegistry implements DomainRegistryI
{

		/**
		 * Return an instance of the DomainRegistry
		 * @return a <code>DomainRegistry</code> value
		 @throws DomainRegistryException if there was a problem
		 instantiating the class
		 */
  synchronized static public DomainRegistry newInstance() throws DomainRegistryException {
	if (instance == null){
	  instance = new DomainRegistry();
	  instance.init();
	}
	return instance;
  }

	public synchronized void registerDomain(DomainEntry de) throws DomainRegistryException {
		prepareForUpdate();
		try {
		  registry.registerDomain(de);
		}
		catch (DomainRegistryException e){
		  store.unlock();
		  throw e;
		}
		saveRegistry();
	}

	public void unregisterDomain(String domain_name) throws DomainRegistryException {
	  prepareForUpdate();
	  try{
		registry.unregisterDomain(domain_name);
	  }
	  catch (DomainRegistryException e){
		store.unlock();
		throw e;
	  }
	  saveRegistry();
	  
	}

	public void unregisterDomain(DomainEntry de) throws DomainRegistryException {
	  this.unregisterDomain(de.getName());
	}

	public void reregisterDomain(DomainEntry de) throws DomainRegistryException {
	  prepareForUpdate();
	  try {
		registry.reregisterDomain(de);
	  }
	  catch (DomainRegistryException e){
		store.unlock();
		throw e;
	  }
	  saveRegistry();

	}
  
	public boolean containsDomain(DomainEntry de) throws DomainRegistryException {
	  refreshRegistry();
	  return registry.containsDomain(de);
	}

	public DomainEntry getDomain(String name) throws DomainRegistryException {
	  refreshRegistry();
	  return registry.getDomain(name);
	}

  public Iterator iterator() throws DomainRegistryException {
	refreshRegistry();
	return registry.iterator();
  }
  
	public int size() throws DomainRegistryException {
	  refreshRegistry();
	  return registry.size();
	}
	

	synchronized void reset() throws IOException {
	  store.unlock();
	  registry = null;
	  store = null;
	  instance = null;
	}

  private void refreshRegistry() throws DomainRegistryException {
	if (lastModified < store.lastModified()){
	  try {
		registry = getRegistryFromStore();
	  }
	  catch (Exception te){
		throw new DomainRegistryException("problem reading from store", te);
	  }
	}
  }

  private Registry getRegistryFromStore() throws IOException, TimeoutException, ClassNotFoundException {
	Registry br;
	br = (Registry) store.readObject();
	return (br != null ? br : new Registry());
  }
  

  private void prepareForUpdate() throws DomainRegistryException {
	try {
	  store.lock();
	}
	catch (Exception e){
	  throw new DomainRegistryException("problem locking store ", e);
	}
	refreshRegistry();
  }

  private void saveRegistry() throws DomainRegistryException {
	try {
	  store.writeObject(registry);
	  store.unlock();
	  lastModified = store.lastModified();
	}
	catch (Exception e){
	  e.printStackTrace();
	  throw new DomainRegistryException("couldn't save registry", e);
	}
  }
  
  
  private void init()  throws DomainRegistryException {
	try {
	  store = LockingStoreFactory.getInstance();
	  registry = getRegistryFromStore();
	}
	catch (Exception e){
	  throw new DomainRegistryException("couldn't initialize registry. Error message: "+ e.getMessage());
	}
  }

/*
  PersistentStore getPS(){
	return (PersistentStore) store;
  }
 */
 
  private Registry registry;
  private LockingStore store;
  private static DomainRegistry instance = null;
  private long lastModified = 0;
  
  private DomainRegistry(){}

}
