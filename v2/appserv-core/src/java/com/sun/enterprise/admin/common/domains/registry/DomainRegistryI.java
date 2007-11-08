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
public interface DomainRegistryI
{
  
		/**
		   Register the given domain information.
		   <p>
		   Precondition - the domain entry's name and location are unique
		   within the registry
		   <p>
		   Postcondition - the entry is registered under the domain
		   entry's name within the registry. 
		   @param de the domain entry to be registered. The name and
		   location of this entry must be unique within the registry.
		   @throws NullPointerException if de is null
		   @throws DomainRegistryException if there's a problem with
		   registering the domain entry.
		*/

  void registerDomain(DomainEntry de) throws DomainRegistryException;
  
		/**
		   Remove the registration of the domain with the given name.
		   <p>
		   Precondition - a domain is registered within the registry
		   with the given name
		   <p>
		   Postcondition - the registry contains no entry for the given
		   name
		   @param domain_name the name of the domain to be unregistered
		   @throws NullPointerException if the domain_name is null
		   @throws DomainRegistryException if the domain_name is unknown
		   with the registry, or if there's a problem in deleting the
		   registry entry
		*/
  void unregisterDomain(String domain_name) throws DomainRegistryException;
  
		/**
		   Remove the registration of the given domain entry.
		   <p>
		   Precondition - a domain with the same name and location as
		   the given domain is registered in the registry
		   <p>
		   Postcondition - the registry contains no entry for the given
		   name
		   @param de the domain entry to be unregistered
		   @throws NullPointerException if de is null
		   @throws DomainRegistryException if de is not registered
		   within the registry, or if there's a problem in deleting the
		   registry entry
		*/
  void unregisterDomain(DomainEntry de) throws DomainRegistryException;
  		/**
		   Re-register the given domain entry with the registry.
		   <p>
		   Precondition - a domain with the same name and location as
		   the given domain entry is already registered in the
		   registry or no domain is registered which has either the
		   same name or the same location (or both).
		   <p>
		   Postcondition - the domain entry registered against the given
		   domain entry's name is the given domain entry.
		   @param de the domain entry to be re-registered
		   @throws DomainRegistryException if no domain with the given
		   domain entry's name is registered, or if there are other
		   problems with updating the registry
		*/
  void reregisterDomain(DomainEntry de) throws DomainRegistryException;
  		/**
		   Obtain an iterator over the entries in the
		   registry. The entries will appear in alphabetically sorted
		   order by name. This iterator will operate on a snapshot of the
		   registry obtained at the time this method is executed -
		   changes to the registry during the lifetime of the iterator returned from
		   this method will not affect the iterator.
		   @return an iterator (whose contents are all instances of
		   {@link DomainEntry}
		   @throws DomainRegistryException if there's a problem in
		   obtaining the iterator.
		*/
  Iterator iterator() throws DomainRegistryException;
  		/**
		   Indicate if the registry contains the given entry.
		   <p>
		   precondition - entry is not null
		   <p>
		   postcondition - registry has not been modified
		   @param de the entry whose presence in the registry is to be determined
		   @return true iff the entry is in the registry
		   @throws DomainRegistryException if there's a problem in accessing the registry
		*/
  boolean containsDomain(DomainEntry de) throws DomainRegistryException;
  		/**
		   Return the domain entry given a domain entry name.
		   <p>
		   precondition - the given name is not null
		   <p>
		   postcondition - the registry has not been modified
		   @param name - the name of the domain whose entry is to be obtained
		   @return DomainEntry from the registry whose name is that
		   given. Returns null if no match.
		   @throws DomainRegistryException if there's a problem accessing the
		   registry
		*/
  DomainEntry getDomain(String name) throws DomainRegistryException;
  		/**
		   Get the number of entries in the registry.
		   @return Return the number of items in the registry
		   @throws DomainRegistryException if there's a problem
		   accessing the registry
		*/
  int size() throws DomainRegistryException;
}
