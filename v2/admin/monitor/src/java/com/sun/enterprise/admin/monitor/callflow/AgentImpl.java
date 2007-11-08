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
 * AgentImpl.java
 * $Id: AgentImpl.java,v 1.36 2007/04/23 17:10:13 harpreet Exp $
 * $Date: 2007/04/23 17:10:13 $
 * $Revision: 1.36 $
 */

package	com.sun.enterprise.admin.monitor.callflow;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.web.connector.extension.GrizzlyConfig;

import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 * This	class implements a call	flow agent, which collects call	flow data
 * and sends it	to data	store, for later querying and analysis.
 *
 * It is possible to filter the	requests for which call	flow data is gathered,
 * based on caller host	IP address, and	caller id (user	name).
 *
 * @author Ram Jeyaraman, Harpreet Singh, Nazrul Islam,	Siraj Ghaffar
 * @date March 21, 2005
 **/
public class AgentImpl implements Agent	{

    /**	Static definitions. */

    private static final Logger	logger =
	    Logger.getLogger(AdminConstants.kLoggerName);

    static class RequestData {

	private	RequestType requestType;
	private	long requestStartTime;
	private	long requestStartTimeMillis;
	private	String callerIPAddress;
	private	String remoteUser;

	RequestType getRequestType() {
	    return requestType;
	}

	void setRequestType(RequestType	requestType) {
	    this.requestType = requestType;
	}

	long getRequestStartTime() {
	    return requestStartTime;
	}

	void setRequestStartTime(long requestStartTime)	{
	    this.requestStartTime = requestStartTime;
	}

	long getRequestStartTimeMillis() {
	    return requestStartTimeMillis;
	}

	void setRequestStartTimeMillis(long requestStartTimeMillis) {
	    this.requestStartTimeMillis	= requestStartTimeMillis;
	}

	String getCallerIPAddress() {
	    return callerIPAddress;
	}

	void setCallerIPAddress(String callerIPAddress)	{
	    this.callerIPAddress = callerIPAddress;
	}

	String getRemoteUser() {
	    return remoteUser;
	}

	void setRemoteUser(String remoteUser) {
	    this.remoteUser = remoteUser;
	}
    }

    // Non-synchronized	Stack implementation.
    static class FlowStack<E> extends LinkedList<E> {
	// Note: Java SE 1.6 introduced	these methods
	// on java.util.Deque. Keeping these methods
	// for backward	compatibility with Java	SE 1.5
	public void push(E e) {
	    addFirst(e);
	}

	public E pop() {
	    return removeFirst();
	}
    }

    public static class	ThreadLocalState implements ThreadLocalData {

	private	String requestId;
	private	boolean	initialized = false;
	private	boolean	storeData = false;

	// The following attributes are	used primarily by the log manager
	// to log these	values into the	log message. ThreadId is not cached
	// at this point, because the log manager does not require it.
	private	String methodName;
	private	String componentType;
	private	String applicationName;
	private	String moduleName;
	private	String componentName;
	private	String transactionId;
	private	String securityId;

	// RequestStart	data holder.
	private	RequestData requestData;

	// To handle nested container trap point invocations.
	private	FlowStack<ContainerTypeOrApplicationType> flowStack;

	ThreadLocalState() {
	    requestId =	UUID.randomUUID().toString();
	    requestData	= new RequestData();
	    flowStack =	new FlowStack<ContainerTypeOrApplicationType>();
	}

	public String getRequestId() {
	    return requestId;
	}

	boolean	getInitialized() {
	    return initialized;
	}

	void setInitialized(boolean initialized) {
	    this.initialized = initialized;
	}

	boolean	getStoreData() {
	    return storeData;
	}

	void setStoreData(boolean storeData) {
	    this.storeData = storeData;
	}

	public String getMethodName() {
	    return methodName;
	}

	void setMethodName(String methodName) {
	    this.methodName = methodName;
	}

	public String getApplicationName() {
	    return applicationName;
	}

	void setApplicationName(String applicationName)	{
	    this.applicationName = applicationName;
	}

