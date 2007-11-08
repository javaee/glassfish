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

/* StatsHolderMBeanImpl.java
 * $Id: StatsHolderMBeanImpl.java,v 1.6 2007/05/04 05:22:45 sankara Exp $
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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanRegistration;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import javax.management.MBeanRegistrationException;

import com.sun.enterprise.admin.monitor.registry.StatsHolder;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Provides the implementation for an MBean that represents a node to give statistical
 * data in the form of its attributes.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.6 $
 */
class StatsHolderMBeanImpl implements DynamicMBean, StatsHolderMBean, MBeanRegistration {
	
	private static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	private static StringManager sm = StringManager.getManager(StatsHolderMBeanImpl.class);
	private final StatsHolder	delegate;
	private final StatsMediator	mediator;
	private DottedNameRegistrar registrar;
	private MBeanInfo mi;
	private int state;
	private Object lock = new Object();
	public static final int INITIALIZED		= 0;
	public static final int REGISTERED		= 1;
	public static final int MBEANINFO_DONE	= 2;
	public static final String JTA_FREEZE = "freeze";
	public static final String JTA_UNFREEZE = "unfreeze";
	public static final String JTA_ROLLBACK = "rollback";
	public static final String JTA_ACTIVE_TRANSACTIONS = "listActiveTransactions";
	public static final String JTA_RUNTIME_RECOVERY_REQUIRED = "isRecoveryRequired";
    public static final String DOTTED_NAME = "dotted-name";

	
	StatsHolderMBeanImpl(StatsHolder delegate) {
		assert (delegate != null);
		this.delegate	= delegate;
		this.mediator	= new StatsMediatorImpl(delegate.getStats(), delegate.getStatsClass());
		changeState(INITIALIZED);
	}
	
	public Object getAttribute(String name) throws AttributeNotFoundException, 
	MBeanException, ReflectionException {
        if(name.equals(DOTTED_NAME))
            return delegate.getDottedName();
        else
            return ( mediator.getAttribute(name) );
	}
	
