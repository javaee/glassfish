/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.cditest.security.interceptor;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import java.io.Serializable;
import org.glassfish.cditest.security.api.Secure;

/**
 * Realizes security for EJBs.
 * 
 * @author ifischer
 * 
 */
@Secure
@Interceptor
public class SecurityInterceptor implements Serializable
{
    private static final Logger LOG = Logger.getLogger(SecurityInterceptor.class.getName());
    
    public static boolean aroundInvokeCalled = false;
    
    @Resource
    private EJBContext ejbCtx;
    
    /**
     * Perform lookup for permissions.
     * Does the caller has the permission to call the method?
     * TODO: implement lookup
     * 
     * @param InvocationContext of intercepted method
     * @return
     * @throws Exception
     */
    @AroundInvoke
    protected Object invoke(final InvocationContext ctx) throws Exception
    {
        Principal p = ejbCtx.getCallerPrincipal();
        Method interfaceMethod = ctx.getMethod();
        
        LOG.log(Level.INFO, "EJB Method called [Full]:\"{0}\" by Principal:{1}", new Object[]{getFullEJBClassName(interfaceMethod), p.toString()});
        LOG.log(Level.INFO, "EJB Method called [Methodonly]:{0} by Principal:{1}", new Object[]{interfaceMethod.getName(), p.toString()});
        
        SecurityInterceptor.aroundInvokeCalled = true;
        return ctx.proceed();
    }
    
    /**
     * The EJBContext interface doesn't provide convenient methods to get the name of the EJB class, 
     * so the classname has to be extracted from the method.
     * 
     * @param the method whose classname is needed
     * @return classname (fully qualified) of given method, e.g. "com.profitbricks.user.api.UserService"
     */
    private String getFullEJBClassName(Method method) {
        // extract className from methodName
        // methodName format example:"public void com.profitbricks.user.api.UserService.testMe()"
        String methodName = method.toString();
        
        int start = methodName.lastIndexOf(' ') + 1;
        int end = methodName.lastIndexOf('.');
        
        String className = methodName.substring(start, end);
        
        return className;
    }
    
    public static void reset(){
        //reset invocation status
        SecurityInterceptor.aroundInvokeCalled = false;
    }
}
