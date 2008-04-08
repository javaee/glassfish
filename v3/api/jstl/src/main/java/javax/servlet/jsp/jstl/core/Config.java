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

package javax.servlet.jsp.jstl.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * Class supporting access to configuration settings.
 */
public class Config {

    /*
     * I18N/Formatting actions related configuration data
     */
    
    /**
     * Name of configuration setting for application- (as opposed to browser-)
     * based preferred locale
     */
    public static final String FMT_LOCALE
	= "javax.servlet.jsp.jstl.fmt.locale";

    /**
     * Name of configuration setting for fallback locale
     */
    public static final String FMT_FALLBACK_LOCALE
	= "javax.servlet.jsp.jstl.fmt.fallbackLocale";

    /**
     * Name of configuration setting for i18n localization context
     */
    public static final String FMT_LOCALIZATION_CONTEXT
	= "javax.servlet.jsp.jstl.fmt.localizationContext";

    /**
     * Name of localization setting for time zone
     */
    public static final String FMT_TIME_ZONE
	= "javax.servlet.jsp.jstl.fmt.timeZone";

    /*
     * SQL actions related configuration data
     */

    /**
     * Name of configuration setting for SQL data source
     */
    public static final String SQL_DATA_SOURCE
	= "javax.servlet.jsp.jstl.sql.dataSource";

    /**
     * Name of configuration setting for maximum number of rows to be included
     * in SQL query result
     */
    public static final String SQL_MAX_ROWS
	= "javax.servlet.jsp.jstl.sql.maxRows";
	
    /*
     * Private constants
     */
    private static final String PAGE_SCOPE_SUFFIX = ".page";
    private static final String REQUEST_SCOPE_SUFFIX = ".request";
    private static final String SESSION_SCOPE_SUFFIX = ".session";
    private static final String APPLICATION_SCOPE_SUFFIX = ".application";

    /**
     * Looks up a configuration variable in the given scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param pc Page context in which the configuration variable is to be
     * looked up
     * @param name Configuration variable name
     * @param scope Scope in which the configuration variable is to be looked
     * up
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(PageContext pc, String name, int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    return pc.getAttribute(name + PAGE_SCOPE_SUFFIX, scope);
	case PageContext.REQUEST_SCOPE:
	    return pc.getAttribute(name + REQUEST_SCOPE_SUFFIX, scope);
	case PageContext.SESSION_SCOPE:
	    return get(pc.getSession(), name);
	case PageContext.APPLICATION_SCOPE:
	    return pc.getAttribute(name + APPLICATION_SCOPE_SUFFIX, scope);
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }

    /**
     * Looks up a configuration variable in the "request" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param request Request object in which the configuration variable is to
     * be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(ServletRequest request, String name) {
	return request.getAttribute(name + REQUEST_SCOPE_SUFFIX);
    }

    /**
     * Looks up a configuration variable in the "session" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.</p>
     *
     * @param session Session object in which the configuration variable is to
     * be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined, if session is null, or if the session
     * is invalidated. 
     */
    public static Object get(HttpSession session, String name) {
        Object ret = null;
        if (session != null) {
            try {
                ret = session.getAttribute(name + SESSION_SCOPE_SUFFIX);
            } catch (IllegalStateException ex) {} // when session is invalidated
        }
        return ret;
    }

    /**
     * Looks up a configuration variable in the "application" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param context Servlet context in which the configuration variable is
     * to be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(ServletContext context, String name) {
	return context.getAttribute(name + APPLICATION_SCOPE_SUFFIX);
    }

    /**
     * Sets the value of a configuration variable in the given scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param pc Page context in which the configuration variable is to be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     * @param scope Scope in which the configuration variable is to be set
     */
    public static void set(PageContext pc, String name, Object value,
			   int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    pc.setAttribute(name + PAGE_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.REQUEST_SCOPE:
	    pc.setAttribute(name + REQUEST_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.SESSION_SCOPE:
	    pc.setAttribute(name + SESSION_SCOPE_SUFFIX, value, scope);
	    break;
	case PageContext.APPLICATION_SCOPE:
	    pc.setAttribute(name + APPLICATION_SCOPE_SUFFIX, value, scope);
	    break;
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }

    /**
     * Sets the value of a configuration variable in the "request" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param request Request object in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(ServletRequest request, String name, Object value) {
	request.setAttribute(name + REQUEST_SCOPE_SUFFIX, value);
    }

    /**
     * Sets the value of a configuration variable in the "session" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param session Session object in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(HttpSession session, String name, Object value) {
	session.setAttribute(name + SESSION_SCOPE_SUFFIX, value);
    }

    /**
     * Sets the value of a configuration variable in the "application" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param context Servlet context in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(ServletContext context, String name, Object value) {
	context.setAttribute(name + APPLICATION_SCOPE_SUFFIX, value);
    }
 
    /**
     * Removes a configuration variable from the given scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param pc Page context from which the configuration variable is to be
     * removed
     * @param name Configuration variable name
     * @param scope Scope from which the configuration variable is to be 
     * removed
     */
    public static void remove(PageContext pc, String name, int scope) {
	switch (scope) {
	case PageContext.PAGE_SCOPE:
	    pc.removeAttribute(name + PAGE_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.REQUEST_SCOPE:
	    pc.removeAttribute(name + REQUEST_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.SESSION_SCOPE:
	    pc.removeAttribute(name + SESSION_SCOPE_SUFFIX, scope);
	    break;
	case PageContext.APPLICATION_SCOPE:
	    pc.removeAttribute(name + APPLICATION_SCOPE_SUFFIX, scope);
	    break;
	default:
	    throw new IllegalArgumentException("unknown scope");
	}
    }

    /**
     * Removes a configuration variable from the "request" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     * 
     * @param request Request object from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(ServletRequest request, String name) {
	request.removeAttribute(name + REQUEST_SCOPE_SUFFIX);
    }

    /**
     * Removes a configuration variable from the "session" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param session Session object from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(HttpSession session, String name) {
	session.removeAttribute(name + SESSION_SCOPE_SUFFIX);
    }

    /**
     * Removes a configuration variable from the "application" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param context Servlet context from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(ServletContext context, String name) {
	context.removeAttribute(name + APPLICATION_SCOPE_SUFFIX);
    }
 
    /**
     * Finds the value associated with a specific configuration setting
     * identified by its context initialization parameter name.
     *
     * <p> For each of the JSP scopes (page, request, session, application),
     * get the value of the configuration variable identified by <tt>name</tt>
     * using method <tt>get()</tt>. Return as soon as a non-null value is
     * found. If no value is found, get the value of the context initialization
     * parameter identified by <tt>name</tt>.
     *
     * @param pc Page context in which the configuration setting is to be 
     * searched
     * @param name Context initialization parameter name of the configuration
     * setting
     * 
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * setting identified by <tt>name</tt>, or null if it is not defined.
     */
    public static Object find(PageContext pc, String name) {
	Object ret = get(pc, name, PageContext.PAGE_SCOPE);
	if (ret == null) {
	    ret = get(pc, name, PageContext.REQUEST_SCOPE);
	    if (ret == null) {
		if (pc.getSession() != null) {
		    // check session only if a session is present
		    ret = get(pc, name, PageContext.SESSION_SCOPE);
		}
		if (ret == null) {
		    ret = get(pc, name, PageContext.APPLICATION_SCOPE);
		    if (ret == null) {
			ret = pc.getServletContext().getInitParameter(name);
		    }
		}
	    }
	}

	return ret;
    }
}