	public String getModuleName() {
	    return moduleName;
	}

	void setModuleName(String moduleName) {
	    this.moduleName = moduleName;
	}

	public String getComponentName() {
	    return componentName;
	}

	void setComponentName(String componentName) {
	    this.componentName = componentName;
	}

	public String getComponentType() {
	    return componentType;
	}

	void setComponentType(String componentType) {
	    this.componentType = componentType;
	}

	public String getTransactionId() {
	    return transactionId;
	}

	void setTransactionId(String transactionId) {
	    this.transactionId = transactionId;
	}

	public String getSecurityId() {
	    return securityId;
	}

	void setSecurityId(String securityId) {
	    this.securityId = securityId;
	}

	RequestData getRequestData() {
	    return requestData;
	}

	FlowStack<ContainerTypeOrApplicationType> getFlowStack() {
	    return flowStack;
	}
    }

    // Thread local object.
    private static final ThreadLocal<ThreadLocalState> threadLocal =
	    new	ThreadLocal() {
	protected ThreadLocalState initialValue() {
	    return new ThreadLocalState();
	}
    };

    /**	Instance variables. */

    private static Agent __singletonAgent = new AgentImpl();

    private AtomicBoolean storeData = new AtomicBoolean (false);
    // With Perf Improvement. This logic will go away. When a complete
    // shift is	done to	completely delete the non-perf code. This will be junked
    private int	dataWriterThreadCount;

    private String callerIPAddressFilter;
    private String callerPrincipalFilter;

    private AsyncHandlerIntf asyncHandler = AsyncHandlerFactory.getInstance();
    private DbAccessObject dbAccessObject = DbAccessObjectImpl.getInstance();

    private boolean traceOn; //	generates flow sequence	trace debug messages.

    private List<Listener> listeners =
	    java.util.Collections.synchronizedList(new ArrayList<Listener>());


    // To allow	plugging the performant	thread implementation.
    private boolean perfImpl =
	    System.getProperty("com.sun.enterprise.callflow.perf", "true").equals("true");

    private AgentImpl() {
    	traceOn	= TraceOnHelper.isTraceOn();
    }

    public static Agent getInstance(){
    	return __singletonAgent;
    }
    /**	Call flow trap points. */

