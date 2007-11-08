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
 * WebContainerListener.java
 * $Id: WebContainerListener.java,v 1.13 2006/04/11 23:57:28 harpreet Exp $
 * $Date: 2006/04/11 23:57:28 $
 * $Revision: 1.13 $
 */

package com.sun.enterprise.admin.monitor.callflow;

import org.apache.catalina.Wrapper;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.core.StandardContext;
import org.apache.coyote.Adapter;
import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet; 

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.sun.enterprise.Switch;
/*
 * WebContainerListener: Listens to web events and pushes to the callflow 
 * infrastructure.
 * @author Harpreet Singh
 * @author Ram Jeyaraman
 */
public class WebContainerListener
        implements InstanceListener, ContainerListener {
    
    private static final String JSP_SERVLET = JspServlet.class.getName();
    /**
     * Thread local object.
     *
     * This motivation for this object is to ensure that the natural call flow
     * sequence is maintained. Refer to the javadoc description of Agent class.
     * The natural call flow sequence expects startTime() to be called after
     * all the addRequestInfo() calls are completed.
     *
     * In the case of the web container, we need to explicitly issue the first
     * startTime() call, just before the beforeFilter or beforeService
     * operation. However, for nested local servlet calls, called by the first
     * Servlet, there is no need to explicitly issue the startTime() call,
     * since the web container provides the beforeDispatch() event.
     * Unfortunately, the beforeDispatch() event is not available for the
     * first Servlet call, it is only available for nested local servlet
     * invocations; so we use this thread local object as a work around.
     *
     * So, this thread local object exists, primarily to support the explicit
     * issuance of the first startTime() call and its corollary endTime() call,
     * during the invocation of the first Servlet or filter in the invocation
     * path.
     */
    private static final ThreadLocal<AgentImpl.FlowStack<Boolean>> threadLocal =
        new ThreadLocal() {
            protected AgentImpl.FlowStack<Boolean> initialValue() {
                return new AgentImpl.FlowStack<Boolean>();
            }
        };
    
       
        private static final ThreadLocal<Boolean> requestStartTls =
          new ThreadLocal() {
            protected Boolean initialValue(){
                return Boolean.FALSE;
            }
        };
        
    private Agent callFlowAgent;
    
    public WebContainerListener() {
        this.callFlowAgent = Switch.getSwitch().getCallFlowAgent();
    }
    
    public void instanceEvent(InstanceEvent event) {

        if (!callFlowAgent.isEnabled()) {
            return;
        }
        
        if (!requestStartTls.get ())
            return;        

        if (event.getType().equals(InstanceEvent.BEFORE_SERVICE_EVENT)) {
            AgentImpl.FlowStack<Boolean> flowStack = threadLocal.get();
            if (flowStack.size() == 0) {
                // Explicitly issue first startTime() only for the first
                // Servlet or filter call, in the invocation path.
                processBeforeDispatchEvent(event);
            }
            flowStack.push(Boolean.TRUE);
            processBeforeServiceEvent(event);
        } else if (event.getType().equals(InstanceEvent.AFTER_SERVICE_EVENT)) {
            processAfterServiceEvent(event);
            AgentImpl.FlowStack<Boolean> flowStack = threadLocal.get();
            flowStack.pop();
            if (flowStack.size() == 0) {
                // Explicitly issue the matching endTime() for the first
                // Servlet or filter call, in the invocation path.
                processAfterDispatchEvent(event);
            }
        } else if (event.getType().equals(InstanceEvent.BEFORE_FILTER_EVENT)) {
            AgentImpl.FlowStack<Boolean> flowStack = threadLocal.get();
            if (flowStack.size() == 0) {
                // Explicitly issue first startTime() only for the first
                // Servlet or filter call, in the invocation path.
                processBeforeDispatchEvent(event);
            }
            flowStack.push(Boolean.TRUE);
            processBeforeFilterEvent(event);
        } else if (event.getType().equals(InstanceEvent.AFTER_FILTER_EVENT)) {
            processAfterFilterEvent(event);
            AgentImpl.FlowStack<Boolean> flowStack = threadLocal.get();
            flowStack.pop();
            if (flowStack.size() == 0) {
                // Explicitly issue the matching endTime() for the first
                // Servlet or filter call, in the invocation path.
                processAfterDispatchEvent(event);
            }
        } else if (event.getType().
                equals(InstanceEvent.BEFORE_DISPATCH_EVENT)) {
            processBeforeDispatchEvent(event);
        } else if (event.getType().equals(InstanceEvent.AFTER_DISPATCH_EVENT)) {
            processAfterDispatchEvent(event);
        }
    }
    
    private void processBeforeServiceEvent(InstanceEvent event) {        

        Servlet servlet = event.getServlet();
        ServletConfig servletConfig = servlet.getServletConfig();
        String servletName = "UNKNOWN";
        if (servletConfig != null) {
            servletName = servletConfig.getServletName();
        }
        
        HttpServletRequest req = (HttpServletRequest) event.getRequest();
        String methodName = req.getRequestURI() + ":";
        if (!servlet.getClass().getName().equalsIgnoreCase(JSP_SERVLET)){
               methodName += servlet.getClass().getName() + ".service";
        } else {
            // JSP
            methodName += extractJSPName(req) +":" + JSP_SERVLET + ".service";
        }
        String callerPrincipal = req.getRemoteUser();
        if (callerPrincipal == null) {
            if (req.getUserPrincipal() != null) {
                callerPrincipal = req.getUserPrincipal().getName();
            } else {
                callerPrincipal = "anonymous";
            }
        }

        Wrapper wrapper = event.getWrapper();
        String applicationName =
                ((StandardContext)wrapper.getParent()).getJ2EEApplication();
        if ((applicationName == null) || (applicationName.equals("null"))) {
            applicationName = "URI:" + req.getRequestURI();
        }
        String moduleName = wrapper.getParent().getName();
         
        callFlowAgent.webMethodStart(
                methodName, applicationName, moduleName,
                servletName, ComponentType.SERVLET,
                callerPrincipal);
    }
    
    private void processAfterServiceEvent(InstanceEvent event) {
        Throwable exception = event.getException();
        callFlowAgent.webMethodEnd(exception);        
    }
    
    private void processBeforeFilterEvent(InstanceEvent event) {

        Filter filter = event.getFilter();
        
        HttpServletRequest req = (HttpServletRequest) event.getRequest();
        String methodName = req.getRequestURI() + ":" +
                            filter.getClass().getName() + ".doFilter";
        String callerPrincipal = req.getRemoteUser();
        if (callerPrincipal == null) {
            if (req.getUserPrincipal() != null) {
                callerPrincipal = req.getUserPrincipal().getName();
            } else {
                callerPrincipal = "anonymous";
            }
        }
        
        Wrapper wrapper = event.getWrapper();
        String applicationName =
                ((StandardContext)wrapper.getParent()).getJ2EEApplication();
        if ((applicationName == null) || (applicationName.equals("null"))) {
            applicationName = "URI:" + req.getRequestURI();
        }
        String moduleName = wrapper.getParent().getName();
                
        callFlowAgent.webMethodStart(
                methodName, applicationName, moduleName,
                filter.getClass().getName(),
                ComponentType.SERVLET_FILTER, callerPrincipal);
    }
    
    private void processAfterFilterEvent(InstanceEvent event) {
        Throwable exception = event.getException();
        callFlowAgent.webMethodEnd(exception);         
    }
        
    private void processBeforeDispatchEvent(InstanceEvent event) {
        callFlowAgent.startTime(ContainerTypeOrApplicationType.WEB_CONTAINER);
    }
    
    private void processAfterDispatchEvent(InstanceEvent event) {
        callFlowAgent.endTime();
    }
    
    /**
     * Receive event from the Grizzly HTTP Connector. 
     * The ContainerEvent.getData() will return the current 
     * <code>RequestInfo</code>, which contains request information.
     * @param event An instance of ContainerEvent.
     */
    public void containerEvent(ContainerEvent event) { 

        if (!callFlowAgent.isEnabled()) {
            return;
        }

        Object obj = event.getData();
        if (!(obj instanceof org.apache.coyote.RequestInfo)) {
            return;
        }
        org.apache.coyote.RequestInfo 
                requestInfo = (org.apache.coyote.RequestInfo) obj;
        if (Adapter.CONNECTION_PROCESSING_STARTED.equals(event.getType())) {
           requestStartTls.set (Boolean.TRUE);            
           callFlowAgent.requestStart(RequestType.REMOTE_WEB);    
           callFlowAgent.addRequestInfo(
                            RequestInfo.CALLER_IP_ADDRESS,
                            requestInfo.getRemoteAddr());
        }  else if (Adapter.REQUEST_PROCESSING_COMPLETED.
                    equals(event.getType())) {
           callFlowAgent.requestEnd();
        }
    }
    
    private String extractJSPName  (HttpServletRequest request){
  
       String jspUri = null;

       String jspFile = (String) request.getAttribute(Constants.JSP_FILE);
       if (jspFile != null) {
           // JSP is specified via <jsp-file> in <servlet> declaration
           jspUri = jspFile;
       } else {
           /*
            * Check to see if the requested JSP has been the target of a
            * RequestDispatcher.include()
            */
           jspUri = (String) request.getAttribute(Constants.INC_SERVLET_PATH);
           if (jspUri != null) {
               /*
                * Requested JSP has been target of
                * RequestDispatcher.include(). Its path is assembled from the
                * relevant javax.servlet.include.* request attributes
                */
               String pathInfo = (String) request.getAttribute(
                                   "javax.servlet.include.path_info");
               if (pathInfo != null) {
                   jspUri += pathInfo;
               }
           } else {
               /*
                * Requested JSP has not been the target of a
                * RequestDispatcher.include(). Reconstruct its path from the
                * request's getServletPath() and getPathInfo()
                */
               jspUri = request.getServletPath();
               String pathInfo = request.getPathInfo();
               if (pathInfo != null) {
                   jspUri += pathInfo;
               }
           }
       }
     return jspUri;
    }
}
