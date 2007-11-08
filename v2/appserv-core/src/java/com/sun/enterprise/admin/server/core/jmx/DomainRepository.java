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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

//Logging imports
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

//JMX imports
import javax.management.ObjectName;

//Admin imports
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.CombinedPatternMatcher;
import com.sun.enterprise.admin.util.IPatternMatcher;
import com.sun.enterprise.admin.server.core.jmx.storage.PersistenceChecker;
import com.sun.enterprise.admin.server.core.jmx.storage.MBeanManufacturer;

/**
    An implementation of IRepository based on Hashtable. Encapsulates
    all the domain. One Hashtable is allocated per domain.
 
    @author Kedar Mhaswade
    @version 1.0
*/

public class DomainRepository implements IRepository
{
    private static final Logger sLogger = 
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    private Hashtable	mDomainTable	    = null;

    /** 
		Creates new Repository, with the default domain name from
		the ServiceName class. This will be the central repository of
		all MBeans for a particular domain.
    */
    
    public DomainRepository()
    {
		mDomainTable = new Hashtable();
    }
    
    /**
        Tests whether an Object with given ObjectName is present in this
        Repository.

        @param objectName - ObjectName of the Object to be searched for. ObjectName
                can't be a Pattern or PropertyPattern.
        @return true if the Object could be found, false otherwise.
    */
    
    public boolean contains(ObjectName objectName)
    {
		return ( find(objectName) != null );
    }
    
    /**
        Returns the number of Objects stored in this repository for
        given domain, the domain's name may not be null.

        @param domainName - the name of domain.
    */
    
    public int getCount(String domainName)
    {
	return ( ((Hashtable) mDomainTable.get(domainName)).size());
    }
    
    /**
        Returns the Object with given ObjectName. Returns null if there
        is no Object with given ObjectName. Passed ObjectName may not be null.
        @param objectName - ObjectName of the Object to be searched for.
        Note that ObjectName may not be a pattern for query or pattern
        on key properties.

        @return Object searched, null if there is none. Also returns null,
        if the ObjectName is pattern.
    */
    
    public Object find(ObjectName objectName)
    {
        Object match = null;
        
        if (objectName != null			&&
            ! objectName.isPattern()            &&
            ! objectName.isPropertyPattern())
        {
            Hashtable domain = findRepository(objectName);
            if (domain != null)
            {
                match = domain.get(objectName);
            }
        }
        return ( match );
    }
    
    /**
        Makes the check for the existence of corresponding element in the 
        persistent store. This method will also register or unregister
        the MBeans as required by adding/removing them, depending on its existence
        in the persistent store.
        @return MBean that is registered, null otherwise. In case of the MBean
        that exists in both MBeanRepository and persistent store simply the
        object in memory is returned.
    */
    public Object findPersistent(ObjectName objectName)
    {
        Object match = null, cachedMatch = null;
        if (objectName != null			&&
            ! objectName.isPattern()            &&
            ! objectName.isPropertyPattern())
        {
            Hashtable domain = findRepository(objectName);
            if (domain != null)
            {
                cachedMatch = domain.get(objectName);
            }
        }
        //sLogger.info("*In find: INFO: Now testing new lazyinit stuff....b4");
        boolean isMonitorMBean = ObjectNameHelper.isMonitorMBean(objectName);
        if (!isMonitorMBean) //don't check for monitorMBeans
        {
            match = findInPersistentStore(objectName, cachedMatch);
        } else {
            match = cachedMatch;
        }
        //sLogger.info("*In find:INFO: match from store = " + match);
        return ( match );
    }
    
    /**
        Adds an Object in the repository. Neither Object nor the ObjectName
        should be null. Given Object will not be added if an Object with the
        same ObjectName already exists in the repository.

        @param objectName - ObjectName of the Object to be stored.
        @param mbeanImpl - Object that is to be stored.

        @return true if the addition was successful, false otherwise.
    */
    
