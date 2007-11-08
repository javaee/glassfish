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

package com.sun.enterprise.admin.server.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Enumeration;

import javax.management.ObjectName;
import javax.management.Attribute;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ElementChangeHelper;
import com.sun.enterprise.admin.util.proxy.CallStack;
import com.sun.enterprise.admin.util.proxy.Call;
import com.sun.enterprise.admin.util.proxy.InterceptorImpl;
import com.sun.enterprise.admin.event.EventStack;
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.audit.AuditManagerFactory;

import com.sun.enterprise.admin.event.pluggable.RestartEventHelper;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.admin.event.pluggable.NotificationFactory;


/**
 * Config interceptor for MBeans
 */
public class ConfigInterceptor extends InterceptorImpl {

    private AdminContext adminContext;
    private Logger _logger;
    private static AuditManager auditManager =
            AuditManagerFactory.getAuditManagerInstance();

    public ConfigInterceptor() {
    }

    public ConfigInterceptor(AdminContext ctx) {
        adminContext = ctx;
        if (adminContext != null) {
            _logger = adminContext.getAdminLogger();
        } else {
            _logger = Logger.getLogger("global");
        }
    }

    /**
     * We flush configcontext only in the last return of
     * setAttribute, setAttributes or invoke. 
     * Send notification if flush is successfull
     *
     * Failure cases will be handled later
     */
    public void postInvoke(CallStack callStack) {

        
        if (callStack.getStackSize() == 1) {
            Call call = callStack.getActiveCall();
            if (call.getState().isSuccess() && isFlushNeeded(call)) {
                try {
                    ConfigContext ctx = adminContext.getAdminConfigContext();
                    if (ctx.isChanged()) {
                        ctx.flush();
                    }
                } catch (ConfigException ce) {
                    _logger.log(Level.WARNING,
                        "core.config_exception_occurred", ce);
                }
            }
            if (isFlushNeeded(call)) {
                new AdminNotificationHelper(adminContext).sendNotification();
            }
        } 
        auditInvocation(callStack.getActiveCall());

        
    }
    
    /**
     * Flush is needed only for those methods that change 
     * configcontext. These are setAttribute(s) and invoke
     */
    private boolean isFlushNeeded(Call call) {
        String methodName = call.getMethod().getName();
        if( methodName.equals("setAttribute") ||
            methodName.equals("setAttributes") ||
            (methodName.equals("invoke") && !isReadonlyInvokeOperation(call))) {
                return true;
        }
        return false;
    }

    private boolean isReadonlyInvokeOperation(Call call) {

        try {
            Object[] args = call.getArguments();
            if(args!=null && args.length>1)
            {
                String operationName = (String) (args[1]);

                // skips notification if this is a ping for deployment status
                if("takeNotifications".equals(operationName)) {
                    return true;
                }
            }
        } catch(Throwable t){ }

        return false;
    }

    public void preInvoke(CallStack callstack) {

        if (callstack.getStackSize() == 1) {
            EventStack stack = new EventStack();
            stack.setConfigContext(adminContext.getAdminConfigContext());
            EventContext.setEventStackToThreadLocal(stack);
        }

    }

/*
    private void sendNotification() {
        ConfigContext context = adminContext.getAdminConfigContext();
        String instanceName = adminContext.getServerName();
        AdminEventCache cache =
                AdminEventCache.getInstance(instanceName);
        cache.setAdminConfigContext(context);
        ArrayList changeList = context.getConfigChangeList();
        context.resetConfigChangeList();
        ArrayList eventList = null;
        if (changeList.size() <= 0) {
            eventList = new ArrayList();
            // Return, no changes to process
            //return;
        }
        else
        {
            cache.processConfigChangeList(changeList, false, false);
            eventList = cache.getAndResetCachedEvents();

            //***********************************
            //ElementChange events 
            try {
            ElementChangeHelper elementHelper = new ElementChangeHelper(); //FIXME: need to put it as member
            AdminEvent[] elementChangeEvents = elementHelper.generateElementChangeEventsFromChangeList(instanceName, changeList, context);
            if(elementChangeEvents!=null)
                for(int i=0; i<elementChangeEvents.length; i++)
                    eventList.add(elementChangeEvents[i]);
            } catch (Exception e) {
                // ignore
            }
            //***********************************

        }
        EventStack stack = EventContext.getEventStackFromThreadLocal();
        List newEventList = stack.getEvents();
        newEventList.addAll(eventList);

        boolean requiresRestart = false;

        Iterator iter = newEventList.iterator();
        while (iter.hasNext()) {
            AdminEvent event = (AdminEvent)iter.next();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "mbean.event_sent",
                        event.getEventInfo());
            } else {
                _logger.log(Level.INFO, "mbean.send_event", event.toString());
            }
            AdminEventResult result=AdminEventMulticaster.multicastEvent(event);
            _logger.log(Level.FINE, "mbean.event_res", result.getResultCode());
            _logger.log(Level.FINEST, "mbean.event_reply",
                    result.getAllMessagesAsString());
            if (!AdminEventResult.SUCCESS.equals(result.getResultCode())) {
                requiresRestart = true;
                cache.setRestartNeeded(true);
                // if there was an error in the listener, admin event
                // multicaster already sets the restart required to true
                //_logger.log(Level.INFO, "mbean.notif_failed");
                _logger.log(Level.INFO, "mbean.event_result_error",
                        result.getAllResultCodesAsString() );
            }

        }
        ServerContext svcCtx = ApplicationServer.getServerContext();
        PluggableFeatureFactory featureFactory = null;
        if (svcCtx != null) {
            featureFactory = svcCtx.getPluggableFeatureFactory();
            
            // see if there were any non reconfigurabled changes
            NotificationFactory nFactory = 
                featureFactory.getNotificationFactory();
            RestartEventHelper helper = nFactory.createRestartEventHelper();
            helper.setRestartRequiredForTarget(context, changeList);
            
        }
        return;
    }
*/


