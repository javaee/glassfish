/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.grizzly.test.http2;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http2.Http2Stream;

/**
 * A simple Http2 response based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpResponse {
    private HttpContent httpContent;
    private HttpResponsePacket response;
    private boolean push = false;
    private HttpPushPromise pushPromise;
    private Map<String, List<String>> headerMap = new HashMap<>();
    private Map<String, String> trailerMap = null;

    HttpResponse(HttpContent httpContent, Map<String, String> trailerMap) {
        this.httpContent = httpContent;
        this.response = (HttpResponsePacket)httpContent.getHttpHeader();
        this.push = Http2Stream.getStreamFor(response).isPushStream();
        MimeHeaders headers = response.getHeaders();
        for (String name : headers.names()) {
            List<String> list = new ArrayList<>();
            for (String value : headers.values(name)) {
                list.add(value);
            }
            list = Collections.unmodifiableList(list);
            headerMap.put(name, list);
        }
        headerMap = Collections.unmodifiableMap(headerMap);
        this.trailerMap = Collections.unmodifiableMap(trailerMap);
        if (this.push) {
            pushPromise = new HttpPushPromise(httpContent);
        }
    }

    public int getStatus() {
        return response.getStatus();
    }

    public long getContentLength() {
        return response.getContentLength();
    }

    public String getHeader(String key) {
        return response.getHeader(key);
    }

    public Map<String, List<String>> getHeaders() {
        return headerMap;
    }

    public Map<String, String> getTrailerFields() {
        return trailerMap;
    }

    public String getBody() {
        return httpContent.getContent().toStringContent();
    }

    public String getBody(String charset) {
        return httpContent.getContent().toStringContent(Charset.forName(charset));
    }

    public boolean isPush() {
        return push;
    }

    public HttpPushPromise getHttpPushPromise() {
        return pushPromise;
    }
}