    public boolean add(ObjectName objectName, Object mbeanImpl)
    {
        Hashtable   domain	= findRepository(objectName);
        String	    domainName	= objectName.getDomain();
        boolean	    added	= false;

        boolean	    newDomainRequired = ( domain == null );

        if (newDomainRequired)
        {
                domain = addNewDomain(domainName);
                domain.put(objectName, mbeanImpl);
                added = true;
        }
        else
        {
                boolean noMatchFound = ( domain.get(objectName) == null );
                if (noMatchFound)
                {
                        domain.put(objectName, mbeanImpl);
                        added = true;
                }
        }
        return ( added );
    }
    
    /**
        Removes the Object with given ObjectName from repository. The passed
        ObjectName must not be null.

        @param objectName - ObjectName of the Object to be removed.
        @return true if the Object could be removed, false otherwise.
    */
    
    public boolean remove(ObjectName objectName)
    {
        Hashtable domain    = findRepository(objectName);
        boolean removed	    = false;

        if (domain != null)
        {
                Object mappedObject = domain.remove(objectName);
                if (mappedObject != null)
                {
                removed = true;
                }
        }
        return removed;
    }
  
    /**
        Finds the repository for the given objectName. Given objectName
        may not be null. This is helper method.

        @return the Hashtable for MBean with this objectName, null if
        there is no such Hashtable. Note that all the MBeans in hashtable for
        domain are having same domainName.
    */
    
    private Hashtable findRepository(ObjectName objectName)
    {
        String domainName   = objectName.getDomain();

        return ( (Hashtable) mDomainTable.get(domainName) );
    }
    
    private Hashtable addNewDomain(String domainName)
    {
        Hashtable newRepository = new Hashtable();
        mDomainTable.put(domainName, newRepository);
        return ( newRepository );
    }
	
    /**
        Returns the total number of MBeans stored in entire repository.

        @return int representing all MBeans in this repository.
    */
	
    public int getTotalCount()
    {
        int			count = 0;
        Iterator	domainNames = mDomainTable.keySet().iterator();
        while (domainNames.hasNext())
        {
                String    domainName	= (String) domainNames.next();
                Hashtable aTable		= (Hashtable)mDomainTable.get(domainName);
                count					= count + aTable.size();
        }
        return ( count );
    }
	
    /**
        Method to get ALL MBeans in all domains in this repository.
        The returned Set contains the ObjectNames of all MBeans.

        @return Set containing MBean ObjectNames, null if none exists.
    */
	
    public Set getAllMBeans()
    {
        Set mbeans = new HashSet();
        Iterator	domainNames = mDomainTable.keySet().iterator();
        while (domainNames.hasNext())
        {
                String    domainName	= (String) domainNames.next();
                Hashtable aTable		= (Hashtable)mDomainTable.get(domainName);
                mbeans.addAll(aTable.keySet());
        }
        return ( mbeans );
    }
	
    /**
        Returns a Set of Objects whose ObjectNames match zero or more Objects in the
        repository as per the pattern suggested by the argument. If the argument
        is NOT a pattern an exact match will be performed.

        @param objectName the ObjectName that may represent a pattern. The
        objectName may not be null.

        @return Set of all Objects that match the pattern in argument, null if
        there is no such object.
    */

    public Set query(ObjectName objectName)
    {
        Set				mbeans					= null;
        Set				allMBeans				= null;
        String			domainNamePattern		= null;
        Hashtable		propertyTable			= null;

        if (objectName != null)
        {
            mbeans					= new HashSet();
            if(!objectName.isPattern())
            {
                if(this.contains(objectName))
                {
                    mbeans.add(objectName);
                }
            }
            else
            {
                allMBeans				= this.getAllMBeans();
                domainNamePattern		= objectName.getDomain();
                propertyTable			= objectName.getKeyPropertyList();

                Iterator allMBeanIter	= allMBeans.iterator();
                while (allMBeanIter.hasNext())
                {
                    ObjectName	sample					= (ObjectName) allMBeanIter.next();
                    String		sampleDomainName		= sample.getDomain();	

                    boolean    domainNameMatches       = matchDomain(
                        domainNamePattern, sampleDomainName);
                    if (!domainNameMatches)
                    {
                        continue;
                    }

                    Hashtable	samplePropertyTable  	= sample.getKeyPropertyList();
                    boolean	sampleMatches = this.matchPropertiesWithPattern(propertyTable, samplePropertyTable);
                    if (sampleMatches)
                    {
                        mbeans.add(sample);
                    }
                }
            }
        }
        return ( mbeans );
    }
	
