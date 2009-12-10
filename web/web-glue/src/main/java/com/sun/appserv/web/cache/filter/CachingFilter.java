/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */

package com.sun.appserv.web.cache.filter;

import java.io.IOException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Cookie;

import com.sun.logging.LogDomains;

import com.sun.appserv.web.cache.CacheHelper;
import com.sun.appserv.web.cache.DefaultCacheHelper;
import com.sun.appserv.web.cache.CacheManager;
import com.sun.appserv.web.cache.CacheManagerListener;

import com.sun.appserv.util.cache.Cache;

public class CachingFilter implements Filter, CacheManagerListener {

    // this servlet filter name
    String filterName;
    String servletName;
    String urlPattern;
    
    // associated cache and the helper
    CacheManager manager;
    CacheHelper helper;
    Cache cache;

    boolean isEnabled = false;

    private static final Logger _logger = LogDomains.getLogger(
        CachingFilter.class, LogDomains.WEB_LOGGER);

    private static final boolean _isTraceEnabled = _logger.isLoggable(
        Level.FINE);

    /** 
     * Called by the web container to indicate to a filter that it is being 
     * placed into service. The servlet container calls the init method exactly
     * once after instantiating the filter. The init method must complete 
     * successfully before the filter is asked to do any filtering work.
     * @param filterConfig filter config
     * @throws ServletException
     */
    public void init(FilterConfig filterConfig) throws ServletException {

        filterName = filterConfig.getFilterName();
        servletName = filterConfig.getInitParameter("servletName");
        urlPattern = filterConfig.getInitParameter("URLPattern");

        ServletContext context = filterConfig.getServletContext();
        manager = (CacheManager) context.getAttribute(
            CacheManager.CACHE_MANAGER_ATTR_NAME);

        if (manager != null && manager.isEnabled()) {
            this.cache = manager.getDefaultCache();
            this.helper = manager.getCacheHelperByFilterName(filterName);

            // add filter as a listener so caching can be disabled at runtime.
            manager.addCacheManagerListener(this);
            isEnabled = true;
        }

        if (_isTraceEnabled) {
            _logger.fine("CachingFilter " + filterName + " ready; isEnabled = " + isEnabled + " manager = " + manager);
        }
    }
    
    /**
     * The <code>doFilter</code> method of the Filter is called by the
     * container each time a request/response pair is passed through the
     * chain due to a client request for a resource at the end of the chain.
     * The FilterChain passed in to this method allows the Filter to pass on
     * the request and response to the next entity in the chain.
     *
     * @param srequest the runtime request
     * @param sresponse the runtime response
     * @param chain the filter chain to in the request processing
     * @throws IOException, ServletException
     * 
     * - First check if this HTTP method permits caching (using helper) 
     *   if not, call the downstream filter and return. 
     * - Otherwise, get the key based on the request (using helper). 
     * - Check if we have a response entry in the cache already. 
     * - If there is entry and is valid, write out the response from that
     *   entry. 
     * - create a CachingResponse and CachingOutputStream wrappers and call 
     *   the downstream filter
     */
    public void doFilter (ServletRequest srequest, ServletResponse sresponse,
                          FilterChain chain ) 
            throws IOException, ServletException {
        String key;
        HttpServletRequest request = (HttpServletRequest)srequest;
        HttpServletResponse response = (HttpServletResponse)sresponse;

        request.setAttribute(DefaultCacheHelper.ATTR_CACHING_FILTER_NAME, 
                                                            filterName);
        request.setAttribute(CacheHelper.ATTR_CACHE_MAPPED_SERVLET_NAME, 
                                        servletName);
        request.setAttribute(CacheHelper.ATTR_CACHE_MAPPED_URL_PATTERN, 
                                        urlPattern);

        if (isEnabled && helper.isCacheable((HttpServletRequest)request) &&
                (key = helper.getCacheKey(request)) != null) {

            // have the key index for reuse
            int index = cache.getIndex(key);

            if (_isTraceEnabled) {
                _logger.fine("CachingFilter " + request.getServletPath() + 
                                " request is cacheable; key " + key + " index = " + index);
            }

            HttpCacheEntry entry = null;
            boolean entryReady = false, waitForRefresh = true;
            
            // if refresh is not needed then check the cache first
            if (!helper.isRefreshNeeded(request)) {
                do {
                    // lookup cache
                    entry = (HttpCacheEntry) cache.get(key);

                    if (entry != null && entry.isValid()) {
                        // see if there is cached entry and is valid
                        entryReady = true;
                        break;
                    }
                    else {
                        /** 
                         *  a cache entry needs to be generated or refreshed.
                         *  if there are more than one thread tries to fill/refresh
                         *  same cache entry, then all but the first thread will block.
                         */
                        waitForRefresh = cache.waitRefresh(index);
                    }
                } while (waitForRefresh);
            } else {
                if (_isTraceEnabled) {
                    _logger.fine("CachingFilter " + request.getServletPath() + 
                                " request needs a refresh; key " + key);
                }
            }

            // do we have a valid response?
            if (entryReady) {
                if (_isTraceEnabled) {
                    _logger.fine("CachingFilter " + request.getServletPath() + 
                                " serving response from the cache " + key);
                }
                sendCachedResponse(entry, response);
            } else {
                // call the target servlet

                HttpCacheEntry oldEntry;

                // setup the response wrapper (and the output stream)
                CachingResponseWrapper wrapper =
                                new CachingResponseWrapper(response);

                // call the target resource
                try {
                    chain.doFilter(srequest, (ServletResponse)wrapper);
                } catch (ServletException se) {
                    wrapper.clear();
                    throw se;
                } catch (IOException ioe) {
                    wrapper.clear();
                    throw ioe;
                }

                // see if the there weren't any errors
                if (!wrapper.isError()) {
                    // create/refresh the cached response entry

                    // compute the timeout
                    int timeout = helper.getTimeout(request);

                    // previous entry gets replaced
                    entry = wrapper.cacheResponse();

                    if (timeout == CacheHelper.TIMEOUT_VALUE_NOT_SET) {
                        // extracts this from the Expires: date header
                        Long lval = wrapper.getExpiresDateHeader();

                        if (lval == null) {
                            timeout = manager.getDefaultTimeout();
                            entry.computeExpireTime(timeout);
                        } else {
                            long expireTime = lval.longValue();

                            // set the time this entry would expires
                            entry.setExpireTime(expireTime);
                        }
                    } else {
                        entry.computeExpireTime(timeout);
                    }

                    oldEntry = (HttpCacheEntry)cache.put(key, entry,
                                                         entry.getSize());
                    cache.notifyRefresh(index);

                    // transmit the response body content
                    writeBody(entry, response);
                } else {
                    /** either there was an error or response from this
                     *  resource is not cacheable anymore; so, remove the
                     *  old entry from the cache.
                     */
                    oldEntry = (HttpCacheEntry)cache.remove(key);
                }

                // clear the wrapper (XXX: cache these??)
                wrapper.clear();
            }

            /** clear the old entry?
             *  may lead to NPEs with one thread that just replaced an entry
             *  which might have just obtained before the new entry is put
             *  in cache. Must implement some sort of ref count or leave it
             *  to garbage collector
             *  if (oldEntry != null)
             *      oldEntry.clear();
             */
        } else {
            if (_isTraceEnabled) {
                _logger.fine("CachingFilter " + request.getServletPath() + 
                             " pass thru; isEnabled = " + isEnabled);
            }
            request.removeAttribute(DefaultCacheHelper.ATTR_CACHING_FILTER_NAME);
            request.removeAttribute(CacheHelper.ATTR_CACHE_MAPPED_SERVLET_NAME);
            request.removeAttribute(CacheHelper.ATTR_CACHE_MAPPED_URL_PATTERN);

            // pass thru
            chain.doFilter(srequest, sresponse);
        }
    }