    //*****************************************************************************
    /*
     * ADMIN AUDIT FOR CALL
     */
    private void auditInvocation(Call call)
    {
        if(auditManager.isAuditOn())
        {
            if (/*auditManager.isAuditOn() &&*/ isAuditable(call)){
                SecurityContext sc = SecurityContext.getCurrent();
//XXX remove audit API
/*
                auditManager.adminInvocation(
                    sc.getCallerPrincipal().getName(), //caller
                    sc.getSubject(), //subject
                    auditManager.getRemoteHost(), // host
                    getObjectNameForCall(call),//ObjectName
                    getOperationName(call), //operation name
                    "JMXLocal",  //mode JMXLocal,JMXRemote-http,JMXRemote-rmi,JMXRemote-jmxmp
                    getAuditedParamsString(call),
                    call.getState().isSuccess());
*/
/*
System.out.println(
"\nCaller: "+sc.getCallerPrincipal().getName() +
"\nSubject: "+ sc.getSubject() +
"\nHost: "+ auditManager.getRemoteHost() +
"\nObjectName: "+  getObjectNameForCall(call)+
"\nOperation: "+  getOperationName(call)+
"\nParams: "+  getAuditedParamsString(call)+
"\nSuccess: "+  call.getState().isSuccess() );
*/            
            }
        }
    }
    /**
     * check call using method name/params/objectname 
     * - whether it auditable or not
     */
    private boolean isAuditable(Call call) {
        if(call==null)
            return false;
        String methodName = call.getMethod().getName();
        if( !methodName.equals("setAttribute")  &&
            !methodName.equals("setAttributes") &&
            !methodName.equals("invoke")) 
                return false;
        if(methodName.equals("invoke"))
        {
            String opName = getOperationName(call);
            if(opName==null ||
               opName.startsWith("get") ||
               opName.startsWith("list") ||
               opName.startsWith("is") )
                return false;
        }
        String objectName = getObjectNameForCall(call);
        if(objectName==null || objectName.indexOf("category=config")<0)
            return false;
        return true;
    }
    
    
    /**
     * returns OperationName invoking in MBean
     */
    private String getOperationName(Call call) {
        if(call==null)
            return null;
        String methodName = call.getMethod().getName();
        if(methodName.equals("invoke"))
        {
            Object[] params = call.getArguments();
            if(params!=null && params.length>1 && (params[1] instanceof String))
                methodName = (String)params[1];
        }
        return methodName;
    }

    /**
     * represents parameters of call in printable form fro audit module
     */
    private String getAuditedParamsString(Call call) {
        if(call==null)
            return null;
        Object[] params = call.getArguments();
        if(params==null)
            return null;
        int i=0;
        String methodName = call.getMethod().getName();
        if(methodName.equals("invoke"))
        {
            if(params.length>2)
                return paramToString(params[2]);
        }
        else
            if(methodName.equals("setAttribute") ||
                methodName.equals("setAttributes") )
            {
                if(params.length>1)
                    return paramToString(params[1]);
            }
        return null;
    }
    
    /**
     * represents parameter of call in printable form fro audit module
     */
    private String paramToString(Object obj) {
        if(obj==null)
            return null;
        String res = null;
        if(obj instanceof Object[])
        {
            res = "{";
            for(int i=0; i<((Object[])obj).length; i++)
                res += paramToString(((Object[])obj)[i]) + ";";
            res += "}";
        }
        else if(obj instanceof ArrayList)
        {
            res = "{";
            for(int i=0; i<((ArrayList)obj).size(); i++)
                res += paramToString(((ArrayList)obj).get(i)) + ";";
            res += "}";
        }
        else if(obj instanceof Properties)
        {
            res = "{";
            Enumeration keys = ((Properties)obj).keys();
            while (keys.hasMoreElements())
            {
                final String key = (String)keys.nextElement();
                res += key + "=" + ((Properties)obj).get(key) + ";";
            }
            res += "}";
        }
        else if(obj instanceof Attribute)
        {
            res = ""+((Attribute)obj).getName()+"="+((Attribute)obj).getValue();
        }
        else if(obj instanceof String ||
                obj instanceof Number ||
                obj instanceof ObjectName )
        {
            res = obj.toString();
        }
        else
            res = "<?>";
        return res;
    }

    /**
     * returns ObjectName of invoking MBean
     */
    private String getObjectNameForCall(Call call) {
        if(call==null)
            return null;
        Object[] params = call.getArguments();
        if( params==null || 
            params.length<1 ||
            !(params[0] instanceof ObjectName))
            return null;
        return params[0].toString();
    }

}
