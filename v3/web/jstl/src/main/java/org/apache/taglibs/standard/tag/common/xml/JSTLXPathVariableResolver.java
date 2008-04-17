/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taglibs.standard.tag.common.xml;

import javax.xml.xpath.XPathVariableResolver;
import javax.xml.namespace.QName;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;


/**
  * The XPathVariableResolver implementation provides access to user
  * XPath variables.
  */
public class JSTLXPathVariableResolver implements XPathVariableResolver {

    //*********************************************************************
    // Support for XPath evaluation

    private PageContext pageContext;

    // The URLs
    private static final String PAGE_NS_URL = 
            "http://java.sun.com/jstl/xpath/page";
    private static final String REQUEST_NS_URL = 
            "http://java.sun.com/jstl/xpath/request";
    private static final String SESSION_NS_URL = 
            "http://java.sun.com/jstl/xpath/session";
    private static final String APP_NS_URL = 
            "http://java.sun.com/jstl/xpath/app";
    private static final String PARAM_NS_URL = 
            "http://java.sun.com/jstl/xpath/param";
    private static final String INITPARAM_NS_URL = 
            "http://java.sun.com/jstl/xpath/initParam";
    private static final String COOKIE_NS_URL = 
            "http://java.sun.com/jstl/xpath/cookie";
    private static final String HEADER_NS_URL = 
            "http://java.sun.com/jstl/xpath/header";
    
    //*********************************************************************
    // Constructor

    public JSTLXPathVariableResolver(PageContext pc) {
        pageContext = pc;
    }

    /**
     * Find variable in set of variables
     *
     * @param QName variable name
     *
     * @return QName variables value
     *
     * @throws NullPointerException if variable name is null
     */
    public Object resolveVariable(QName qname) throws NullPointerException {

        Object varObject = null;

        if (qname == null) {
            throw new NullPointerException("Cannot resolve null variable");
        }

        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();
        // p("[resolveVariable] namespace: " + namespace + " prefix: " + prefix + " localName: " + localName);

        try {
            varObject = getVariableValue(namespace, prefix, localName);
        } catch (UnresolvableException ue) {
            System.out.println("JSTLXpathVariableResolver.resolveVariable threw UnresolvableException: " + ue);
        }

        // p("[resolveVariable] varObject: " + varObject);
        return varObject;
    }

    /** 
     * Retrieve an XPath's variable value using JSTL's custom 
     * variable-mapping rules
     */ 
    protected Object getVariableValue(String namespace,
                                      String prefix,
                                      String localName) 
                                      throws UnresolvableException {       
        // p("resolving: ns=" + namespace + " prefix=" + prefix + " localName=" + localName);  
        // We can match on namespace with Xalan but leaving as is
        // [ I 'd prefer to match on namespace, but this doesn't appear
        // to work in Jaxen] 
        if (namespace == null || namespace.equals("")) {
            return notNull(
            pageContext.findAttribute(localName),
            namespace,
            localName);
        } else if (namespace.equals(PAGE_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,PageContext.PAGE_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(REQUEST_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.REQUEST_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(SESSION_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.SESSION_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(APP_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.APPLICATION_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(PARAM_NS_URL)) {
            return notNull(
            pageContext.getRequest().getParameter(localName),
            namespace,
            localName);            
        } else if (namespace.equals(INITPARAM_NS_URL)) {
            return notNull(
            pageContext.getServletContext().
            getInitParameter(localName),
            namespace,
            localName);
        } else if (namespace.equals(HEADER_NS_URL)) {
            HttpServletRequest hsr =
            (HttpServletRequest) pageContext.getRequest();
            return notNull(
            hsr.getHeader(localName),
            namespace,
            localName);
        } else if (namespace.equals(COOKIE_NS_URL)) {
            HttpServletRequest hsr =
            (HttpServletRequest) pageContext.getRequest();
            Cookie[] c = hsr.getCookies();
            for (int i = 0; i < c.length; i++)
                if (c[i].getName().equals(localName))
                    return c[i].getValue();
            throw new UnresolvableException("$" + namespace + ":" + localName);
        } else {
            throw new UnresolvableException("$" + namespace + ":" + localName);
        }
    }   

    /**
     * Validate that the Object returned is not null. If it is
     * null, throw an exception.
     */
    private Object notNull(Object o, String namespace, String localName)
        throws UnresolvableException {
        if (o == null) {
            throw new UnresolvableException("$" + (namespace==null?"":namespace+":") + localName);
        }
        // p("resolved to: " + o);
        return o;
    } 
    
    //*********************************************************************
    // Utility methods

    private static void p(String s) {
        System.out.println("[JSTLXPathVariableResolver] " + s);
    }

}
