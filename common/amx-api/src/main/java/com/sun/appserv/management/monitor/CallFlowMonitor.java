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
 
/*
 */

package com.sun.appserv.management.monitor;

import com.sun.appserv.management.base.XTypes;

import java.util.List;
import java.util.Map;

/**
 * Provides CallFlow Monitoring information as well as enables/disables 
 * CallFlow Monitoring.
 */
public interface CallFlowMonitor extends Monitoring
{
    /** The j2eeType as returned by
     * {@link com.sun.appserv.management.base.AMX#getJ2EEType}. 
     */
    public static final String J2EE_TYPE  = XTypes.CALL_FLOW_MONITOR;
        
    /**
     * Key accessing a container-generated Unique ID used by 
     * {@link #queryCallStackForRequest} and {@link #queryPieInformation}
     */
    public static final String REQUEST_ID_KEY = "RequestID";
    /**
     * TimeStamp obtained from {@link java.lang.System#nanoTime}
     */
    public static final String TIME_STAMP_KEY = "TimeStamp";
    
    /*
     * TimeStamp of Request Start obtained from {@link java.lang.System#currentTimeMillis}
     */
    public static final String TIME_STAMP_MILLIS_KEY = "TimeStampMillis";
    
    /**
     * The type of the Incoming request. An incoming request is the container 
     * that the request came into the Appserver.
     * Container types are the following types
     * <ul>
     *      <li>{@link #REMOTE_ASYNC_MESSAGE} </li>
     *      <li>{@link #REMOTE_EJB}</li>
     *      <li>{@link #REMOTE_WEB}</li>
     *      <li>{@link #REMOTE_WEB_SERVICE}</li>
     *      <li>{@link #TIMER_EJB}</li>
     * </ul>
     */
    public static final String REQUEST_TYPE_KEY = "RequestType";

    /**
     * Remote Aysnchronous Message Request Type. Typically incoming {@link #MESSAGE_DRIVEN_BEAN} Calls
     */
    public static final String REMOTE_ASYNC_MESSAGE = "REMOTE_ASYNC_MESSAGE";    

    /**
     * Remote EJB Request Type
     */
    public static final String REMOTE_EJB = "REMOTE_EJB";
    
    /**
     * Remote Web Request Type
     */
    public static final String REMOTE_WEB = "REMOTE_WEB";
    
    /**
     * Remote Web Service Request Type 
     */
    public static final String REMOTE_WEB_SERVICE = "REMOTE_WEB_SERVICE";
    
    /**
     * Timer EJB Request Type
     */
    public static final String TIMER_EJB = "TIMER_EJB";
    
    
    /**
     * The type of the component where the call is in.
     * Container types are the following types
     * <ul>
     *      <li>{@link #BEAN_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #CONTAINER_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #MESSAGE_DRIVEN_BEAN}</li>
     *      <li>{@link #SERVLET}</li>
     *      <li>{@link #SERVLET_FILTER}</li>
     *      <li>{@link #STATEFUL_SESSION_BEAN}</li>
     *      <li>{@link #STATELESS_SESSION_BEAN}</li>
     * </ul>
     */
    public static final String COMPONENT_TYPE_KEY = "ComponentType";

    /**
     * Servlet Component Type
     */
    public static final String SERVLET= "SERVLET";

    /**
     * Servlet Filter Component Type
     */
    public static final String SERVLET_FILTER = "SERVLET_FILTER";

    /**
     * Stateless Session Bean Component Type
     */
    public static final String STATELESS_SESSION_BEAN = "STATELESS_SESSION_BEAN";

    /**
     * Stateful Session Bean Component Type
     */
    public static final String STATEFUL_SESSION_BEAN = "STATEFUL_SESSION_BEAN";

    /**
     * Bean Managed Persistence Component Type
     */
    public static final String BEAN_MANAGED_PERSISTENCE = "BEAN_MANAGED_PERSISTENCE";

    /**
     * Container Managed Persistence Component Type
     */
    public static final String CONTAINER_MANAGED_PERSISTENCE = "CONTAINER_MANAGED_PERSISTENCE";

    /**
     * Message Driven Bean Component Type
     */
    public static final String MESSAGE_DRIVEN_BEAN = "MESSAGE_DRIVEN_BEAN";

    /**
     * The type of the container where the call originated
     */
    public static final String CONTAINER_TYPE_KEY = "ContainerType";
    
