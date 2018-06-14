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

package org.apache.catalina.ssi;


import org.apache.catalina.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
public class SSIServletRequestUtil {
    /**
     * Return the relative path associated with this servlet. Taken from
     * DefaultServlet.java. Perhaps this should be put in
     * org.apache.catalina.util somewhere? Seems like it would be widely used.
     * 
     * @param request
     *            The servlet request we are processing
     */
    public static String getRelativePath(HttpServletRequest request) {
        // Are we being processed by a RequestDispatcher.include()?
        if (request.getAttribute("javax.servlet.include.request_uri") != null) {
            String result = (String)request
                    .getAttribute("javax.servlet.include.path_info");
            if (result == null)
                result = (String)request
                        .getAttribute("javax.servlet.include.servlet_path");
            if ((result == null) || (result.equals(""))) result = "/";
            return (result);
        }
        // No, extract the desired path directly from the request
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return RequestUtil.normalize(result);
    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out. If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements are
     * present), return <code>null</code> instead. This normalize should be
     * the same as DefaultServlet.normalize, which is almost the same ( see
     * source code below ) as RequestUtil.normalize. Do we need all this
     * duplication?
     * 
     * @param path
     *            Path to be normalized
     * @deprecated
     */
    public static String normalize(String path) {
        return RequestUtil.normalize(path);
    }
}
