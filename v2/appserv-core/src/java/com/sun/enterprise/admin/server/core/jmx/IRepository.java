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

package com.sun.enterprise.admin.server.core.jmx;

//JDK imports
import java.util.Set;

//JMX imports
import javax.management.ObjectName;

/**
    Defines the interface for a Repository of MBeans. Note that there is a 
    one to many relationship between an MBeanServer and Domains of MBeans.
    This interface abstracts out that relationship and hides the fact
    that in fact there can be multiple Repositories, one per Domain.
    @author Kedar Mhaswade
    @version 1.0
*/

public interface IRepository
{
    /**
		Adds an Object in the repository. Neither Object nor the ObjectName
		should be null. Given Object will not be added if an Object with the
		same ObjectName already exists in the repository.

		@param objectName - ObjectName of the Object to be stored.
		@param mbeanImpl - Object that is to be stored.

		@return true if the addition was successful, false otherwise.
    */
    public boolean add(ObjectName name, Object mbeanImpl);
    
    /**
		Removes the Object with given ObjectName from repository. The passed
		ObjectName must not be null.

		@param objectName - ObjectName of the Object to be removed.
		@return true if the Object could be removed, false otherwise.
    */
    public boolean remove(ObjectName objectName);
    
    /**
		Tests whether an Object with given ObjectName is present in this
		Repository.
		@param objectName - ObjectName of the Object to be searched for.
		@return true if the Object could be found, false otherwise.
		*/
    public boolean contains(ObjectName objectName);
    
    /**
		Returns the Object with given ObjectName. Returns null if there
		is no Object with given ObjectName. Passed ObjectName may not be null.
		@param objectName - ObjectName of the Object to be searched for.
		Note that ObjectName may not be a pattern for query or pattern
		on key properties.

		@return Object searched, null if there is none. Also returns null,
		if the ObjectName is pattern.
    */
    public Object find(ObjectName objectName);
    
    /**
        Makes the check for the existence of corresponding element in the 
        persistent store. This method will also register or unregister
        the MBeans as required by adding/removing them, depending on its existence
        in the persistent store.
        @return MBean that is registered, null otherwise. In case of the MBean
        that exists in both MBeanRepository and persistent store simply the
        object in memory is returned.
    */
    public Object findPersistent(ObjectName objectName);
    
    /**
		Returns the number of Objects stored in the repository for
		given domainName.
		@param domainName - String representing the name of domain.
    */
    public int getCount(String domainName);
	
	/**
		Returns the total number of MBeans stored in entire repository.
		
		@return int representing all MBeans in this repository.
	*/
	public int getTotalCount();
	
	/**
		Method to get ALL MBeans in all domains in this repository.
		The returned Set contains the ObjectNames of all MBeans.
	 
		@return Set containing MBean ObjectNames, null if none exists.
	*/
	public Set getAllMBeans();
	
	/**
		Returns a Set of Objects whose ObjectNames match zero or more Objects in the
		repository as per the pattern suggested by the argument. If the argument
		is NOT a pattern an exact match will be performed.
	 
		@param objectName the ObjectName that may represent a pattern.
		
		@return Set of all Objects that match the pattern in argument, null if
		there is no such object.
	*/
	public Set query(ObjectName objectName);
}