    /**
     * Web Container
     */
     public static final String WEB_CONTAINER = "WEB_CONTAINER";
     
    /**
     * EJB Container
     */
     public static final String EJB_CONTAINER = "EJB_CONTAINER";
     
     /**
      * ORB Container
      */
     public static final String ORB = "ORB_CONTAINER";

     /**
      * WEB Application Container 
      */
     public static final String WEB_APPLICATION = "WEB_APPLICATION";

     /**
      * EJB Application Container
      */
     public  static final String EJB_APPLICATION = "EJB_APPLICATION";
     
    /**
     * OTHER Container, containers that are not monitored for callflow
     */
    public static final String OTHER = "OTHER";
    
    /**
     * Username of the Caller making a request.
     */
    public static final String USER_KEY = "User";
    /**
     * Status of the request.
     */
    public static final String STATUS_KEY = "Status";
    /**
     * The client host from where the request came in.
     */
    public static final String CLIENT_HOST_KEY = "ClientHost";
    /**
     * Name of the method invoked to service a request.
     */
    public static final String METHOD_NAME_KEY = "MethodName";
    /**
     * Name of component invoked to service a request.     
     */
    public static final String COMPONENT_NAME_KEY = "ComponentName";

    /**
     * Name of the module invoked to service a request.
     */ 
    public static final String MODULE_NAME_KEY = "ModuleName";    
    /**
     * Name of application invoked to service a request.
     */
    public static final String APPLICATION_NAME_KEY = "ApplicationName";
    /**
     * Response time for a particular request.
     */
    public static final String RESPONSE_TIME_KEY = "ResponseTime";

    /**
     * Thread ID used for a particular request.
     */
    public static final String THREAD_ID_KEY = "ThreadID";
    /**
     * Transaction ID for a particular request
     */
    public static final String TRANSACTION_ID_KEY = "TransactionID";
    /**
     * Exception (if any) for the request. String reprsentation of
     * {@link java.lang.Throwable}
     */
    public static final String EXCEPTION_KEY = "Exception";
    /**
     * This is the key for the row type returned by the 
     * {@link #queryCallStackForRequest} method.
     * There are four types of rows returned each signifying the RequestStart, 
     * MethodStart, MethodEnd and RequestEnd information.
     * The values of the CallStackRowType are as follows 
     * <ul>
     *  <li> {@link #CALL_STACK_REQUEST_START} </li>
     *  <li> {@link #CALL_STACK_REQUEST_END} </li>
     *  <li> {@link #CALL_STACK_METHOD_START} </li>
     *  <li> {@link #CALL_STACK_METHOD_END} </li>
     * </ul>
     */
    public static final String CALL_STACK_ROW_TYPE_KEY = "CallStackRowType";
    
    /**
     * Value of type of a row returned by {@link #queryCallStackForRequest} method.
     * Keyed by {@link #CALL_STACK_ROW_TYPE_KEY}
     * This represents information pertaining to start of a incoming request 
     * in the container.
     */
    public static final String CALL_STACK_REQUEST_START = "RequestStart";
    
    /**
     * Value of type of a row returned by {@link #queryCallStackForRequest} method.
     * Keyed by {@link #CALL_STACK_ROW_TYPE_KEY}
     * This represents information pertaining to end of a incoming request 
     * in the container.
     */    
    public static final String CALL_STACK_REQUEST_END = "RequestEnd";

    /**
     * Value of type of a row returned by {@link #queryCallStackForRequest} method.
     * Keyed by {@link #CALL_STACK_ROW_TYPE_KEY}
     * This represents information pertaining to start of a method for a 
     * incoming request in the container.
     */
    public static final String CALL_STACK_METHOD_START = "MethodStart";
    
    /**
     * Value of type of a row returned by {@link #queryCallStackForRequest} method.
     * Keyed by {@link #CALL_STACK_ROW_TYPE_KEY}
     * This represents information pertaining to method end of a incoming request 
     * in the container.
     */    
    public static final String CALL_STACK_METHOD_END = "MethodEnd";
    
    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a
     * particular request in the EJB Container
     */
    public static final String EJB_CONTAINER_TYPE = "EJBContainer";

    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a
     * particular request in the Web Container
     */
    public static final String WEB_CONTAINER_TYPE = "WebContainer";

    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a
     * particular request in the ORB layer in EJB Container.
     */
    public static final String ORB_LAYER_EJB_CONTAINER_TYPE = "ORBLayerInEJBContainer";

    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a
     * particular request in a users EJB application code. This time is different
     * from the time spent in the EJB or ORB Container code.
     */
    public static final String EJB_APPLICATION_TYPE = "EJBApplication";

    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a
     * particular request in a users Web Application code. This time is different
     * from the time spent in the Web Container code.
     */
    public static final String WEB_APPLICATION_TYPE = "WebApplication";