	public AttributeList getAttributes(String[] names) {
		final AttributeList list = new AttributeList();
		for (int i = 0 ; i < names.length ; i++) {
            try {
				final Attribute a = new Attribute(names[i], this.getAttribute(names[i]));
                list.add(a);
			}
            catch(Exception e) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Error while accessing an attribute named: " + names[i]);
                }
                //The exception SHOULD BE squelched per the contract of this method
            }
		}
		return ( list );
	}
	
	public MBeanInfo getMBeanInfo() {
		synchronized(lock) {
			if (state == MBEANINFO_DONE) {
				return mi;
			}
		}
		build();
		changeState(MBEANINFO_DONE);
		return ( mi );
	}
	
	private void build() {
		final String			name		= StatsHolderMBeanImpl.class.getName();
		final String			desc		= getDescription();
		final MBeanAttributeInfo[] mais		= mediator.getAttributeInfos();
		final MBeanConstructorInfo[] mcis	= this.getConstructorInfos();
		final MBeanOperationInfo[] mois		= this.getOperationInfos();
		final MBeanNotificationInfo[] mnis	= this.getNotificationInfos();
		mi = new MBeanInfo(name, desc, mais, mcis, mois, mnis);
	}
	
	private String getDescription() {
		return "StatsHolder MBean for: " + StatsHolderMBeanImpl.class.getName();
	}
	
	private MBeanConstructorInfo[] getConstructorInfos() {
		final MBeanConstructorInfo[] cis = new MBeanConstructorInfo[0];
		return ( cis ); //we don't want management applications to create instances of this MBean
	}
	private MBeanOperationInfo[] getOperationInfos() {
		
		final ArrayList opInfo = new ArrayList();

		opInfo.add(getChildrenInfo());
		opInfo.add(getNameInfo());
		opInfo.add(getTypeInfo());
		// Add the additional ops only for StatsHolders that have an actual Stats object
		// associated with them
		if(delegate.getStats() != null) {
			opInfo.add(getStatisticNameInfo());
			opInfo.add(getStatsInfo());
		}
		
		MBeanOperationInfo[] mos = new MBeanOperationInfo[opInfo.size()];
		mos = (MBeanOperationInfo[])opInfo.toArray(mos);
		
		// if we are dealing with JTAStats, we need to add the additional
		// operations freeze, unfreeze & rollback to the MBeanOerationInfo
		if(isJta())
			return (getJTAOperationInfo(mos));
		
		return ( mos );
	}
	
	public Object invoke(String method, Object[] params, String[] sign) throws 
	MBeanException, ReflectionException {
		if ("getChildren".equals(method)) {
			return ( this.getChildren() );
		} else if ("getName".equals(method)) {
			return (this.getName());
		} else if ("getType".equals(method)) {
			return (this.getType());
		} else if ("getStatisticNames".equals(method)) {
			return (this.getStatisticNames());
		} else if ("getStatistics".equals(method)) {
			return (this.getStatistics());
		} else if(isJTAMethod(method)) {
			return(mediator.invoke(method, params, sign));
		} else {
			final String msg = sm.getString("smi.no_such_method", method);
                        final Exception ae = new UnsupportedOperationException(msg);
			throw new MBeanException(ae);
		}
	}
	
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, 
	InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new UnsupportedOperationException("NYI");
	}
	
	public AttributeList setAttributes(AttributeList attributes) {
		throw new UnsupportedOperationException("NYI");
	}
	
	public ObjectName[] getChildren() {
		final Collection c			= delegate.getAllChildren();
		final ObjectName[] names	= new ObjectName[c.size()];
		final Iterator it			= c.iterator();
		int i = 0;
		while (it.hasNext()) {
			final StatsHolder s = (StatsHolder) it.next();
			names[i++] = s.getObjectName();
		}
		assert (names.length == i) : "Sizes don't match";
		return ( names );
	}
	
	private MBeanOperationInfo getChildrenInfo() {
		final MBeanOperationInfo info = new MBeanOperationInfo(
			"getChildren",
			"Gets the children of this StatsHolder",
			null,
			ObjectName[].class.getName(),
			MBeanOperationInfo.INFO
		);
		return ( info );
	}
	
	private MBeanNotificationInfo[] getNotificationInfos() {
		final MBeanNotificationInfo[] mns = new MBeanNotificationInfo[0];
		return ( mns );
	}
	
	private boolean isJta() {
		boolean isJta = false;
		final Class cl = delegate.getStatsClass();
		if (cl != null) {
			if (com.sun.enterprise.admin.monitor.stats.JTAStats.class.getName().equals(cl.getName())) {
				isJta = true;
			}
		}
		return ( isJta);
	}
	private boolean isJTAMethod(String methodName) {
		return ((JTA_FREEZE.equals(methodName)) || 
		       (JTA_UNFREEZE.equals(methodName)) || 
		       (JTA_ACTIVE_TRANSACTIONS.equals(methodName)) || 
		       (JTA_RUNTIME_RECOVERY_REQUIRED.equals(methodName)) || 
			   (JTA_ROLLBACK.equals(methodName))); 
	}
	
	//Implementation of MBeanRegistration - start	
	public void postDeregister() {		
	}
	
	public void postRegister(Boolean registered) {
		if (registered.equals(Boolean.TRUE)) {
			registrar.registerDottedName(delegate.getDottedName(),
				delegate.getObjectName());
			changeState(REGISTERED);
		}
	}
	
	public void preDeregister() throws Exception {
		registrar.unregisterDottedName(delegate.getDottedName());		
	}
	
	public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws 
	Exception {
		this.registrar = new DottedNameRegistrar(mBeanServer);
		return objectName;
	}
	//Implementation of MBeanRegistration - end	

	private void changeState(int to) {
		synchronized(lock) {
			state = to;
		}
	}
	
	private MBeanOperationInfo[] getJTAOperationInfo(MBeanOperationInfo[] mos) {
		ArrayList opInfo = new ArrayList();
		for(int i = 0; i < mos.length ; i++)
			opInfo.add(mos[i]);
		// not performing any reflection for now, as it is assumed that 
		// only 3 methods will be added to the MBeanOperationInfo and 
		// their names and signatures are fixed.
		MBeanOperationInfo mInfo = new MBeanOperationInfo(JTA_FREEZE,
		                                                  "Freezes the transaction service",
														  null,
														  void.class.getName(),
														  MBeanOperationInfo.ACTION);
	
		opInfo.add(mInfo);
		
		mInfo = new MBeanOperationInfo(JTA_UNFREEZE,
		                               "Unfreezes the transaction service",
										null,
										void.class.getName(),
										MBeanOperationInfo.ACTION);
		
		opInfo.add(mInfo);
        mInfo = new MBeanOperationInfo(JTA_ROLLBACK,
		                               "Rollsback a given transaction",
										new MBeanParameterInfo[] {
		                                    new MBeanParameterInfo("txnId", 
											                       String.class.getName(), 
																   "Id of the transaction to be rolled back"
																   )},
										void.class.getName(),
										MBeanOperationInfo.ACTION);
        opInfo.add(mInfo);
        mInfo = new MBeanOperationInfo(JTA_ACTIVE_TRANSACTIONS,
		                               "Gets Active Transactions in a Map",
                                        null,
										List.class.getName(),
										MBeanOperationInfo.ACTION_INFO);
        opInfo.add(mInfo);
        mInfo = new MBeanOperationInfo(JTA_RUNTIME_RECOVERY_REQUIRED,
                                       "Returns if the recovery is required",
                                       null,
                                       Boolean.class.getName(),
                                       MBeanOperationInfo.ACTION_INFO);
        opInfo.add(mInfo);
		MBeanOperationInfo[] jtaOpInfo = new MBeanOperationInfo[opInfo.size()];
		return (MBeanOperationInfo[])opInfo.toArray(jtaOpInfo);
		
	}
	
	public String getName() {
		return delegate.getName();
	}
	
	public String getType() {
		return delegate.getType().getTypeName();
	}
	
	private MBeanOperationInfo getNameInfo() {
		MBeanOperationInfo mInfo = new MBeanOperationInfo("getName",
		                                                  "Gets the name of this StatsHolder",
														  null,
														  String.class.getName(),
														  MBeanOperationInfo.INFO);
		return mInfo;
	}
	
	private MBeanOperationInfo getTypeInfo() {
		MBeanOperationInfo mInfo = new MBeanOperationInfo("getType",
		                                                  "Gets the type of this StatsHolder",
														  null,
														  String.class.getName(),
														  MBeanOperationInfo.INFO);
		return mInfo;
	}
	
	public String[] getStatisticNames() {
        Stats stats = delegate.getStats();
        if (stats != null) {
            return stats.getStatisticNames();
        } else {
            return null;
        }
	}
	
	public Statistic[] getStatistics() {
        Stats stats = delegate.getStats();
        if (stats == null) {
            return null;
        }

		Statistic[] statArray = stats.getStatistics();
		boolean isSerializable = checkSerializability(statArray);
		if(isSerializable) {
			final Statistic[] hackedArray = StatisticWorkaround.populateDescriptions(statArray);
			return hackedArray;
		}
		else 
			return null;
	}
	
	private MBeanOperationInfo getStatisticNameInfo() {
		MBeanOperationInfo mInfo = new MBeanOperationInfo("getStatisticNames",
		                                                  "Gets the names of all the statistics in the given Stats Object", 
														  null, 
														  String[].class.getName(),
														  MBeanOperationInfo.INFO);
		return mInfo;
	}
	
	private MBeanOperationInfo getStatsInfo() {
		MBeanOperationInfo mInfo = new MBeanOperationInfo("getStatistics", 
		                                                  "returns the results of all the getXXX methods, in the given Stats object",
														  null, 
														  Statistic[].class.getName(), 
														  MBeanOperationInfo.INFO);
		return mInfo;
	}
	
	private boolean checkSerializability(Object[] objArray) {
		boolean isSerializable = true;
		for(int i = 0; i < objArray.length; i++) {
			isSerializable = (isSerializable) && (objArray[i] instanceof java.io.Serializable);
		}
		return isSerializable;
	}
	
}
