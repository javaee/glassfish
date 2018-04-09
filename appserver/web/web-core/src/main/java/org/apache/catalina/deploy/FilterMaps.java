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

package org.apache.catalina.deploy;

import javax.servlet.DispatcherType;
import java.util.Set;

/**
 * Representation of a filter mapping for a web application, as represented
 * in a <code>&lt;filter-mapping&gt;</code> element in the deployment
 * descriptor.  Each filter mapping must contain a filter name and any 
 * number of URL patterns and servlet names.
 *
 */

public class FilterMaps {

    private String[] urlPatterns = new String[0];
    private String[] servletNames = new String[0];
    private String filterName = null;
    private Set<DispatcherType> dispatcherTypes;

    // ------------------------------------------------------------ Properties
    
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterName() {
        return filterName;
    }

    public void addServletName(String servletName) {
        String[] results = new String[servletNames.length + 1];
        System.arraycopy(servletNames, 0, results, 0, servletNames.length);
        results[servletNames.length] = servletName;
        servletNames = results;
    }

    public String[] getServletNames() {
        return servletNames;
    }

    public void addURLPattern(String urlPattern) {
        String[] results = new String[urlPatterns.length + 1];
        System.arraycopy(urlPatterns, 0, results, 0, urlPatterns.length);
        results[urlPatterns.length] = urlPattern;
        urlPatterns = results;
    }

    public String[] getURLPatterns() {
        return urlPatterns;
    }
    
    public void setDispatcherTypes(Set<DispatcherType> dispatcherTypes) {
        this.dispatcherTypes = dispatcherTypes;
    }

    public Set<DispatcherType> getDispatcherTypes() {
        return dispatcherTypes;
    }
}
