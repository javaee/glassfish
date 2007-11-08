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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import com.sun.enterprise.admin.common.AdminResponse;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Admin Event Result objects are used to communicate result of admin event
 * notification.
 *
 * AdminEventResult object can be either used to represent composite 
 * result of multiple instances.
 *
 * getResultCode() returns one of the valid result codes either ERROR,
 * SUCCESS or others like TRANSMISSION_ERROR, RESTART_NEEDED etc. This
 * is composite result of all event's recipients' results. In order to get
 * individual recipient's result getResultCodes() can be used. It returns
 * hash map of instance name and result code.
 *
 * If some recipients got the notification and the others did not receive
 * the notification or failed to process it for any reason.The composite result
 * would be MIXED_RESULT.
 *
 * Similarly hash map of messages and exceptions can be obtained using
 * getMessages and getExceptions respectively.
 *
 * All the methods (setters/getters) of AdminEventResult (except setResultCode 
 * and getResultCode) take instance name as argument.
 *
 * Helper methods are also available to format the results and
 * messages/exceptions into a string format. Please look at 
 * getAllResultCodesAsString() and getAllMessagesAsString()
 * for details.
 *
 * This class is not thread safe and usage from AdminEventMulticaster does
 * not require it to be thread safe. The methods add/remove Attribute do
 * synchronize internally but that might not be sufficient.
 */
public class AdminEventResult implements Serializable {

    /**
     * Constant denoting Serializable class
     */
    private static final Class SERIALIZABLE = Serializable.class;

    /**
     * Constant denoting - restart of server required.
     */
    public static final String RESTART_NEEDED = "restart";

    /**
     * Constant denoting - runtime exception while processing event
     */
    public static final String RUNTIME_EXCEPTION = "runtime_exception";

    /**
     * Constant denoting - runtime error while processing event
     */
    public static final String RUNTIME_ERROR = "runtime_error";

    /**
     * Constant denoting - event transmission error
     */
    public static final String TRANSMISSION_ERROR = "transmission_error";

    /**
     * Constant denoting - error while processing event in listener
     */
    public static final String LISTENER_ERROR = "listener_error";

    /**
     * Constant denoting - error while creating snapshot of config context
     */
    public static final String CONFIG_SNAPSHOT_ERROR = "config_snapshot_error";

    /**
     * Constant denoting - MBean not found.
     */
    public static final String MBEAN_NOT_FOUND = "mbean_not_found";

    /**
     * Constant denoting - MBean attribute not found. This is used to denote
     * invalid attribute name for Monitoring GET command.
     */
    public static final String MBEAN_ATTR_NOT_FOUND = "mbean_attr_not_found";

    /**
     * Constant denoting - successful event processing
     */
    public static final String SUCCESS = "success";

    /**
     * Constant denoting - some events failed and some events passsed
     */
    public static final String MIXED_RESULT  = "mixed_result";

    /**
     * Constant denoting - that event notification was not successful, 
     *   resultCodes needs to checked for individual status 
     */
    public static final String ERROR  = "Event did not reach any recipient";

    private static HashMap resultList = new HashMap();

    private long eventId;
    private String resultCode;

    private HashMap resultCodes = new HashMap();
    private HashMap allMessages = new HashMap();
    private HashMap allExceptions = new HashMap();
    private HashMap allAttributes = new HashMap();

    // Set this, when an AdminEventListenerException is added to this result
    private AdminEventListenerException firstAle =null;

