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

import java.io.Serializable;
import java.util.HashMap;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.NoSuchElementException;


class Registry implements Serializable, Cloneable, DomainRegistryI
{
  TreeMap roots = new TreeMap();
  HashMap entries = new HashMap();

  public boolean isRegistered(String name){
	return roots.containsKey(name);
  }


  public void registerDomain(DomainEntry de) throws DomainRegistryException{
	if (isRegistered(de.getName())) {
	  throw new AlreadyRegisteredException(de.getName());
	}
	if (containsRoot(de.getRoot())){
	  throw new InvalidRootException("The root \""+de.getRoot()+"\" is already registered");
	}
	roots.put(de.getName(), de.getRoot());
	entries.put(de.getName(), de);
  }

 public void unregisterDomain(String domain_name) throws DomainRegistryException{
   if (!isRegistered(domain_name)){
	 throw new UnregisteredDomainException(domain_name);
   }
	delete(domain_name);
  }

  public void unregisterDomain(DomainEntry de) throws DomainRegistryException{
	unregisterDomain(de.getName());
  }
  public void reregisterDomain(DomainEntry de) throws DomainRegistryException {
	if (isRegistered(de.getName())) {
	  if (!roots.get(de.getName()).equals(de.getRoot())){
		throw new InvalidRootException("The given root ("+de.getRoot()+") of domain "+de.getName()+" doesn't match the already registered root for this domain");
	  }
	} else {
	  if (containsRoot(de.getRoot())){
		throw new InvalidRootException("The given root ("+de.getRoot()+") of domain "+de.getName()+" is already registered with a different domain");
	  }
	};

	entries.put(de.getName(), de);
  }

  public Iterator iterator() throws DomainRegistryException{
	return new RegistryIterator(this);
  }

  public boolean containsDomain(DomainEntry de) throws DomainRegistryException{
	return entries.values().contains(de);
  }
  public DomainEntry getDomain(String name) throws DomainRegistryException {
	return (DomainEntry) entries.get(name);
  }
  public int size(){
	return roots.size();
  }

  private boolean containsRoot(File root){
	return roots.containsValue(root);
  }

  private void delete(String name){
	roots.remove(name);
	entries.remove(name);
  }

	

  protected Object clone(){
	try {
	  Registry lhs = (Registry) super.clone();
	  lhs.roots = (TreeMap) this.roots.clone();
	  lhs.entries = (HashMap) this.entries.clone();
	  return lhs;
	}
	catch (CloneNotSupportedException cne){
	  return null;
	}
  }

  
  class RegistryIterator implements Iterator
  {
	Registry registry;
	Iterator iterator;
	  
	RegistryIterator(Registry r){
	  registry = (Registry) r.clone();
	  iterator = registry.roots.keySet().iterator();
	}
	public boolean hasNext(){
	  return iterator.hasNext();
	}
	public Object next() throws NoSuchElementException{
	  return entries.get((String) iterator.next());
	}
	public void remove() throws UnsupportedOperationException{
	  throw new UnsupportedOperationException();
	}
  }

}

