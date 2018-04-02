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

import org.apache.catalina.util.RequestUtil;

import javax.servlet.DispatcherType;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Representation of a filter mapping for a web application, as represented
 * in a <code>&lt;filter-mapping&gt;</code> element in the deployment
 * descriptor.  Each filter mapping must contain a filter name plus either
 * a URL pattern or a servlet name.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2007/01/23 00:06:56 $
 */

public class FilterMap implements Serializable {

    private static final EnumSet<DispatcherType> DEFAULT_DISPATCHER =
        EnumSet.of(DispatcherType.REQUEST);


    /**
     * The name of the filter with which this filter mapping is associated
     */
    private String filterName = null;    

    /**
     * The servlet name for which this filter mapping applies
     */
    private String servletName = null;

    /**
     * The URL pattern for which this filter mapping applies
     */
    private String urlPattern = null;

    /**
     * The dispatcher types of this filter mapping
     */
    private Set<DispatcherType> dispatcherTypes;


    // ------------------------------------------------------------- Properties

    public String getFilterName() {
        return (this.filterName);
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getServletName() {
        return (this.servletName);
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getURLPattern() {
        return (this.urlPattern);
    }

    public void setURLPattern(String urlPattern) {
        this.urlPattern = RequestUtil.urlDecode(urlPattern);
    }
    
    public Set<DispatcherType> getDispatcherTypes() {
        // Per the SRV.6.2.5 absence of any dispatcher elements is
        // equivelant to a REQUEST value
        return (dispatcherTypes == null || dispatcherTypes.isEmpty()) ?
            DEFAULT_DISPATCHER : dispatcherTypes;
    }

    public void setDispatcherTypes(Set<DispatcherType> dispatcherTypes) {
        this.dispatcherTypes = dispatcherTypes;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("FilterMap[");
        sb.append("filterName=");
        sb.append(this.filterName);
        if (servletName != null) {
            sb.append(", servletName=");
            sb.append(servletName);
        }
        if (urlPattern != null) {
            sb.append(", urlPattern=");
            sb.append(urlPattern);
        }
        sb.append("]");
        return (sb.toString());
    }

}
