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
package javax.interceptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Context information passed to AroundInvoke and 
 * Interceptor-class lifecycle callback methods.
 */
public interface InvocationContext {

    /**
     * Returns the target instance. 
     */
    public Object getTarget();

    /**
     * Returns the method of the bean class for which the interceptor
     * was invoked.  For AroundInvoke methods, this is the business
     * method on the bean class. For lifecycle callback methods, 
     * returns null.
     */
    public Method getMethod();

    /**
     * Returns the parameters that will be used to invoke
     * the business method.  If setParameters has been called, 
     * getParameters() returns the values to which the parameters 
     * have been set.  
     * 
     * @exception java.lang.IllegalStateException if invoked within
     * a lifecycle callback method.
     */
    public Object[] getParameters();
    
    /**
     * Sets the parameters that will be used to invoke the 
     * business method.  
     *
     * @exception java.lang.IllegalStateException if invoked within
     * a lifecycle callback method.
     *
     * @exception java.lang.IllegalArgumentException if the parameter types do not match 
     * the types for the business method, or the number of parameters supplied does not
     * equal the number of parameters for the business method.
     */
    public void setParameters(Object[] params);

    /**
     * Returns the context data associated with this invocation or
     * lifecycle callback.  If there is no context data, an
     * empty Map<String,Object> object will be returned.  
     */
    public Map<String, Object> getContextData();

    /**
     * Proceed to the next entry in the interceptor chain.
     * The proceed method returns the result of the next
     * method invoked.  If the method returns void, proceed
     * returns null.
     */
    public Object proceed() throws Exception;

} 
