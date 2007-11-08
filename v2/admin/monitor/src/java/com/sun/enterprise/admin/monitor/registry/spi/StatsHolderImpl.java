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

/* StatsHolderImpl.java
 * $Id: StatsHolderImpl.java,v 1.3 2005/12/25 03:43:36 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:36 $
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

import com.sun.enterprise.admin.monitor.registry.*;
import javax.management.j2ee.statistics.*;
import javax.management.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collection;
import java.util.logging.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
/**
 * Creates and holds a hierarchy of Objects that represent various dependent 
 * components that require monitoring
 * @author  Shreedhar Ganapathy<mailto:shreedhar.ganapathy@sun.com>
 */
public class StatsHolderImpl implements StatsHolder {
    private final String name;
    private final Hashtable children;
    private final MBeanServer server;
	private Class statsClass = null;
	private String statsClassName = null;
	/* these can be set after constructing the object */
    MonitoredObjectType type = null;
    Stats stats = null;
    ObjectName objectName = null;
	String dottedName = null;
	/* these can be set after constructing the object */
	private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName); //better way
	private static final StringManager sm = StringManager.getManager(StatsHolderImpl.class);
    /** Creates a new instance of StatsHolderImpl */
	public StatsHolderImpl(String name) {
		this.name = name;
		this.children = new Hashtable();
		this.server = findMBeanServer();
		logger.fine("StatsHolderImpl initialized, name = " + name);
	}
	
    public StatsHolderImpl(String name, MonitoredObjectType type) {
		this(name);
		this.setType(type);
    }
    
    /**
     * finds and returns an MBeanServer instance
     */
    private MBeanServer findMBeanServer() {
        MBeanServer server = null;
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty()){
            server = (MBeanServer)servers.get(0);
        }
        return server;
    }
    
    /**
     * Add a child node or leaf to this node.
     * @param statsHolder
     */    
    private StatsHolder addChild(StatsHolder sh) {
		// The name of the child is the unique key
		assert (sh != null) : "Null StatsHolder to be added";
        if(!children.containsKey(sh.getName())){
            children.put(sh.getName(), sh);
			logger.fine("StatsHolder.addChild: New key created with statsHolder key name = " + sh.getName() + " type = " + sh.getType().toString());
        }
		else {
			sh = this.getChild(sh.getName());
			logger.fine("Not adding, StatsHolder.addChild: Child exists, name = " + sh.getName() + " type = " + sh.getType().toString());
			//Ideally an exception should be thrown
		}
		return ( sh );
    }
    
    /**
     * Add a child node or leaf to this node
     * @param name
     * @param type
     * @return StatsHolder
     */    
    public StatsHolder addChild(String name, MonitoredObjectType type) {
		assert ( name != null && type != null) : "Asked to add a null name-type child";
        StatsHolder child = this.getChild(name);
		if (child == null) 
			child = this.addChild(new StatsHolderImpl(name, type));
		
		return ( child );
    }

    /**
     * return an array of StatHolder objects each representing a child
     * of this node.
     * @return Collection
     */ 
    public Collection getAllChildren() {
        return children.values();
    }

    /**
     * removes all children belonging to this node.
     */
     public void removeAllChildren() {
		 //removes all the children, grandchildren etc. recursively and deregisters mbeans
		 if (children.isEmpty()) {
			 this.unregisterMBean();
		 }
		 else {
			 final Enumeration e = children.elements();
			 while (e.hasMoreElements()) {
				 final StatsHolder sh = (StatsHolder)e.nextElement();
				 sh.removeAllChildren();
			 }
			 children.clear();
		 }
     }

    /**
     * Remove a child node or leaf from this node.
     * @param name
     * @param type
     */    
    public void removeChild(String name) {
        children.remove(name);
    }

    /**
     * Returns name of this hierarchical node
     */    
    public String getName(){
        return name;
    }

    /**
     * Returns type of this hierarchical node
     */    
    public MonitoredObjectType getType(){
        return type;
    }

    /**
     * sets this hierarchical node's associated stats object. Used when node was
     * originally created without a Stats implementation or a new monitoring 
     * level has been set requiring a new Stats registration
     */    
    public void setStats(Stats stats) {
	    this.stats=stats;
    }
    
	public Stats getStats() {
		return ( this.stats );
	}
    /**
     * sets the hierarchically denoted dotted name for this node.
     */
    public void setObjectName(ObjectName name) {
        this.objectName = name;
    }
    
    public ObjectName getObjectName(){
        return objectName;
    }

	public void setDottedName(String dottedName) {
		this.dottedName = dottedName;
	}
	
	public String getDottedName() {
		return dottedName;
	}
	
    /**
     * registers a monitoring MBean with the MBeanServer
     */
    public void registerMBean() {
        try {
			if (server.isRegistered(this.objectName)) {
				logger.fine("ObjectName obj is already registered - this is an error, ignoring for now : " + this.objectName);
				return;
			}
            //final DynamicMBean mbean = MonitoringMBeansFactory.generateMonitoringMBean(stats);
            //server.registerMBean(mbean, this.objectName);
			final StatsHolderMBean mbean = new StatsHolderMBeanImpl(this);
			server.registerMBean(mbean, this.objectName);
			logger.finer("Registered the MBean for this StatsHolder: " + this.objectName);
        }
		catch(Exception e) {
			logger.fine("SH.registerMBean: Exception for object-name " + this.objectName);
			logger.throwing(StatsHolderImpl.class.getName(), "registerMBean()", e);
			//squelching it for now - throw RuntimeException
        }
    }
    
    /**
     * unregisters a monitoring MBean from the MBean Server
     */
    public void unregisterMBean() {
        String msg = null;
        try {
			if (server.isRegistered(this.objectName)) {
				server.unregisterMBean(this.objectName);
				logger.fine("SH.unregisterMonitoringMBean(): unregistered - " + objectName);
			}
			else {
				logger.fine("SH.unregisterMonitoringMBean(): never registered - so not unregistering" + objectName);
			}
        }
		catch(Exception e){
			logger.fine("SH.unregisterMBeanUnregistration failed, objectName = " + objectName);
			//squelching for now - throw RuntimeException
        }
    }
    
	public void setType(MonitoredObjectType type) {
		this.type = type;
	}
	
	public StatsHolder getChild(String name) {
		final StatsHolder child = (StatsHolder) children.get(name);
		if (child == null) {
			logger.fine("SH.getChild - child is null with name = " + name);
		}
		return ( child );
	}	
	
	/** this is just a convenience - debugging method */
	void write() {
		//new DumpThread().start();
	}
	
	public Class getStatsClass() {
		return ( this.statsClass );
	}
	
	public void setStatsClass(Class c) {
		if (! javax.management.j2ee.statistics.Stats.class.isAssignableFrom(c)) {
			final String msg = sm.getString("sh.not_a_stats_interface", c.getName());
			throw new IllegalArgumentException(msg);
		}
		//the given class has to be an interface.
		if (! c.isInterface()) {
			final String msg = sm.getString("sh.should_be_an_interface", c.getName());
			throw new IllegalArgumentException(msg);
		}
		
		this.statsClass = c;
		this.statsClassName = c.getName();
	}
	
	public String getStatsClassName() {
		return ( this.statsClassName );
	}
	
	public void setStatsClassName(String cName) {
        Class c = null;
        try {
            c = Class.forName(cName);
        } catch (ClassNotFoundException cnfe) {
            final String msg = sm.getString("invalidclassname.statsholder",
            cName);
            throw new IllegalArgumentException(msg);
        }

		if (! javax.management.j2ee.statistics.Stats.class.isAssignableFrom(c)) {
			final String msg = sm.getString("sh.not_a_stats_interface", c.getName());
			throw new IllegalArgumentException(msg);
		}
		//the given class has to be an interface.
		if (! c.isInterface()) {
			final String msg = sm.getString("sh.should_be_an_interface", c.getName());
			throw new IllegalArgumentException(msg);
		}
		
		this.statsClass = c;
		this.statsClassName = cName;
	}

	private class DumpThread extends Thread {
		public void run() {
			while (true) {
				logger.fine("Start Element: " + name);
				if (children.isEmpty()) {
					logger.fine("Leaf Element");
				}
				else {
					final Enumeration e = children.elements();
					while (e.hasMoreElements()) {
						final StatsHolderImpl c = (StatsHolderImpl) e.nextElement();
						c.write();
					}
				}
				logger.fine("End Element: " + name);
				try {
					Thread.sleep(5000);
				}
				catch(InterruptedException e) {}
			}
		}
	}
}
