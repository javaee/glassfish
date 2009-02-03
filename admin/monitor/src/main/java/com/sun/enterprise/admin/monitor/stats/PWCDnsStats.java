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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * PWCDnsStats.java
 *
 * Created on April 2, 2004, 10:37 AM
 */

package com.sun.enterprise.admin.monitor.stats;

/**
 *
 * @author  nsegura
 */
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.CountStatistic;

/** 
 * The DNS Cache caches IP addresses and DNS names. The server’s DNS cache is
 * disabled by default. A single cache entry represents a single IP address or DNS
 * name lookup
 */
public interface PWCDnsStats extends Stats {
    
    /**
     * Indicates whether the DNS cache is enabled or disable. Default is disabled.
     * @return DNS cache enabled?
     */
    public CountStatistic getFlagCacheEnabled();
    
    /** 
     * The number of current cache entries
     * @return current cache entries
     */
    public CountStatistic getCountCacheEntries();
    
    /** 
     * The maximum number of cache entries
     * @return max cache entries
     */
    public CountStatistic getMaxCacheEntries();
    
    /** 
     * The number of cache hits
     * @return cache hits
     */
    public CountStatistic getCountCacheHits();
    
    /** 
     * The number of cache misses
     * @return cache misses
     */
    public CountStatistic getCountCacheMisses();
    
    /** 
     * Returns whether asynchronic lookup is enabled. 1 if true, 0 otherwise
     * @return enabled
     */
    public CountStatistic getFlagAsyncEnabled();
    
    /** 
     * The total number of asynchronic name lookups
     * @return asyn name lookups
     */
    public CountStatistic getCountAsyncNameLookups();
    
    /** 
     * The total number of asynchronic address lookups
     * @return asyn address lookups
     */
    public CountStatistic getCountAsyncAddrLookups();
    
    /** 
     * The number of asynchronic lookups in progress
     * @return async lookups in progress
     */
    public CountStatistic getCountAsyncLookupsInProgress();
    
}
