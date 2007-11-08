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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.management.Notification;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Superclass of all admin events. This is the marker object for all events
 * in administration.
 */
public class AdminEvent extends Notification implements Cloneable {

    private EventKey eKey = null;
    
    private static long eventCounter = 0;

    /** unresolved/abstract target destination */
    private String targetDest;

    /** 
     * Resolved destination based on target destination. This is 
     * where the notification event is delivered. Target destination 
     * always refers to concrete server end point.
     */
    private String effectiveDest;

    /** 
     * Number of times this event is forwarded from one server instance
     * to another server.  
     */
    private int hops = 0;

    /** maximum number of times an event is allowed to be forwarded */
    private static final int MAX_HOPS = 3;

    /**
     * Event type is required for all sub-classes of Notification. It is a
     * string representation for the event using dotted notation (for example -
     * network.alarm.router)
     */
    static final String eventType = AdminEvent.class.getName();

    /**
     * Config context. This object provides access to a snapshot of config
     * updated with all changes in this event at the time event processing
     * starts on the instance to which the event applies.
     */
    transient private ConfigContext configContext;

    /**
     * Old config context. The object provides access to config context before
     * applying changes from this event.
     */
    transient private ConfigContext oldConfigContext;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminEvent.class );

    /**
     * List of config changes.
     */
    ArrayList configChangeList;

    /**
     * List of dependent config changes.
     */
    List dependentChangeList;

    /**
     * Create a new admin event for specified ias instance.
     * @param instanceName name of ias instance
     */
    public AdminEvent(String instanceName) {
        this(eventType, instanceName);
    }

    /**
     * Create a new admin event for specified ias instance.
     * @param eventType type of the event
     * @param instanceName name of ias instance
     */
    public AdminEvent(String eventType, String instanceName) {
        super(eventType, instanceName, ++eventCounter,
                System.currentTimeMillis());
    }

    protected AdminEvent(String type, Object source,
                         long seqNumber, long time) {
        super(type, source, seqNumber, time);
    }

    /**
     * Return name of the instance to which this event applies. (This
     * interpretation is slightly different from EventObject.getSource()
     * where it is expected to return the object on which event happened -
     * so instead of the instance object it returns the name of the instance.)
     * @return name of the instance to which event applies
     */
    public Object getSource() {
        return super.getSource();
    }

    /**
     * Return name of the instance to which this event applies. The difference
     * from getSource() is that this returns a String object, so there is no
     * need to cast the return value.
     * @see getSource()
     * @return name of the instance to which the event applies
     */
    public String getInstanceName() {
        return (String)getSource();
    }

    /**
     * Set config context to specified value. Event listeners can use this
     * to get a consistent view of config. This config context should contain
     * all changes from this event.
     * @param ctx the config context
     */
    void setContext(ConfigContext ctx) {
        configContext = ctx;
    }

    /**
     * Set old config context to specified value. This context will not have
     * changes from this event.
     * @param ctx the old config context
     */
    void setOldContext(ConfigContext ctx) {
        oldConfigContext = ctx;
    }

    /**
     * Get config context. This returns a snapshot of config context with all
     * changes in this event at the time event processing started on the
     * receiving server instance. Event listeners should use this object to get
     * a consistent view of context instead of system wide context because that
     * can be updated during event processing. This method will return null if
     * called before the event processing has started. So, it is intended for
     * use by event listeners only.
     * @return the snapshot of config context.
     */
    public ConfigContext getConfigContext() {
        return configContext;
    }

    /**
     * Get old config context. This returns a reference of config context prior
     * to applying any changes from this event. This method will return null if
     * called before event processing has started. This is just a reference to
     * global config context at the time event processing started. Even though
     * this is not a snapshot of config context (i.e. cloned copy), it is still
     * sufficient for the usage in getting old config values, because once a
     * context has been made global it is immutable.
     */
    public ConfigContext getOldConfigContext() {
        return oldConfigContext;
    }

    /**
     * Return a String representation.
     */
    public String toString() {
        int numChg = (configChangeList == null) ? 0 : configChangeList.size();
        return this.getClass().getName() + " -- " + this.getInstanceName()
                + " [" + numChg + " Change(s), Id:" + this.getSequenceNumber()
                + ", ts:" + this.getTimeStamp() + "]";
    }

    /**
     * Get detailed event information. Unless overidden in sub classes, this
     * method returns exactly same value as concatenation of toString() and
     * getConfigChangeInfo() methods. The intent of this method is to return a
     * detailed string representation of the event that can be used for
     * debugging.
     */
    public String getEventInfo() {
        return toString() + getConfigChangeInfo();
    }

    /**
     * Get config change info. This method returns a string that lists config
     * changes associated to this event. Returns an empty string if no changes
     * are associated to this event.
     */
    public String getConfigChangeInfo() {
        StringBuffer buf = new StringBuffer();
        if (configChangeList != null) {
            Iterator iter = configChangeList.iterator();
            while (iter.hasNext()) {
                ConfigChange change = (ConfigChange)iter.next();
                buf.append(change.toString());
            }
        }
        return buf.toString();
    }

    /**
     * Add specified change to the event.
     * @param change the change to add to this event
     */
    synchronized void addConfigChange(ConfigChange change) {
        assertNotNull(change);
        if (configChangeList == null) {
            configChangeList = new ArrayList();
        }
        configChangeList.add(change);
    }

    /**
     * Add specified changes to the event.
     * @param changeList the list of changes to add to this event
     */
    public synchronized void addConfigChange(ArrayList changeList) {
        if (changeList == null) {
			String msg = localStrings.getString( "admin.event.null_configchangelist" );
            throw new IllegalArgumentException( msg );
        }
        if (configChangeList == null) {
            configChangeList = new ArrayList();
        }
        configChangeList.addAll(changeList);
    }

    public synchronized void addDependentConfigChange(List list) {
        if (list == null) {
            String msg = localStrings.getString( "admin.event.null_configchangelist" );
            throw new IllegalArgumentException( msg );
        }

        if (dependentChangeList == null) {
            dependentChangeList = new ArrayList();
        }
        dependentChangeList.addAll(list);
    }

    public List getDependentChangeList() {
        return dependentChangeList;
    }

    public ArrayList getConfigChangeList() {
        return configChangeList;
    }

    /**
     * Remove specified config change from the event.
     * @param change the change to add to this event
     */
    synchronized void removeConfigChange(ConfigChange change) {
        assertNotNull(change);
        if (configChangeList != null) {
            int ndx = configChangeList.indexOf(change);
            if (ndx != -1) {
                configChangeList.remove(ndx);
            }
        }
    }

    /**
     * Is this event a no-op. The default implementation always returns false.
     * However, the sub-classes can override and provide a more intelligent
     * implementation.
     * @return true if the event is a no op, false otherwise.
     */
    boolean isNoOp() {
        return false;
    }

    /**
     * Assert that specified ConfigChange is not null.
     * @throws IllegalArgumentException if specified ConfigChange is null.
     */
    // Replace this by JDK 1.4 assert
    private void assertNotNull(ConfigChange change) {
        if (change == null) {
			String msg = localStrings.getString( "admin.event.null_configchange" );
            throw new IllegalArgumentException( msg );
        }
    }

    /**
     * Each event will have 2 destinations, target and effective destination
     * target destination would be for example cluster1. effective destination
     * would be endpoints of target destination.
     *
     * @return name of the target destination 
     */
    public String getTargetDestination() {
        return targetDest;
    }

    /**
     * Sets the target destination
     *
     * @param tarDest target destination info
     */
    public void setTargetDestination(String tarDest) {
        targetDest = tarDest;
    }

    /**
     * Gets the effective destination for this event
     *
     * @return name of the effective destination
     */
    public String getEffectiveDestination() {
        return effectiveDest;
    }

    /**
     * Sets the effective destination
     * 
     * @param eDest effective destination info
     */
    public void setEffectiveDestination(String eDest) {
        effectiveDest = eDest;
    }

    /**
     * Set event key information
     */
    public void setEventId(EventKey ek) {
        eKey = ek;
     }

    /**
     * Get event key information
     */
    public EventKey getEventId() {
        return eKey;
    }

    /**
     * Returns a top level clone of this object.
     *
     * @return  a clone of this object
     */
    public Object clone() throws CloneNotSupportedException { 
        return super.clone();
    } 

    /** 
     * Returns the current hop count (number of times this event has been 
     * forwarded) of this event.
     *
     * @return  current hop count
     */
    public int getHopCount() {
        return hops;
    }

    /**
     * Increments the current hop count.
     *
     * @return incremented hop count
     */
    public int incrementHopCount() {
        return ++hops;
    }

    /**
     * Returns true if current hop count is less than or equal to maximum 
     * hop allowed for this event.
     *
     * @return true if current hop count is less than or equal to maximum
     *         hop allowed
     */
    public boolean isValidHopCount() {
        return (hops <= MAX_HOPS) ? true : false;
    }

    // sub classes of AdminEvent, override the following two method
    // to provide appropriate action codes.

    /**
     * Get action type for this event.
     */
    public int getActionType() {
        return 0;
    }

    /**
     * Set action to specified value. If action is not one of allowed,
     */
    private void setAction(int action) {
        return;
    }

}
