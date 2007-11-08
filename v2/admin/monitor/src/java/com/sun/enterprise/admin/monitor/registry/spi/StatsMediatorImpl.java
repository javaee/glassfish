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

/* StatsMediatorImpl.java
 * $Id: StatsMediatorImpl.java,v 1.6 2007/05/04 05:22:45 sankara Exp $
 * $Revision: 1.6 $
 * $Date: 2007/05/04 05:22:45 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.ArrayList;
import javax.management.MBeanAttributeInfo;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import com.sun.enterprise.admin.monitor.registry.StatsHolder;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.6 $
 */
class StatsMediatorImpl implements StatsMediator {
	
	private final Stats				delegate;
	private final Class				metaData;
	private final Map				methodMap;
	private final Map				firstParts;
	private final Map				secondParts;
	private final static Logger		logger = Logger.getLogger(AdminConstants.kLoggerName);
	private final static StringManager sm = StringManager.getManager(StatsMediatorImpl.class);
	private final String			DELIMITER = "-";
	private final String			OLD_DELIMITER = "_";
	// some JTA specific constants
	private Map						opsMap; 
	private final StatsDescriptionHelper helper = new StatsDescriptionHelper();
	private final String DESCRIPTION_GETTER = "getDescription";
    private static final String DOTTED_NAME = "dotted-name";

	public StatsMediatorImpl(Stats delegate, Class metaData) {
		/* the params are already checked in StatsHolderImpl class and this
		 * class is package-private, hence not checking the params.
		 * Subclasses should take a note of it. */
		this.delegate		= delegate;
		this.metaData		= metaData; //if null, there are no attributes in corresponding mbean
		this.methodMap		= new HashMap();
		this.firstParts		= new HashMap();
		this.secondParts	= new HashMap();
		reflectedAttributes();
		// special case handling of JTAStats
		if (isJtaMetaData())
			reflectJTAOps();
	}
	
	public Object getAttribute(String name) {
		if (! methodMap.containsKey(name)) {
			logger.finer("The name supplied may be an old-styled name, making one more attempt: " + name);
			name = getHyphenedName(name); //this means the name may be old-styled and we still need to support it.
			if (! methodMap.containsKey(name)) {
				final String msg = sm.getString("smi.no_such_attribute", name);
				throw new IllegalArgumentException (msg);
			}
		}
		return  ( invokeGetter(name) );
	}
	
	public MBeanAttributeInfo[] getAttributeInfos() {
		return ( attributes2Info() );
        
	}
	
	public Object invoke(String method, Object[] params, String[] sign) {
		// for now this will handle the special case of invoking on
		// the JTAStats only. Other Stats will be ignored.
		// the method name has been verified in the StatsHolderMBean
		logger.fine("Invoking Method: "+method);
		Object result = null;
		if(! opsMap.containsKey(method)) {
			final String msg = sm.getString("smi.no_such_method", method);
			throw new IllegalArgumentException(msg);
		}
		else
		{
			Method m = (Method)opsMap.get(method);			
			try {
				result = m.invoke(this.delegate, params);
			} catch(Exception e) {
				logger.info(e.getMessage());
			}
		}
		return result;
	}
	
	private void reflectedAttributes() {
		if (metaData == null)
			return;			//this means that this instance does not have any stats
		/* Note that only method names come from the class (metaData) and
		 * actual Method object should come from the Stats Object to which the
		 * getters will delegate. */
		final Method[] methods = metaData.getMethods();
		for (int i = 0; i < methods.length ; i++) {
			final String	method			= methods[i].getName();
			if (isStatsInterfaceMethod(method))
				continue; //ignore the methods from the super interface javax.management.j2ee.statistics.Stats
			final int		index			= method.indexOf("get");
			// a non-getXXX method name should not be processed any further
			// needed to support the ops in the JTAStats
			if(index != -1) {
				final String	baseAttrName	= method.substring(index + 3);
				final String[]	attrSubs		= getAttributeSubs(methods[i]);
				final Method actualMethod		= getMethodFromDelegate(method);
				addMapping(baseAttrName, attrSubs, actualMethod);
			}
		}
	}
	
	private void reflectJTAOps() {
		opsMap = new HashMap();
		final Method[] methods = metaData.getMethods();
		for(int i = 0; i < methods.length ; i++) {
			String methodName = methods[i].getName();
			if((StatsHolderMBeanImpl.JTA_FREEZE.equals(methodName)) || 
			   (StatsHolderMBeanImpl.JTA_UNFREEZE.equals(methodName)) || 
			   (StatsHolderMBeanImpl.JTA_ACTIVE_TRANSACTIONS.equals(methodName)) || 
			   (StatsHolderMBeanImpl.JTA_RUNTIME_RECOVERY_REQUIRED.equals(methodName)) || 
			   (StatsHolderMBeanImpl.JTA_ROLLBACK.equals(methodName))) {
				   
				opsMap.put(methodName, methods[i]);
			}
		}
	}
	