    public void	requestStart(RequestType requestType) {
	try {
	    if (!getStoreData())
		return;
	    threadLocal.remove(); // sanity check
	    ThreadLocalState tls = threadLocal.get();
	    tls.getFlowStack().clear();
	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", requestStart()");
	    }
	    // NOTE: The request start trap point
	    // data is written out during the first startTime trap point.
	    tls.getRequestData().setRequestType(requestType);
	    tls.getRequestData().setRequestStartTime(System.nanoTime());
	    tls.getRequestData().
		    setRequestStartTimeMillis(System.currentTimeMillis());
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.request_start_operation_failed", e);
	}
    }

    public void	addRequestInfo(RequestInfo requestInfo,	String value) {
	try {
	    if(!getStoreData())
		return;
	    ThreadLocalState tls = threadLocal.get();
	    if (traceOn) {
		logger.log(
			Level.INFO, tls.getRequestId() + ", addRequestInfo()");
	    }
	    if (tls.getInitialized()) {
		logger.log(
			Level.FINE,
			"callflow.add_request_info_disallowed");
		return;
	    }
	    if (requestInfo == RequestInfo.CALLER_IP_ADDRESS) {
		tls.getRequestData().setCallerIPAddress(value);
	    } else if (requestInfo == RequestInfo.REMOTE_USER) {
		tls.getRequestData().setRemoteUser(value);
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.add_request_info_operation_failed", e);
	}
    }

    /**
     * Called only from	startTime method. startTime is the first trap point
     * that is called after all	the request information	such as	callerIPAddress
     * and remoteUser is supplied by the container.
     *
     * Upon being called, this method initializes the request thread local
     * state. Even though this method may be called several times by various
     * startTime invocations, only the first call for this request succeeds in
     * initializing the	thread local state.
     *
     * Note, the trap point call sequence has a	specific order:
     *
     *	    {
     *	      requestStart, addRequestInfo*,
     *	      (startTime, (webMethodStart|ejbMethodStart))*,
     *	      ((ejbMethodEnd|webMethodEnd), endTime)*,
     *	      requestEnd
     *	    }
     *
     *	The startTime is the first trap	point to be called after all the
     *	request	information is supplied	via addRequestInfo method.
     */
    private void initialize() {

	ThreadLocalState tls = threadLocal.get();
	if (tls.getInitialized()) {
	    return;
	}

	// Check filters.

	RequestData requestData	= tls.getRequestData();
	String callerIPAddress = requestData.getCallerIPAddress();
	String remoteUser = requestData.getRemoteUser();
	boolean	filtered = false;

	if ((this.callerIPAddressFilter	!= null) &&
		!(this.callerIPAddressFilter.equals(callerIPAddress))) {
	    filtered = true;
	}

	if ((this.callerPrincipalFilter	!= null) &&
		!(this.callerPrincipalFilter.equals(remoteUser))) {
	    filtered = true;
	}

	// Allow storing the data, iff the call	is not filtered	and
	// storeData flag is turned on.
	tls.setStoreData(!filtered && getStoreData());

	// Create asychronous data asyncHandler	iff there is atleast one thread
	// whose storeData flag	is set.
	if (!perfImpl){
	    if (!filtered && getStoreData())	{
		synchronized (asyncHandler) {
		    if (dataWriterThreadCount == 0) {
			this.asyncHandler.enable();
		    }
		    dataWriterThreadCount++;
		}
	    }
	}

	// Note, this flag has to be set before	the delayed write.
	// Otherwise, this will	result in unterminated recursion,
	// due to the startTime() method call.
	tls.setInitialized(true);

	// Notify listeners.

	boolean	listenersPresent = false;
	long otherStartTime = System.nanoTime();
	if (listeners.isEmpty()	== false) {
	    listenersPresent = true;
	    synchronized (listeners) {
		for (Listener listener : listeners) {
		    listener.requestStart(
			    tls.getRequestId(),	requestData.getRequestType(),
			    callerIPAddress, remoteUser);
		}
	    }
	}
	long otherEndTime = System.nanoTime();

	// Store data (delayed write).

	if (tls.getStoreData())	{
	    storeRequestStartData(
		    tls.getRequestId(),	requestData.getRequestStartTime(),
		    requestData.getRequestStartTimeMillis(),
		    requestData.getRequestType(), callerIPAddress, remoteUser,
		    tls.getFlowStack(),	listenersPresent, otherStartTime,
		    otherEndTime);
	}
    }

    public void	requestEnd() {

	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", requestEnd()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.requestEnd(tls.getRequestId());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeRequestEndData(
			tls.getRequestId(), tls.getFlowStack(),
			listenersPresent, otherStartTime, otherEndTime);
	    }

	    // Check if	there are any more data	writer threads.	If this	is the
	    // last one, no need for the async data asyncHandler thread.

	    if (!perfImpl){
		if (tls.getStoreData())	{
		    synchronized (asyncHandler)	{
			dataWriterThreadCount--;
			if (dataWriterThreadCount == 0)	{
			    this.asyncHandler.disable();
			}
		    }
		}
	    }

	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.request_end_operation_failed", e);
	} finally {
	    threadLocal.remove();
	}
    }

    public void	startTime(ContainerTypeOrApplicationType type) {
	try {
	    if (!getStoreData())
		return;
	    ThreadLocalState tls = threadLocal.get();
	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", startTime()");
	    }
	    // Note: In	the natural callflow sequence, startTime() is the first
	    // method that is called after all the addRequestInfo() calls are
	    // complete. So, we	use the	very first startTime() operation to
	    // do the initialization, filtering, et cetera.
	    initialize(); // idempotent
	    if (tls.getStoreData()) {
		storeStartTimeData(
			tls.getRequestId(), type, tls.getFlowStack());
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.start_time_operation_failed", e);
	}
    }

    public void	endTime() {
	try {
	    if (!getStoreData())
		return;
	    ThreadLocalState tls = threadLocal.get();
	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", endTime()");
	    }
	    if (tls.getStoreData()) {
		storeEndTimeData(tls.getRequestId(), tls.getFlowStack());
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.end_time_operation_failed", e);
	}
    }

    public void	ejbMethodStart(CallFlowInfo info) {

	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(
			Level.INFO, tls.getRequestId() + ", ejbMethodStart()");
	    }

	    String requestId = tls.getRequestId();
	    String methodName =	info.getMethod().toString();
	    ComponentType componentType	= info.getComponentType();
	    String applicationName = info.getApplicationName();
	    String moduleName =	info.getModuleName();
	    String componentName = info.getComponentName();
	    String transactionId = info.getTransactionId();
	    String securityId =	info.getCallerPrincipal();

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.ejbMethodStart(
				requestId, methodName, applicationName,
				moduleName, componentName, componentType,
				securityId, transactionId);
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeMethodStartData(
			tls.getRequestId(), info.getMethod().toString(),
			info.getComponentType(), info.getApplicationName(),
			info.getModuleName(), info.getComponentName(),
			Thread.currentThread().getName(), info.getTransactionId(),
			info.getCallerPrincipal(), tls.getFlowStack(),
			ContainerTypeOrApplicationType.EJB_APPLICATION,
			listenersPresent, otherStartTime, otherEndTime);
	    }

	    // Update thread local state.

	    tls.setMethodName(methodName);
	    tls.setApplicationName(applicationName);
	    tls.setModuleName(moduleName);
	    tls.setComponentName(componentName);
	    tls.setComponentType(componentType.toString());
	    tls.setTransactionId(transactionId);
	    tls.setSecurityId(securityId);

	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.ejb_method_start_operation_failed", e);
	}
    }

    public void	ejbMethodEnd(CallFlowInfo info)	{

	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", ejbMethodEnd()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.ejbMethodEnd(
				tls.getRequestId(), info.getException());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeMethodEndData(
			tls.getRequestId(), info.getException(), tls.getFlowStack(),
			listenersPresent, otherStartTime, otherEndTime);
	    }

	    // Clear relevant thread local state.

	    tls.setMethodName(null);
	    tls.setApplicationName(null);
	    tls.setModuleName(null);
	    tls.setComponentName(null);
	    tls.setComponentType(null);
	    tls.setTransactionId(null);
	    tls.setSecurityId(null);

	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.ejb_method_end_operation_failed",	e);
	}
    }

    public void	webMethodStart(
	    String methodName, String applicationName, String moduleName,
	    String componentName, ComponentType	componentType,
	    String callerPrincipal) {

	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(
			Level.INFO, tls.getRequestId() + ", webMethodStart()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.webMethodStart(
				tls.getRequestId(), methodName,	applicationName,
				moduleName, componentName, componentType,
				callerPrincipal);
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeMethodStartData(
			tls.getRequestId(), methodName,	componentType,
			applicationName, moduleName,
			componentName, Thread.currentThread().getName(),
			null, callerPrincipal, tls.getFlowStack(),
			ContainerTypeOrApplicationType.WEB_APPLICATION,
			listenersPresent, otherStartTime, otherEndTime);
	    }

	    // Update thread local state.

	    tls.setMethodName(methodName);
	    tls.setApplicationName(applicationName);
	    tls.setModuleName(moduleName);
	    tls.setComponentName(componentName);
	    tls.setComponentType(componentType.toString());
	    tls.setTransactionId(null);
	    tls.setSecurityId(callerPrincipal);

	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.web_method_start_operation_failed", e);
	}
    }

    public void	webMethodEnd(Throwable exception) {

	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", webMethodEnd()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.webMethodEnd(tls.getRequestId(), exception);
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeMethodEndData(
			tls.getRequestId(), exception, tls.getFlowStack(),
			listenersPresent, otherStartTime, otherEndTime);
	    }

	    // Clear relevant thread local state.

	    tls.setMethodName(null);
	    tls.setApplicationName(null);
	    tls.setModuleName(null);
	    tls.setComponentName(null);
	    tls.setComponentType(null);
	    tls.setTransactionId(null);
	    tls.setSecurityId(null);

	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.web_method_end_operation_failed",	e);
	}
    }

    public void	entityManagerQueryStart(EntityManagerQueryMethod queryMethod) {
	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(
			Level.INFO, tls.getRequestId() +
			", entityManagerQuerytart()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.entityManagerQueryStart(
				tls.getRequestId(), queryMethod, tls.getApplicationName(),
				tls.getModuleName(), tls.getComponentName(),
				ComponentType.JPA, tls.getSecurityId());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();
	    // Store data.
	    if (tls.getStoreData()) {
		storeMethodStartData(
			tls.getRequestId(), queryMethod.toString(), ComponentType.JPA,
			tls.getApplicationName(), tls.getModuleName(),
			tls.getComponentName(),	Thread.currentThread().getName(),
			null, tls.getSecurityId(), tls.getFlowStack(),
			ContainerTypeOrApplicationType.JAVA_PERSISTENCE,
			listenersPresent, otherStartTime, otherEndTime);
	    }
	} catch	(Exception e) {
	    // XXX change the warning method
	    logger.log(
		    Level.WARNING,
		    "callflow.web_method_start_operation_failed", e);
	}

    }
    public void	entityManagerQueryEnd()	{
	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() + ", entityManagerQueryEnd()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.entityManagerQueryEnd(tls.getRequestId());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.
	    if (tls.getStoreData()) {
		storeMethodEndData(
			tls.getRequestId(), null, tls.getFlowStack(),
			listenersPresent, otherStartTime, otherEndTime);
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.ejb_method_end_operation_failed",	e);
	}
    }

    public void	entityManagerMethodStart(EntityManagerMethod entityManagerMethod) {
	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(
			Level.INFO, tls.getRequestId() +
			", entityManagerMethodStart()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.entityManagerMethodStart(
				tls.getRequestId(), entityManagerMethod,
				tls.getApplicationName(),
				tls.getModuleName(), tls.getComponentName(),
				ComponentType.JPA, tls.getSecurityId());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.
	    if (tls.getStoreData()) {
		storeMethodStartData(
			tls.getRequestId(), entityManagerMethod.toString(), ComponentType.JPA,
			tls.getApplicationName(), tls.getModuleName(),
			tls.getComponentName(),	Thread.currentThread().getName(),
			null, tls.getSecurityId(), tls.getFlowStack(),
			ContainerTypeOrApplicationType.JAVA_PERSISTENCE,
			listenersPresent, otherStartTime, otherEndTime);
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.web_method_start_operation_failed", e);
	}


    }
    public void	entityManagerMethodEnd() {
	try {
	    if (!getStoreData())
		return;

	    ThreadLocalState tls = threadLocal.get();

	    if (traceOn) {
		logger.log(Level.INFO, tls.getRequestId() +
			", entityManagerMethodEnd()");
	    }

	    // Notify listeners.

	    boolean listenersPresent = false;
	    long otherStartTime	= System.nanoTime();
	    if (listeners.isEmpty() == false) {
		listenersPresent = true;
		synchronized (listeners) {
		    for	(Listener listener : listeners)	{
			listener.entityManagerMethodEnd(tls.getRequestId());
		    }
		}
	    }
	    long otherEndTime =	System.nanoTime();

	    // Store data.

	    if (tls.getStoreData()) {
		storeMethodEndData(
			tls.getRequestId(), null, tls.getFlowStack(),
			listenersPresent, otherStartTime, otherEndTime);
	    }
	} catch	(Exception e) {
	    logger.log(
		    Level.WARNING,
		    "callflow.ejb_method_end_operation_failed",	e);
	}


    }

    // Private methods.

    /**
     * Note: This method is called lazily from startTime/initialize().
     */
    private void storeRequestStartData(
	    String requestId, long timeStamp, long timeStampMillis,
	    RequestType	requestType, String callerIPAddress,
	    String remoteUser,
	    FlowStack<ContainerTypeOrApplicationType> flowStack,
	    boolean listenersPresent, long otherStartTime, long	otherEndTime) {
	if (requestType	== RequestType.REMOTE_WEB) {
	    flowStack.push(ContainerTypeOrApplicationType.WEB_CONTAINER);
	} else if (requestType == RequestType.REMOTE_EJB) {
	    // Remote EJBs first enter via the ORB layer.
	    flowStack.push(ContainerTypeOrApplicationType.ORB_CONTAINER);
	} else { // Timer EJB or MDB
	    flowStack.push(ContainerTypeOrApplicationType.EJB_CONTAINER);
	}
	asyncHandler.handleRequestStart(
		requestId, timeStamp, timeStampMillis,
		requestType, callerIPAddress, remoteUser);
	asyncHandler.handleStartTime(requestId,	timeStamp, flowStack.peek());
	if (listenersPresent) {
	    flowStack.push(ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleStartTime(
		    requestId, otherStartTime,
		    ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleEndTime(
		    requestId, otherEndTime, flowStack.pop());
	}
    }

    private void storeRequestEndData(
	    String requestId,
	    FlowStack<ContainerTypeOrApplicationType> flowStack,
	    boolean listenersPresent, long otherStartTime, long	otherEndTime) {
	asyncHandler.handleEndTime(
		requestId, System.nanoTime(), flowStack.pop());
	if (listenersPresent) {
	    flowStack.push(ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleStartTime(
		    requestId, otherStartTime,
		    ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleEndTime(
		    requestId, otherEndTime, flowStack.pop());
	}
	asyncHandler.handleRequestEnd(requestId, System.nanoTime());
    }

    private void storeMethodStartData(
	    String requestId, String methodName,
	    ComponentType componentType, String	applicationName,
	    String moduleName, String componentName, String threadId,
	    String transactionId, String securityId,
	    FlowStack<ContainerTypeOrApplicationType> flowStack,
	    ContainerTypeOrApplicationType appType,
	    boolean listenersPresent, long otherStartTime, long	otherEndTime) {
	asyncHandler.handleEndTime(
		requestId, System.nanoTime(), flowStack.peek());
	if (listenersPresent) {
	    flowStack.push(ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleStartTime(
		    requestId, otherStartTime,
		    ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleEndTime(
		    requestId, otherEndTime, flowStack.pop());
	}
	asyncHandler.handleMethodStart(
		requestId, System.nanoTime(), methodName, componentType,
		applicationName, moduleName, componentName, threadId,
		transactionId, securityId);
	flowStack.push(appType);
	asyncHandler.handleStartTime(requestId,	System.nanoTime(), appType);
    }

    private void storeMethodEndData(
	    String requestId, Throwable	exception,
	    FlowStack<ContainerTypeOrApplicationType> flowStack,
	    boolean listenersPresent, long otherStartTime, long	otherEndTime) {
	asyncHandler.handleEndTime(
		requestId, System.nanoTime(), flowStack.pop());
	asyncHandler.handleMethodEnd(requestId,	System.nanoTime(), exception);
	if (listenersPresent) {
	    flowStack.push(ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleStartTime(
		    requestId, otherStartTime,
		    ContainerTypeOrApplicationType.OTHER);
	    asyncHandler.handleEndTime(
		    requestId, otherEndTime, flowStack.pop());
	}
	asyncHandler.handleStartTime(
		requestId, System.nanoTime(), flowStack.peek());
    }


    private void storeStartTimeData(
	    String requestId,
	    ContainerTypeOrApplicationType type,
	    FlowStack<ContainerTypeOrApplicationType> flowStack) {
	asyncHandler.handleEndTime(
		requestId, System.nanoTime(), flowStack.peek());
	flowStack.push(type);
	asyncHandler.handleStartTime(requestId,	System.nanoTime(), type);
    }

    private void storeEndTimeData(
	    String requestId,
	    FlowStack<ContainerTypeOrApplicationType> flowStack) {
	asyncHandler.handleEndTime(
		requestId, System.nanoTime(), flowStack.pop());
	asyncHandler.handleStartTime(
		requestId, System.nanoTime(), flowStack.peek());
    }

    /**	Data accessors.	*/

    public ThreadLocalData getThreadLocalData()	{
	return (ThreadLocalData) threadLocal.get();
    }

    /**	Support	for notification. */

    public void	registerListener(Listener listener) {
	if (listeners.contains(listener) == false) {
	    listeners.add(listener);
	}
    }

    public void	unregisterListener(Listener listener) {
	listeners.remove(listener);
    }

    public boolean getStoreData (){
        return this.storeData.get();
    }
    
    public void setStoreData (boolean value){
        this.storeData.set(value);
    }
    /**	API to support AMX MBean calls.	*/

    public void setEnable(boolean enable) {
        synchronized (this){
            if (enable) {
                asyncHandler = AsyncHandlerFactory.getInstance();
                if(perfImpl){ // perf impl
                    asyncHandler.enable();
                }
                if (getStoreData() == false){
                    boolean	result = this.dbAccessObject.enable();
                    if (result) {
                        enableGrizzly(true);
                        logger.log(Level.INFO, "callflow.enable_succeeded");
                    } else {
                        logger.log(Level.SEVERE, "callflow.enable_failed");
                        throw new RuntimeException("Callflow Enable	Failed");
                    }
                }
            } else {
                this.callerIPAddressFilter = null;
                this.callerPrincipalFilter = null;
                if (perfImpl){
                    if(asyncHandler	!= null){
                        asyncHandler.disable();
                    }
                    asyncHandler = null;
                }
                if (getStoreData() == true)	{
                    enableGrizzly(false);
                    this.dbAccessObject.disable();
                    logger.log(Level.INFO, "callflow.disable_succeeded");
                }
            }
            setStoreData(enable);
        }
   }

    public boolean isEnabled() {
	return storeData.get();
    }

    public void	enableGrizzly(boolean enable) {
	List<GrizzlyConfig> grizzlies =	GrizzlyConfig.getGrizzlyConfigInstances();
	for (GrizzlyConfig grizzly: grizzlies){
	    grizzly.setEnableCallFlow(true);
	}

    }
    public void	setCallerIPFilter(String ipAddress) {
	this.callerIPAddressFilter = ipAddress;
	if ((callerIPAddressFilter != null) &&
		(callerIPAddressFilter.equals(""))) {
	    callerIPAddressFilter = null;
	}
    }

    public String getCallerIPFilter() {
	return this.callerIPAddressFilter;
    }

    public void	setCallerPrincipalFilter(String	callerPrincipal) {
	this.callerPrincipalFilter = callerPrincipal;
	if ((callerPrincipalFilter != null) &&
		(callerPrincipalFilter.equals(""))) {
	    callerPrincipalFilter = null;
	}
    }

    public String getCallerPrincipalFilter() {
	return this.callerPrincipalFilter;
    }

    public void	clearData() {
	if (getStoreData() == false){
	    dbAccessObject.clearData();
	} else {
	    logger.log(
		    Level.WARNING,
		    "callflow.turn_off_callflow_before_clearData");
	}
    }

    public boolean deleteRequestIds(String[] requestIds) {
	return dbAccessObject.deleteRequestIds(requestIds);
    }

    public List<Map<String, String>> getRequestInformation() {
	// flush AsyncHandlerQ's so that if asyncThread	is awake, we get
	// freshly cooked informtion
	if (asyncHandler != null)
	    this.asyncHandler.flush();
	return dbAccessObject.getRequestInformation();
    }

    public List<Map<String, String>> getCallStackForRequest(String requestId) {
	return dbAccessObject.getCallStackInformation(requestId);
    }

    public java.util.Map<String, String> getPieInformation(String requestID) {
	return dbAccessObject.getPieInformation(requestID);
    }

}
