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
package com.sun.enterprise.web.stats;


import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;


/**
 * <code>FileCache</code> monitoring support.
 *
 * @author Jeanfrancois Arcand
 */
public class PWCFileCacheStatsImpl implements PWCFileCacheStats {
    private final static Logger logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    private GenericStatsImpl baseStatsImpl;
    private ObjectName fileCacheName;
    private MBeanServer server;
    
    private MutableCountStatistic flagEnabled;    
    private MutableCountStatistic secondsMaxAge;
    private MutableCountStatistic countEntries;
    private MutableCountStatistic maxEntries;
    private MutableCountStatistic countOpenEntries;
    private MutableCountStatistic maxOpenEntries;
    private MutableCountStatistic sizeHeapCache;
    private MutableCountStatistic maxHeapCacheSize;
    private MutableCountStatistic sizeMmapCache;
    private MutableCountStatistic maxMmapCacheSize;
    private MutableCountStatistic countHits;
    private MutableCountStatistic countMisses;
    private MutableCountStatistic countInfoHits;
    private MutableCountStatistic countInfoMisses;
    private MutableCountStatistic countContentHits;
    private MutableCountStatistic countContentMisses;
    
    
    public PWCFileCacheStatsImpl(String domain) {
        
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=PWCFileCache,*";
        try {
            fileCacheName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            logger.log(Level.SEVERE, msg, t);
        }

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
     
    /** 
     * Returns flag indicating whether file cache has been enabled
     * @return 1 if file cache has been enabled, 0 otherwise
     */
    public CountStatistic getFlagEnabled() {
        flagEnabled.setCount(
            StatsUtil.getMaxStatistic(server, fileCacheName,"flagEnabled"));
        return (CountStatistic)flagEnabled.unmodifiableView();        
    }
    
    
    /** 
     * Return the maximum age of a valid cache entry
     * @return cache entry maximum age
     */
    public CountStatistic getSecondsMaxAge() {
        secondsMaxAge.setCount(
          StatsUtil.getMaxStatistic(server, fileCacheName,"secondsMaxAge"));
        return (CountStatistic)secondsMaxAge.unmodifiableView();        
    }
    
    
    /** 
     * Return the number of current cache entries.  
     */
    public CountStatistic getCountEntries() {
        countEntries.setCount(getAggregateLong("countEntries"));
        return (CountStatistic)countEntries.unmodifiableView();        
    }
    
    
    /** 
     * Return the maximum number of cache entries
     */
    public CountStatistic getMaxEntries() {
        maxEntries.setCount(
                StatsUtil.getMaxStatistic(server, fileCacheName,"maxEntries"));
        return (CountStatistic)maxEntries.unmodifiableView();        
    }
    
    
    /** 
     * Return the number of current open cache entries
     * @return open cache entries
     */
    public CountStatistic getCountOpenEntries() {
        countOpenEntries.setCount(getAggregateLong("countOpenEntries"));
        return (CountStatistic)countOpenEntries.unmodifiableView();        
    }
    
    
    /** 
     * The Maximum number of open cache entries
     */
    public CountStatistic getMaxOpenEntries() {
        maxOpenEntries.setCount(
            StatsUtil.getMaxStatistic(server, fileCacheName,"maxOpenEntries"));
        return (CountStatistic)maxOpenEntries.unmodifiableView();        
    }
    
    
    /** 
     * The  Heap space used for cache
     * @return heap size
     */
    public CountStatistic getSizeHeapCache() {
        sizeHeapCache.setCount(
            StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"sizeHeapCache"));
        return (CountStatistic)sizeHeapCache.unmodifiableView();        
    }
    
    
    /** 
     * Return he Maximum heap space used for cache
     */
    public CountStatistic getMaxHeapCacheSize() {
        maxHeapCacheSize.setCount(
            StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"maxHeapCacheSize"));
        return (CountStatistic)maxHeapCacheSize.unmodifiableView();        
    }
    
    
    /** 
     * Return he size of Mapped memory used for caching
     */
    public CountStatistic getSizeMmapCache() {
        sizeMmapCache.setCount(
          StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"sizeMmapCache"));
        return (CountStatistic)sizeMmapCache.unmodifiableView();        
    }
    
    
    /** 
     * Return the Maximum Memory Map size to be used for caching
     */
    public CountStatistic getMaxMmapCacheSize() {
        maxMmapCacheSize.setCount(
           StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"maxMmapCacheSize"));
        return (CountStatistic)maxMmapCacheSize.unmodifiableView();        
    }
    
    
    /** 
     * Return he Number of cache lookup hits
     */
    public CountStatistic getCountHits() {
        countHits.setCount(getAggregateLong("countHits"));
        return (CountStatistic)countHits.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of cache lookup misses
     */
    public CountStatistic getCountMisses() {
        countMisses.setCount(getAggregateLong("countMisses"));
        return (CountStatistic)countMisses.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of hits on cached file info
     */
    public CountStatistic getCountInfoHits() {
        countInfoHits.setCount(getAggregateLong("countInfoHits"));
        return (CountStatistic)countInfoHits.unmodifiableView();        
    }
    
    
    /** 
     * The Number of misses on cached file info
     * @return misses on cache file info
     */
    public CountStatistic getCountInfoMisses() {
        countInfoMisses.setCount(getAggregateLong("countInfoMisses"));
        return (CountStatistic)countInfoMisses.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of hits on cached file content
     */
    public CountStatistic getCountContentHits() {
        countContentHits.setCount(getAggregateLong("countContentHits"));
        return (CountStatistic)countContentHits.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of misses on cached file content
     */
    public CountStatistic getCountContentMisses() {
        countContentMisses.setCount(getAggregateLong("countContentMisses"));
        return (CountStatistic)countContentMisses.unmodifiableView();        
    }

    
    /** 
     * This is an implementation of the mandatory JSR77 Stats
     * interface method.
     * Here we simply delegate it to the GenericStatsImpl object
     * that we have
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }

    
    public Statistic getStatistic( String str ) {
        return baseStatsImpl.getStatistic( str );
    }

    
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

   
    /**
     * This method initialize statistics.
     */
    private void initializeStatistics() {
        CountStatistic cs = null;
        
        //enabled?
        cs = new CountStatisticImpl("FlagEnabled");
        flagEnabled = new MutableCountStatisticImpl( cs );

        //seconds Max Age
        cs = new CountStatisticImpl("SecondsMaxAge");
        secondsMaxAge = new MutableCountStatisticImpl( cs );

        //count entries
        cs = new CountStatisticImpl("CountEntries");
        countEntries = new MutableCountStatisticImpl( cs );

        //maxEntries
        cs = new CountStatisticImpl("MaxEntries");
        maxEntries = new MutableCountStatisticImpl( cs );

        //Open Entries
        cs = new CountStatisticImpl("CountOpenEntries");
        countOpenEntries = new MutableCountStatisticImpl( cs );

        //Max Open Entries
        cs = new CountStatisticImpl("MaxOpenEntries");
        maxOpenEntries = new MutableCountStatisticImpl( cs );

        // heap cache size
        cs = new CountStatisticImpl("SizeHeapCache");
        sizeHeapCache = new MutableCountStatisticImpl( cs );

        //max heap cache size
        cs = new CountStatisticImpl("MaxHeapCacheSize");
        maxHeapCacheSize = new MutableCountStatisticImpl( cs );

        //Mmap cache size
        cs = new CountStatisticImpl("SizeMmapCache");
        sizeMmapCache = new MutableCountStatisticImpl( cs );

        //Max Mmap cache size
        cs = new CountStatisticImpl("MaxMmapCacheSize");
        maxMmapCacheSize = new MutableCountStatisticImpl( cs );

        //count hits
        cs = new CountStatisticImpl("CountHits");
        countHits = new MutableCountStatisticImpl( cs );

        //count Misses
        cs = new CountStatisticImpl("CountMisses");
        countMisses = new MutableCountStatisticImpl( cs );

        //count Info Hits
        cs = new CountStatisticImpl("CountInfoHits");
        countInfoHits = new MutableCountStatisticImpl( cs );

        //count Info Misses
        cs = new CountStatisticImpl("CountInfoMisses");
        countInfoMisses = new MutableCountStatisticImpl( cs );

        //content hits
        cs = new CountStatisticImpl("CountContentHits");
        countContentHits = new MutableCountStatisticImpl( cs );

        //content misses
        cs = new CountStatisticImpl("CountContentMisses");
        countContentMisses = new MutableCountStatisticImpl( cs );

    }
    
    
    /**
     * Get Aggregated int statistics.
     */
    private final int getAggregateInt(String attribute){
        return StatsUtil.getAggregateStatistic(server,fileCacheName,attribute);
    }
    
    
    /**
     * Get Aggregated long statistics.
     */
    private final long getAggregateLong(String attribute){
        return StatsUtil.
                getAggregateLongStatistic(server,fileCacheName,attribute);
    }
}
