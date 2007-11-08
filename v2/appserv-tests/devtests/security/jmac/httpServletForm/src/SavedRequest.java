/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.httpservletform;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

final class SavedRequest implements Serializable {
    private String method = null;
    private String requestURI = null;
    private String queryString = null;

    SavedRequest(HttpServletRequest hreq) {
        method = hreq.getMethod();
        requestURI = hreq.getRequestURI();
        queryString = hreq.getQueryString();
    }

    String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    String getRequestURI() {
        return requestURI;
    }

    void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    String getQueryString() {
        return queryString;
    }

    void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
