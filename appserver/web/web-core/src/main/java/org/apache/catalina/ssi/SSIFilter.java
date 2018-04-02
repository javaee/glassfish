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


import org.apache.catalina.Globals;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Filter to process SSI requests within a webpage. Mapped to a content types
 * from within web.xml.
 * 
 * @author David Becker
 * @version $Revision: 1.1 $, $Date: 2007/02/13 19:16:21 $
 * @see org.apache.catalina.ssi.SSIServlet
 */
public class SSIFilter implements Filter {
	protected FilterConfig config = null;
    /** Debug level for this servlet. */
    protected int debug = 0;
    /** Expiration time in seconds for the doc. */
    protected Long expires = null;
    /** virtual path can be webapp-relative */
    protected boolean isVirtualWebappRelative = false;
    /** regex pattern to match when evaluating content types */
	protected Pattern contentTypeRegEx = null;
	/** default pattern for ssi filter content type matching */
	protected Pattern shtmlRegEx =
        Pattern.compile("text/x-server-parsed-html(;.*)?");


    //----------------- Public methods.
    /**
     * Initialize this servlet.
     * 
     * @exception ServletException
     *                if an error occurs
     */
    public void init(FilterConfig config) throws ServletException {
    	this.config = config;
    	
        if (config.getInitParameter("debug") != null) {
            debug = Integer.parseInt(config.getInitParameter("debug"));
        }

        if (config.getInitParameter("contentType") != null) {
            contentTypeRegEx = Pattern.compile(config.getInitParameter("contentType"));
        } else {
            contentTypeRegEx = shtmlRegEx;
        }

        isVirtualWebappRelative = 
            Boolean.parseBoolean(config.getInitParameter("isVirtualWebappRelative"));

        if (config.getInitParameter("expires") != null)
            expires = Long.valueOf(config.getInitParameter("expires"));

        if (debug > 0)
            config.getServletContext().log(
                    "SSIFilter.init() SSI invoker started with 'debug'=" + debug);
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        // cast once
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        
        // indicate that we're in SSI processing
        req.setAttribute(Globals.SSI_FLAG_ATTR, "true");           

        // setup to capture output
        ByteArrayServletOutputStream basos = new ByteArrayServletOutputStream();
        ResponseIncludeWrapper responseIncludeWrapper =
            new ResponseIncludeWrapper(config.getServletContext(),req, res, basos);

        // process remainder of filter chain
        chain.doFilter(req, responseIncludeWrapper);

        // we can't assume the chain flushed its output
        responseIncludeWrapper.flushOutputStreamOrWriter();
        byte[] bytes = basos.toByteArray();

        // get content type
        String contentType = responseIncludeWrapper.getContentType();

        // is this an allowed type for SSI processing?
        if (contentTypeRegEx.matcher(contentType).matches()) {
            String encoding = res.getCharacterEncoding();

            // set up SSI processing 
            SSIExternalResolver ssiExternalResolver =
                new SSIServletExternalResolver(config.getServletContext(), req,
                        res, isVirtualWebappRelative, debug, encoding);
            SSIProcessor ssiProcessor = new SSIProcessor(ssiExternalResolver,
                    debug);
            
            // prepare readers/writers
            Reader reader =
                new InputStreamReader(new ByteArrayInputStream(bytes), encoding);
            ByteArrayOutputStream ssiout = new ByteArrayOutputStream();
            PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(ssiout, encoding));
            
            // do SSI processing  
            long lastModified = ssiProcessor.process(reader,
                    responseIncludeWrapper.getLastModified(), writer);
            
            // set output bytes
            writer.flush();
            bytes = ssiout.toByteArray();
            
            // override headers
            if (expires != null) {
                res.setDateHeader("expires", (new java.util.Date()).getTime()
                        + expires.longValue() * 1000);
            }
            if (lastModified > 0) {
                res.setDateHeader("last-modified", lastModified);
            }
            res.setContentLength(bytes.length);
            
            Matcher shtmlMatcher =
                shtmlRegEx.matcher(responseIncludeWrapper.getContentType());
            if (shtmlMatcher.matches()) {
            	// Convert shtml mime type to ordinary html mime type but preserve
                // encoding, if any.
            	String enc = shtmlMatcher.group(1);
            	res.setContentType("text/html" + ((enc != null) ? enc : ""));
            }
        }

        // write output
        OutputStream out = null;
        try {
            out = res.getOutputStream();
        } catch (IllegalStateException e) {
            // Ignore, will try to use a writer
        }
        if (out == null) {
            res.getWriter().write(new String(bytes, Charset.defaultCharset()));
        } else {
            out.write(bytes);
        }
    }

    public void destroy() {
    }
}
