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

/**
 * Class representing a servlet mapping containing multiple URL patterns.
 * See Servlet 2.5, SRV.18.0.3 ("Multiple Occurrences of Servlet Mappings")
 * for details.
 */
public class ServletMap {
    String servletName;
    String[] urlPatterns = new String[0];
    
    public void setServletName(String name) {
        servletName = name;
    }

    public void addURLPattern(String pattern) {
        String[] results = new String[urlPatterns.length + 1];
        System.arraycopy(urlPatterns, 0, results, 0, urlPatterns.length);
        results[urlPatterns.length] = pattern;
        urlPatterns = results;
    }

    public String getServletName() {
        return servletName;
    }
        
    public String[] getURLPatterns() {
        return urlPatterns;
    }
}
