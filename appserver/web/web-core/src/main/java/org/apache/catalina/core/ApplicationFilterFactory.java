/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.core;

import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.FilterMap;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Factory for the creation and caching of Filters and creation
 * of Filter Chains.
 *
 * @author Greg Murray
 * @author Remy Maucherat
 * @version $Revision: 1.0
 */

public final class ApplicationFilterFactory {


    // -------------------------------------------------------------- Constants

    private static ApplicationFilterFactory factory = new ApplicationFilterFactory();


    // ----------------------------------------------------------- Constructors


    /*
     * Prevent instantiation outside of the getInstanceMethod().
     */
    private ApplicationFilterFactory() {
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the factory instance.
     */
    public static ApplicationFilterFactory getInstance() {
        return factory;
    }


    /**
     * Construct and return a FilterChain implementation that will wrap the
     * execution of the specified servlet instance.  If we should not execute
     * a filter chain at all, return <code>null</code>.
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     */
    public ApplicationFilterChain createFilterChain
        (ServletRequest request, Wrapper wrapper, Servlet servlet) {
        
        // If there is no servlet to execute, return null
        if (servlet == null)
            return (null);

        // Create and initialize a filter chain object
        ApplicationFilterChain filterChain = null;
        /** IASRI 4665318
        if ((securityManager == null) && (request instanceof Request)) {
            Request req = (Request) request;
            filterChain = (ApplicationFilterChain) req.getFilterChain();
            if (filterChain == null) {
                filterChain = new ApplicationFilterChain();
                req.setFilterChain(filterChain);
            }
        } else {
            // Security: Do not recycle
            filterChain = new ApplicationFilterChain();
        }

        filterChain.setServlet(servlet);

        filterChain.setSupport
            (((StandardWrapper)wrapper).getInstanceSupport());
        */

        // Acquire the filter mappings for this Context
        StandardContext context = (StandardContext) wrapper.getParent();
        List<FilterMap> filterMaps = context.findFilterMaps();

        // If there are no filter mappings, we are done
        if (filterMaps.isEmpty()) {
            return (filterChain);
        }

        // get the dispatcher type
        DispatcherType dispatcher = request.getDispatcherType();
        String requestPath = null;
        Object attribute = request.getAttribute(
            Globals.DISPATCHER_REQUEST_PATH_ATTR);
        if (attribute != null){
            requestPath = attribute.toString();
        }

        // Acquire the information we will need to match filter mappings
        String servletName = wrapper.getName();

        int n = 0;

        // Add the relevant path-mapped filters to this filter chain
        Iterator<FilterMap> i = filterMaps.iterator(); 
        while (i.hasNext()) {
            FilterMap filterMap = i.next();
            if (!filterMap.getDispatcherTypes().contains(dispatcher)) {
                continue;
            }
            /* SJSWS 6324431
            if (!matchFiltersURL(filterMaps[i], requestPath))
                continue;
            */
            // START SJSWS 6324431
            if (!matchFiltersURL(filterMap, requestPath, 
                                 context.isCaseSensitiveMapping()))
                continue;
            // END SJSWS 6324431
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMap.getFilterName());
            if (filterConfig == null) {
                // FIXME - log configuration problem
                continue;
            }
            // START IASRI 4665318
            // Create a filter chain only when there are filters to add
            if (filterChain == null)
                filterChain = internalCreateFilterChain(request, wrapper,
                                                        servlet);
            // END IASRI 4665318
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Add filters that match on servlet name second
        i = filterMaps.iterator(); 
        while (i.hasNext()) {
            FilterMap filterMap = i.next();
            if (!filterMap.getDispatcherTypes().contains(dispatcher)) {
                continue;
            }
            if (!matchFiltersServlet(filterMap, servletName))
                continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMap.getFilterName());
            if (filterConfig == null) {
                // FIXME - log configuration problem
                continue;
            }
            // START IASRI 4665318
            // Create a filter chain only when there are filters to add
            if (filterChain == null)
                filterChain = internalCreateFilterChain(request, wrapper,
                                                        servlet);
            // END IASRI 4665318
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Return the completed filter chain
        return (filterChain);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return <code>true</code> if the context-relative request path
     * matches the requirements of the specified filter mapping;
     * otherwise, return <code>null</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    /* SJSWS 6324431
    private boolean matchFiltersURL(FilterMap filterMap, String requestPath) {
    */
    // START SJSWS 6324431
    private boolean matchFiltersURL(FilterMap filterMap, String requestPath,
                                    boolean caseSensitiveMapping) {
    // END SJSWS 6324431

        if (requestPath == null)
            return (false);

        // Match on context relative request path
        String testPath = filterMap.getURLPattern();
        if (testPath == null)
            return (false);

        // START SJSWS 6324431
        if (!caseSensitiveMapping) {
            requestPath = requestPath.toLowerCase(Locale.ENGLISH);
            testPath = testPath.toLowerCase(Locale.ENGLISH);
        }
        // END SJSWS 6324431

        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
            return (true);

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
            return (true);
        if (testPath.endsWith("/*")) {
            if (testPath.regionMatches(0, requestPath, 0, 
                                       testPath.length() - 2)) {
                if (requestPath.length() == (testPath.length() - 2)) {
                    return (true);
                } else if ('/' == requestPath.charAt(testPath.length() - 2)) {
                    return (true);
                }
            }
            return (false);
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) 
                && (period != requestPath.length() - 1)
                && ((requestPath.length() - period) 
                    == (testPath.length() - 1))) {
                return (testPath.regionMatches(2, requestPath, period + 1,
                                               testPath.length() - 2));
            }
        }

        // Case 4 - "Default" Match
        return (false); // NOTE - Not relevant for selecting filters

    }


    /**
     * Return <code>true</code> if the specified servlet name matches
     * the requirements of the specified filter mapping; otherwise
     * return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param servletName Servlet name being checked
     */
    private boolean matchFiltersServlet(FilterMap filterMap, 
                                        String servletName) {

        if (servletName == null) {
            return (false);
        } else {
            if (servletName.equals(filterMap.getServletName())
                    || "*".equals(filterMap.getServletName())) {
                return (true);
            } else {
                return false;
            }
        }

    }


    // START IASRI 4665318
    private ApplicationFilterChain internalCreateFilterChain(ServletRequest request, Wrapper wrapper, Servlet servlet) {
        ApplicationFilterChain filterChain = null;
        if (!Globals.IS_SECURITY_ENABLED && (request instanceof Request)) {
            Request req = (Request) request;
            filterChain = (ApplicationFilterChain) req.getFilterChain();
            if (filterChain == null) {
                filterChain = new ApplicationFilterChain();
                req.setFilterChain(filterChain);
            }
        } else {
            // Security: Do not recycle
            filterChain = new ApplicationFilterChain();
        }

        filterChain.setServlet(servlet);
        filterChain.setWrapper((StandardWrapper)wrapper);

        return filterChain;
    }
    // END IASRI 4665318


}