	/**
	 * Returns true if and only if the metaData is non null and is an instance
	 * of JTAStats interface. 
	 */
	private boolean isJtaMetaData() {
		boolean jta = false;
		if (metaData != null) {
			if (com.sun.enterprise.admin.monitor.stats.JTAStats.class.getName().equals(metaData.getName()))
				jta = true;
		}
		return ( jta );
	}
	private boolean isStatsInterfaceMethod(String name) {
		final Method[] methods = javax.management.j2ee.statistics.Stats.class.getMethods();
		boolean isInterfaceMethod = false;
		for (int i = 0 ; i < methods.length ; i++) {
			if (methods[i].getName().equals(name)) {
				isInterfaceMethod = true;
				break;
			}
		}
		return ( isInterfaceMethod );
	}
	private Method getMethodFromDelegate(String methodName) {
		final Method[] instanceMethods = delegate.getClass().getMethods();
		Method m = null;
		boolean matched = false;
		for (int i = 0 ; i < instanceMethods.length ; i++) {
			m = instanceMethods[i];
			if (methodName.equals(m.getName())) {
				matched = true;
				break;
			}
		}
		assert (matched != false) : "The Stats object: " + delegate.getClass().getName() + " does not implement declared method: " + methodName;
		return ( m );
	}
	private String[] getAttributeSubs(Method m) {
		final Class c = m.getReturnType();
		assert (javax.management.j2ee.statistics.Statistic.class.isAssignableFrom(c)) : "The method does not return a Statistic: " + m.getName();
		//assert (c.isInterface()) : "Has to be an interface: " + c.getName();
		final Method[] rets = c.getMethods();
		final String[] subs = new String[rets.length];
		for (int i = 0 ; i < rets.length ; i++) {
			final Method	am		= rets[i];
			final String	name	= am.getName();
			if (name.startsWith("get")) {
				subs[i] = name.substring(3); //String length of "get"
				logger.fine("return type = " + subs[i]);
			}
		}
		return ( subs );
	}
	private void addMapping(String first, String[] lasts, Method getter) {
		for (int i = 0 ; i < lasts.length ; i++) {
			String lc1 = null;
            if (first != null) {
                lc1 = first.toLowerCase();
            }
            if ( lasts[i] != null) {
                final String lc2 = lasts[i].toLowerCase();
                final String full = new StringBuffer(lc1).append(DELIMITER).append(lc2).toString();
                methodMap.put(full, getter);
                firstParts.put(lc1, first);
                secondParts.put(lc2, lasts[i]);
                logger.finer("Method: " + getter.getName() + " added for full attribute: " + full);
            }
		}
	}
	private Object invokeGetter(String ab) {
		//it is already checked if this is a valid attribute;
		String first	= ab.substring(0, ab.indexOf(DELIMITER));
		first			= (String)firstParts.get(first);
		final String fName	= "get" + first;
		String last	= ab.substring(ab.indexOf(DELIMITER) + 1);
		last		= (String)secondParts.get(last);
		final String lName	= "get" + last;
		if(lName.equalsIgnoreCase(DESCRIPTION_GETTER))
			return ((Object)getDescription(first));
	
		Method lastMethod = null;
		try {
			final Method firstMethod = (Method)methodMap.get(ab);
			final Object firstResult = firstMethod.invoke(delegate);
			lastMethod = firstResult.getClass().getMethod(lName);
			final Object value = lastMethod.invoke(firstResult);
			logger.finer("Got value for: " + ab + " as: " + value + " class = " + value.getClass().getName());
			return ( value );
		}
		catch(Exception e) {
			logger.throwing(StatsMediatorImpl.class.getName(), "invokeGetter", e);
			throw new RuntimeException (e);
		}
	}
	
	private MBeanAttributeInfo[] attributes2Info() {
		//go through all the attrs and build the MBeanAttributeInfo[]
		final Iterator it = methodMap.keySet().iterator();
        final ArrayList attrInfo = new ArrayList();

		int i = 0;
		while (it.hasNext()) {
			final String	name		= (String) it.next();
			final String	type		= getType(name);
			final String	desc		= getDescription(name);
			final boolean	isReadable	= getReadable(name);
			final boolean	isWritable	= false; //change for JTA
			final boolean	isIs		= false;
            attrInfo.add(new MBeanAttributeInfo(name, type, desc, isReadable, isWritable, isIs));
			logger.finer("Added the attribute to MBeanAttributeInfo: " + name);
		}
        // also add the dotted name as an attribute of the MBean
        MBeanAttributeInfo dottedNameInfo = new MBeanAttributeInfo(StatsHolderMBeanImpl.DOTTED_NAME,
                                                                   getType(DOTTED_NAME),
                                                                   getDescription(DOTTED_NAME),
                                                                   true,
                                                                   false,
                                                                   false);
        attrInfo.add(dottedNameInfo);
        final MBeanAttributeInfo[] ais = new MBeanAttributeInfo[attrInfo.size()];
        logger.finer("No of attrs = " + attrInfo.size());
		return (MBeanAttributeInfo[])attrInfo.toArray(ais);
	}
	
	private String getDescription(String name) {
		return helper.getDescription(name);
	}
	
	private String getType(String name) {
		return ( "java.lang.String" ); //will change later
	}
	
	private boolean getReadable(String name) {
		return ( true ); // will change later
	}
	
	/** Returns a name with hyphens and lower case characters.
	 * This method is there only to support the J2EE 1.4 SDK release which
	 * supported such names as HeapSize_Current. This method will return a String
	 * that is a modified form of the passed String. The call to this method
	 * occurs only when the given attribute is not found in the method map of
	 * this class.
	 */
	private String getHyphenedName(final String name) {
		return ( name.toLowerCase().replace(OLD_DELIMITER.charAt(0), DELIMITER.charAt(0)) );
	}
}
