/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.SingleThreadModel;

import org.glassfish.api.invocation.ComponentInvocation;

import java.lang.reflect.Method;

public class WebComponentInvocation extends ComponentInvocation {


    /**
     * Used by container within JAXRPC handler processing code.
     */
    private Object webServiceTie;
    private Method webServiceMethod;

    public WebComponentInvocation(WebModule wm) {
        this(wm, null);
    }

    public WebComponentInvocation(WebModule wm, Object instance) {
        setComponentInvocationType(
                ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION);
        componentId = wm.getComponentId();
        jndiEnvironment = wm.getWebBundleDescriptor();
        container = wm;
        this.instance = instance;
        setResourceTableKey(_getResourceTableKey());
    }

    private Object _getResourceTableKey() {
        Object resourceTableKey = null;
        if (instance instanceof Servlet || instance instanceof Filter) {
            // Servlet or Filter
            if (instance instanceof SingleThreadModel) {
                resourceTableKey = instance;
            } else {
                resourceTableKey =
                        new PairKey(instance, Thread.currentThread());
            }
        } else {
            resourceTableKey = instance;
        }

        return resourceTableKey;
    }

    public void setWebServiceTie(Object tie) {
        webServiceTie = tie;
    }

    public Object getWebServiceTie() {
        return webServiceTie;
    }

    public void setWebServiceMethod(Method method) {
        webServiceMethod = method;
    }

    public Method getWebServiceMethod() {
        return webServiceMethod;
    }

    class PairKey {
        private Object instance = null;
        private Thread thread = null;
        int hCode = 0;

        private PairKey(Object inst, Thread thr) {
            instance = inst;
            thread = thr;
            if (inst != null) {
                hCode = 7 * inst.hashCode();
            }
            if (thr != null) {
                hCode += thr.hashCode();
            }
        }

        @Override
        public int hashCode() {
            return hCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            boolean eq = false;
            if (obj != null && obj instanceof PairKey) {
                PairKey p = (PairKey)obj;
                if (instance != null) {
                    eq = (instance.equals(p.instance));
                } else {
                    eq = (p.instance == null);
                }

                if (eq) {
                    if (thread != null) {
                        eq = (thread.equals(p.thread));
                    } else {
                        eq = (p.thread == null);
                    }
                }
            }
            return eq;
        }
    }
}