    /**
     * Used in {@link #queryPieInformation}. Denotes the time spent by a 
     * particular request in the connector layer.
     */
    public static final String CONNECTOR_CONTAINER_TYPE = "Connector";
    /**
     *  @return true if the callflow tracking is on, false otherwise
     */
    public boolean getEnabled();
    
    
    /**
     * Turns CallFlow On or Off
     * @param enabled true to enable call flow tracking
     */
    public void setEnabled(boolean enabled);
    
    /**
     * Deletes all the data that was collected during the last callflow run 
     * from the database
     */
    public void clearData();
    
    /**
     * Delete a list of request ids.
     * @param requestId an array of request ids. Request IDs are obtained using
     * the {@link #REQUEST_ID_KEY} from {@link #queryRequestInformation}
     */
    public boolean deleteRequestIDs (String[] requestId);
    /**
     * Gets information for requests.
     * Each row in the list is a Map of key=value 
     * pairs, each key and value is a @link java.lang.String respectively.
     * The keys are of the type 
     * <ul>
     * <li>{@link #REQUEST_ID_KEY}</li>
     * <li>{@link #TIME_STAMP_MILLIS_KEY}</li>
     * <li>{@link #REQUEST_TYPE_KEY}. There are 5 types</li>
     * <ul>
     *      <li>{@link #REMOTE_ASYNC_MESSAGE} </li>
     *      <li>{@link #REMOTE_EJB}</li>
     *      <li>{@link #REMOTE_WEB}</li>
     *      <li>{@link #REMOTE_WEB_SERVICE}</li>
     *      <li>{@link #TIMER_EJB}</li>
     * </ul>
     * <li>{@link #USER_KEY}</li>
     * <li>{@link #STATUS_KEY}</li>
     * <li>{@link #CLIENT_HOST_KEY}</li>
     * <li>{@link #METHOD_NAME_KEY}</li>
     * <li>{@link #APPLICATION_NAME_KEY}</li>
     * <li>{@link #RESPONSE_TIME_KEY}</li>
     * </ul>
     * @return List<Map<String, String>>, a list of Maps each encapsulating
     * all information represented by the keys
     */
    public List<Map<String, String>> queryRequestInformation();       
    
    /**
     * Returns CallStackInformation for a particular RequestID. This list is 
     * sorted on time to return a logical flow of calls through various containers
     * for a particular requestID. Each row in the list is a Map of key=value 
     * pairs, each key and value is a @link java.lang.String respectively.
     * The keys are of the type 
     * <ul>
     * <li> </li>
     * <li>{@link #REQUEST_ID_KEY}</li>
     * <li>{@link #TIME_STAMP_KEY}</li>
     * <li>{@link #TIME_STAMP_MILLIS_KEY}</li>
     * <li>{@link #REQUEST_TYPE_KEY}. This represents the Container where the call
     * origniated in and is only available for 
     * {@link #CALL_STACK_ROW_TYPE_KEY} of the types 
     *      <ul>
     *          <li>{@link #CALL_STACK_REQUEST_START}</li>
     *      </ul>
     * <pre>
     * There are 5 values for the {@link #REQUEST_TYPE_KEY}
     * <ul>
     *      <li>{@link #REMOTE_ASYNC_MESSAGE} </li>
     *      <li>{@link #REMOTE_EJB}</li>
     *      <li>{@link #REMOTE_WEB}</li>
     *      <li>{@link #REMOTE_WEB_SERVICE}</li>
     *      <li>{@link #TIMER_EJB}</li>
     * </ul>
     * </li>
     * <li>{@link #USER_KEY}</li>
     * <li>{@link #STATUS_KEY}</li>
     * <li>{@link #METHOD_NAME_KEY}</li>
     * <li>{@link #APPLICATION_NAME_KEY}</li>
     * <li>{@link #THREAD_ID_KEY}</li>
     * <li>{@link #TRANSACTION_ID_KEY}</li>
     * <li>{@link #RESPONSE_TIME_KEY}</li>
     * <li>{@link #EXCEPTION_KEY}</li>     
     * <li>{@link #CONTAINER_TYPE_KEY}. This represents the container where the 
     * call is and is only available for 
     * {@link #CALL_STACK_ROW_TYPE_KEY} of the types 
     *      <ul>
     *          <li>{@link #CALL_STACK_METHOD_START}</li>
     *          <li>{@link #CALL_STACK_METHOD_END}</li>
     *      </ul>
     * There are  7 valid values for {@link #CONTAINER_TYPE_KEY}
     * <ul>
     *      <li>{@link #BEAN_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #CONTAINER_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #MESSAGE_DRIVEN_BEAN}</li>
     *      <li>{@link #SERVLET}</li>
     *      <li>{@link #SERVLET_FILTER}</li>
     *      <li>{@link #STATEFUL_SESSION_BEAN}</li>
     *      <li>{@link #STATELESS_SESSION_BEAN}</li>
     * </ul>
     * </li>
     * </ul>
     * @param requestID obtained on calling #getRequestInformation
     * @return List<Map<String, String>>, a list of Maps each encapsulating
     * all information represented by the keys
     */
    public List<Map<String, String>> queryCallStackForRequest(String requestID);
       