    private boolean matchPropertiesWithPattern(Hashtable pattern, Hashtable sample)
    {
        boolean currentMatch    = true;
        Iterator keyIter        = pattern.keySet().iterator();
        while (currentMatch  && keyIter.hasNext())
        {
            String key             = (String) keyIter.next();
            String patternVal      = (String) pattern.get(key);
            String sampleVal       = (String) sample.get(key);
            currentMatch = patternVal.equals(sampleVal);
        }

        return ( currentMatch );
    }

    
    private boolean matchDomain(String domainNamePattern, String testDomainName)
    {
        IPatternMatcher matcher = new CombinedPatternMatcher(domainNamePattern,
            testDomainName);
        
        return ( matcher.matches() );
    }
    /**
        This is where the check into persistent
        storage registry comes into picture. So first 
        check whether a Bean corresponding exactly to this
        objectName exists in the Config API. For now, this check
        is controlled by a boolean flag mDoLazyInit, which is
        set to true.
        All the MBeans that have corresponding config beans
        will be checked here. Only ServerController and GenericConfigurator
        won't be.
        This method operates on the given ObjectName and finds whether
        corresponding Bean exists in storage. It modifies the internal
        cached registry based on its findings. Thus note that this method
        indeed modifies the state of MBeanServer though it is only a finder.
        @param objectName ObjectName of the MBean to be found, may not be null.
        @param cachedObject Object reference to the corresponding MBean in 
        internal registry. This is accepted for the sake of optimization. If this
        is null, it means that the MBean is not in cache.
        @return the object found, null, if there is no such Object in persistent store.
    */

    private Object findInPersistentStore(ObjectName objectName, Object
            cachedObject)
    {
		//for Generic Configurator and ServerController, just return the passed
		// as these will NEVER be there in persistent store.
		String type = ObjectNameHelper.getType (objectName);
		if (type.equals(ObjectNames.kController)			||
			type.equals(ObjectNames.kGenericConfigurator))
		{
			return cachedObject;
		}
        PersistenceChecker      checker         = new PersistenceChecker();
        Object					storedObject = null;
        try {
            storedObject	= checker.findElement(objectName);
        } catch (Exception e)
        {
        }
        
        Object                  match           = null;
        if (storedObject != null)
        {
            if (cachedObject != null)
            {
                //sLogger.info("*In findInPersistentStore: stored-cached nonnull");
                /*
                    MBean is registered, cachedObject is in sync with storedObject,
                    no need to do anything. Most of the times, this is the case.
                    Hence simply return the cached object.
                */
                match = cachedObject;
            }
            else
            {
                /*
                    We will have to construct the proper Object
                    and register it as MBean. Will be invoked: for the
                    first time any MBean is invoked.
                */
				MBeanManufacturer producer = new MBeanManufacturer(objectName, storedObject);
				match = producer.createMBeanInstance();
				this.add(objectName, match);
                //sLogger.info("*In findInPersistentStore: " + 
                //"stored non null-cached null - constructing a new MBean " + 
                //match.getClass().getName() );
            }
        }
        else //Not found in the persistent store
        {
            match = null; // if not found in persistent store, just return null.
            if (cachedObject != null)
            {
                /* This means that the cached copy is stale and out of sync, remove it */
                this.remove(objectName);
                //sLogger.info("*In findInPersistentStore: stored is null, "
                //        + "cached is non-null, removed it");
            }
            else
            {
                //sLogger.info("*In findInPersistentStore: stored is null, " 
                //        + "cached is null, no prob");
                /* do nothing, as persistent store does not
                 have it and the cached store also does not have it */
            }
        }
        //Now return the stored object, be it null or non-null.
        return ( match );
    }
}