    // Set this, when an Throwable is added to this result
    private Throwable firstThr =null;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminEventResult.class );

    /**
     * Create a new admin event result for specified event id.
     */
    // FIX - Review whether public constructor is required, there is a
    // package factory method.
    public AdminEventResult(long eventId) {
        this.eventId = eventId;
        resultCode = SUCCESS;
    }

    /**
     * Get event id
     */
    public long getEventId() {
        return eventId;
    }

    /**
     * Get result code
     */
    public String getResultCode() {
        return resultCode;
    }

    /**
     * Get result codes, used in clustered environment
     */
     public HashMap getResultCodes() {
        return resultCodes;
     }

    /**
     * Get all event messages, used in clustered environment
     */
     public HashMap getMessages() {
        return allMessages;
     }

    /**
     * Get all event messages for an event sent to a target
     */
     public Collection getMessages(String target) {
        return (Collection)allMessages.get(target);
     }

    /**
     * Get all event exceptions occured during the event delivery. 
     * This method should be used in clustered environment
     */
     public HashMap getExceptions() {
        return allExceptions;
     }

    /**
     * Get all event exceptions occured during the event delivery. 
     * This method should be used in clustered environment
     */
     public Collection getExceptions(String target) {
        return (Collection)allExceptions.get(target);
     }

    /**
    /**
     * Set result code to specified value
     * WARNING: This should be used for setting indiviual instance 
     * result code.
     */
    public void setResultCode(String resultCode) {
        if (!RESTART_NEEDED.equals(this.resultCode)) {
            this.resultCode = resultCode;
        }
    }

    /**
     * Return all the result codes as a String. Note that the Returned
     * String can be really big. Returns a non null empty string if there
     * are no messages. Never returns a null.
     */
    public String getAllResultCodesAsString() {
        StringBuffer sb = new StringBuffer();
        // Process messages array list
        Set resultCodeSet = resultCodes.entrySet();

        Iterator iter = resultCodeSet.iterator();

        while (( iter != null) && (iter.hasNext())) {
            Entry nextMapEntry = (Entry) iter.next();
            sb.append(localStrings.getString("admin.event.target_string"));
            sb.append(nextMapEntry.getKey());
            sb.append("            ");
            sb.append(localStrings.getString("admin.event.result_code_string"));
            sb.append(nextMapEntry.getValue());
            sb.append("            ");
        }
        return (sb.toString());
    }

    /**
     * Return all the messages as a String. Note that the Returned
     * String can be really big. Returns a non null empty string if there
     * are no messages. Never returns a null.
     */
    public String getAllMessagesAsString() {
        StringBuffer sb = new StringBuffer();
        // Process messages array list
        Set messageSet = allMessages.entrySet();

        Iterator iter = messageSet.iterator();

        while (( iter != null) && (iter.hasNext())) {
            Entry nextMapEntry = (Entry) iter.next();
            sb.append(localStrings.getString("admin.event.target_string"));
            sb.append(nextMapEntry.getKey());
            sb.append("            ");
            Collection msgCol = (Collection) nextMapEntry.getValue();
            Iterator msgs = null;
            if ( msgCol != null) {
                msgs = msgCol.iterator();
            }
            int msgCount = 0;
            if (msgs != null) {
                while (msgs.hasNext()) {
                    msgCount++; 
                    sb.append(localStrings.getString(
                       "admin.event.msg_string", new Integer(msgCount).toString()));
                    String msg = (String) msgs.next();
                    sb.append(msg);
                    sb.append("            ");
                }
            }
        }
        // Process exceptions array list
        Set exceptionSet = allExceptions.entrySet();

        iter = exceptionSet.iterator();
        while ( (iter != null) && (iter.hasNext())) {
            Entry nextMapEntry = (Entry) iter.next();

            Collection excsCol = (Collection) nextMapEntry.getValue();
            Iterator excs = null;
            if ( excsCol != null) {
                excs = excsCol.iterator();
            }

            if (excs !=null) {
                sb.append(localStrings.getString("admin.event.target_string"));
                sb.append(nextMapEntry.getKey());
                sb.append("            ");
                int excCount = 0;
                while ( excs.hasNext()) {
                    Throwable tt = (Throwable) excs.next();
                    excCount++;
                    if (tt != null) {
                        sb.append(localStrings.getString(
                           "admin.event.exp_string", 
                           new Integer(excCount).toString()));
                        String nextStr = tt.getMessage();
                        sb.append(nextStr);
                        sb.append(System.getProperty("line.separator"));
                        StringWriter sw = new StringWriter();
                        tt.printStackTrace(new PrintWriter(sw));
                        sb.append(sw.toString());
                        sb.append(System.getProperty("line.separator"));
                    }
                }
            }
        }
        return (sb.toString());
    }

    /**
     * Add another message
     */
    public void addMessage(String target, String message) {
        Collection msgs = (Collection)allMessages.get(target);
        if ( msgs == null) {
            msgs = new ArrayList();
            allMessages.put(target, msgs);
        }
        msgs.add(message);
    }

    /**
     * Add an exception to the result.
     * @param tt the exception to add.
     */
    public void addException(String target, Throwable tt) {
        Collection excs = (Collection)allExceptions.get(target);
        if (excs == null) {
            excs = new ArrayList();
            allExceptions.put(target, excs);
        }
        excs.add(tt);
        if ((firstAle == null) && (tt instanceof AdminEventListenerException)) {
            firstAle = (AdminEventListenerException)tt;
        }
        if (firstThr == null) {
            firstThr = tt;
        }
    }


    /**
     * This method returns the first exception added to AdminEventResult
     * on behalf of any target. 
     *
     * @return AdminEventListenerException    It is the first added
     *                                        AdminEventListenerException to this
     *                                        AdminEventResult.
     */
    public AdminEventListenerException getFirstAdminEventListenerException() {
        return firstAle;
    }

    /**
     * This method returns the first throwbale added to AdminEventResult
     * on behalf of any target. 
     *
     * @return Throwable                      It is the first added Throwable
     *                                         to this AdminEventResult.
     */
    public Throwable getFirstThrowable() {
        return firstThr;
    }

    /**
     * Add specified attribute. This method can be used to add any
     * Serializable object to event result. The method throws
     * IllegalArgumentException, if name is null or the value is not
     * Serializable.
     * @param target name of the target for this event
     * @param name name of the attribute
     * @param value value of the specified attribute
     * @throws IllegalArgumentException if name is null or value is not
     *    Serializable.
     */
    void addAttribute(String target, String name, Object value) {
        attributeCheck(name, value);
        synchronized (allAttributes) {
            HashMap attributes = (HashMap) allAttributes.get(target);
            if ( attributes == null ) {
                attributes = new HashMap();
                allAttributes.put(target, attributes);
            }
            attributes.put(name, value);
        }
    }

    void attributeCheck(String name, Object value) {
        if (name == null) {
			String msg = localStrings.getString( "admin.event.null_attribute_name" );
            throw new IllegalArgumentException( msg );
        }
        if (value != null) {
            if (!SERIALIZABLE.isInstance(value)) {
				String msg = localStrings.getString( "admin.event.value_not_serializable" );
                throw new IllegalArgumentException( msg );
            }
        }
    }

    /**
     *  Returns all the attributes for a target as a map
     *
     * @return attributes hash map
     */
    public HashMap getAttributes(String target) {
        return (HashMap) allAttributes.get(target);
    }

    /**
     * Get names of all attributes for a target.The returned Set is empty if 
     * there are no attributes associated to this event result, 
     * otherwise it contains String objects representing names of all attributes.
     *
     * @param target  name of the target
     *
     * @return a set of all attribute names.
     */
    public Set getAttributeNames(String target) {
        HashMap h = (HashMap) allAttributes.get(target);
        if ( h!=null) {
            return h.keySet();
        } else {
            return null;
        }
    }

    /**
     * Get value of attribute with specified name.
     * @param name name of the attribute
     * @throws IllegalArgumentException if name is null.
     * @return Value of the specified attribute name, if it exists, null,
     *     otherwise. A null return value may also mean that a null value
     *     was associated with specified name.
     */
    public Object getAttribute(String target, String name) {
        if (name == null) {
			String msg = localStrings.getString( "admin.event.null_attribute_name" );
            throw new IllegalArgumentException( msg );
        }
        HashMap attributes = (HashMap) allAttributes.get(target);
        if (attributes != null) {
            return attributes.get(name);
        } else {
            return null;
        }
    }

    /**
     * Remove specified attribute. This method can be used to remove any
     * previously added attribute to event result. If name is null,
     * IllegalArgumentException is thrown.
     * @param target name of the target
     * @param name name of the attribute
     * @throws IllegalArgumentException if name is null.
     */
    void removeAttribute(String target, String name) {
        if (name == null) {
			String msg = localStrings.getString( "admin.event.null_attribute_name" );
            throw new IllegalArgumentException( msg );
        }
        synchronized (allAttributes) {
            HashMap attributes = (HashMap) allAttributes.get(target);
            if ( attributes != null) {
                attributes.remove(name);
            }
        }
    }

    /**
     * Get admin event result from the cache
     */
    public static AdminEventResult getAdminEventResult(AdminEvent event) {
        AdminEventResult result = (AdminEventResult)resultList.get(event);
        if (result == null) {
            result = new AdminEventResult(event.getSequenceNumber());
            resultList.put(event, result);
        }
        return result;
    }

    /**
     * Remove specified event from cache
     */
    static void clearAdminEventResultFromCache(AdminEvent event) {
        resultList.remove(event);
    }

    /**
     * Merge another event result into the current EventResult
    */
    public void addEventResult(String target, AdminEventResult eventResult) {
        if ( eventResult == null )
            return;

        this.allMessages.put( target,
            eventResult.getMessages().get(target));
        this.allExceptions.put( target,
            eventResult.getExceptions().get(target));
        this.allAttributes.put(target, 
            (HashMap)eventResult.getAttributes(target));

        if ( resultCodes == null ) {
            resultCodes = new HashMap();
            resultCodes.put(target, eventResult.getResultCode());
        }
    }
}