    /**
     * called by doFilter to send out the cached response
     * @param entry cached response entry
     * @param response response object to write out the response
     * @throws IOException and ServletException.
     */
    private void sendCachedResponse(HttpCacheEntry entry, 
                                    HttpServletResponse response)
            throws IOException {

        // status code/message
        if (entry.statusCode != HttpCacheEntry.VALUE_NOT_SET) {
            response.setStatus(entry.statusCode);
        }

        // set the outbound response headers
        for (Iterator iter = entry.responseHeaders.keySet().iterator(); 
                                                         iter.hasNext(); ) {
            String name = (String)iter.next();
            ArrayList values = (ArrayList)entry.responseHeaders.get(name);

            for (int i = 0; i < values.size(); i++) {
                response.addHeader(name, (String)values.get(i));
            }
        }

        // date headers
        for (Iterator iter = entry.dateHeaders.keySet().iterator(); 
                                                    iter.hasNext(); ) {
            String name = (String)iter.next();
            ArrayList values = (ArrayList)entry.dateHeaders.get(name);

            for (int i = 0; i < values.size(); i++) {
                response.addDateHeader(name, ((Long)values.get(i)).longValue());
            }
        }

        // cookies
        for (int i = 0; i < entry.cookies.size(); i++) {
            response.addCookie((Cookie)entry.cookies.get(i));
        }

        // content type, length and locale
        if (entry.contentLength != HttpCacheEntry.VALUE_NOT_SET) {
            response.setContentLength(entry.contentLength);
        }
        if (entry.contentType != null) {
            response.setContentType(entry.contentType);
        }
        if (entry.locale != null) {
            response.setLocale(entry.locale);
        }

        // the response body
        writeBody(entry, response);
    }

    /**
     * called by doFilter/sendCachedResponse to write the body content
     * @param entry cached response entry
     * @param response response object to write out the response
     * @throws IOException and ServletException.
     */
    private void writeBody(HttpCacheEntry entry, 
                           HttpServletResponse response)
                           throws IOException {
        ServletOutputStream out = response.getOutputStream();
        out.write(entry.bytes);
    }

    /**
     * cache manager listener method
     */
    public void cacheManagerEnabled() {
        if (_isTraceEnabled)
            _logger.fine("CachingFilter " + filterName +
                " received cacheManager enabled event.");

        this.isEnabled = true;
    }

    /**
     * cache manager listener method
     */
    public void cacheManagerDisabled() {
        if (_isTraceEnabled)
            _logger.fine("CachingFilter " + filterName +
                " received cacheManager disabled event.");

        this.isEnabled = false;
    }

    /**
     * Called by the web container to indicate to a filter that it is being 
     * taken out of service. This method is only called once all threads 
     * within the filter's doFilter method have exited or after a timeout 
     * period has passed. 
     * After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter.
     */
    public void destroy() {
    }
}