    /**
     * Returns the "PIE" information for a requestID. The "PIE" is a simple
     * Map<String, String> with key=value pairs indicating the time spent 
     * for each call in a particular container/
     * The keys are of the types
     * <li>{@link #CONTAINER_TYPE_KEY} . There are four types</li>
     *  <ul>
     *      <li>{@link #EJB_CONTAINER_TYPE} </li>
     *      <li>{@link #WEB_CONTAINER_TYPE}</li>
     *      <li>{@link #ORB} </li>
     *      <li>{@link #CONNECTOR_CONTAINER_TYPE}</li>
     *   </ul>
     * The values for the keys are String representation of time spent in each
     * container. Time is obtained using 
     * {@link java.lang.System#currentTimeMillis}
     * @param requestID ID of the request whose PIE information is to 
     * be obtained.
     * @return Map<String, String>
     */    
    public Map<String, String> queryPieInformation (String requestID);
   
    /**
     * Gets the at-source IP filter set up on this particular Server Instance.
     * An At-source filter, filters the Call Flow Data at Data Collection Time.
     * of the type of filter and value is a String value.
     * @return String the string representation of the IP filter set for this 
     * instance.
     */
    public String getCallerIPFilter();
    
    /**
     * Sets the at-source IP filter for this particular Server Instance.
     * @param filter The ip address to filter on.
     */    
    public void setCallerIPFilter(String filter);	
    
    /**
     * Gets the at-source Principal filter for this particular Server Instance.
     * @return String The Principal Name that is filtered for.
     */    
    public String getCallerPrincipalFilter();
    
    /**
     * Sets the at-source Principal filter for this particular Server Instance.
     * @param filter The Principal Name to filter on.
     */    
    public void setCallerPrincipalFilter (String filter);
    
    /**
     * Returns a list of all request types
     * <ul>
     *      <li>{@link #REMOTE_ASYNC_MESSAGE} </li>
     *      <li>{@link #REMOTE_EJB}</li>
     *      <li>{@link #REMOTE_WEB}</li>
     *      <li>{@link #REMOTE_WEB_SERVICE}</li>
     *      <li>{@link #TIMER_EJB}</li>
     * </ul>
     *
     */
    public String[] queryRequestTypeKeys ();
    
    /**
     * Returns a list of component types
     *      <li>{@link #BEAN_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #CONTAINER_MANAGED_PERSISTENCE}</li>
     *      <li>{@link #MESSAGE_DRIVEN_BEAN}</li>
     *      <li>{@link #SERVLET}</li>
     *      <li>{@link #SERVLET_FILTER}</li>
     *      <li>{@link #STATEFUL_SESSION_BEAN}</li>
     *      <li>{@link #STATELESS_SESSION_BEAN}</li>
     */
    public String[] queryComponentTypeKeys ();
    
    /**
     * Returns a list of Container types
     * <li>{@link #WEB_CONTAINER}</li>
     * <li>{@link #EJB_CONTAINER}</li>
     * <li>{@link #ORB}</li>
     * <li>{@link #WEB_APPLICATION}</li>
     * <li>{@link #EJB_APPLICATION}</li>
     * <li>{@link #OTHER}</li>
     */
    public String[] queryContainerTypeOrApplicationTypeKeys ();
}
