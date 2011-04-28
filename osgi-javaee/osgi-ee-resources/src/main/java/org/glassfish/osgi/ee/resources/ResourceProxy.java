/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.osgi.ee.resources;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * ResourceProxy that can delegate to actual objects upon usage.<br>
 * Does not cache the actual object as the actual object can be re-configured<br>
 *
 * @author Jagadish Ramu
 */
public class ResourceProxy implements InvocationHandler, Invalidate {

    private String jndiName;
    private boolean invalidated = false;

    public ResourceProxy(String jndiName) {
        this.jndiName = jndiName;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        if (method.getName().equals("invalidate")) {
            invalidate();
        } else {
            result = method.invoke(getActualObject(), args);
        }
        return result;
    }

    /**
     * It is possible that reconfiguration of resource will happen.<br>
     * Always do lookup.
     */
    private Object getActualObject() {
        if (!invalidated) {
            try {
                return new InitialContext().lookup(jndiName);
            } catch (NamingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Resource [" + jndiName + "] is invalidated");
        }
    }

    /**
     * Sets the state of the proxy as invalid<br>
     */
    public void invalidate() {
        invalidated = true;
    }
}
