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
package com.sun.enterprise.web.connector.grizzly;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.enterprise.web.connector.grizzly.FileCache.FileCacheEntry;
/**
 * A factory for creating <code>FileCache</code> instance.
 *
 * @author Jeanfrancois Arcand
 */
public class FileCacheFactory{

    
     /**
     * Timeout before remove the static resource from the cache.
     */
    public int secondsMaxAge = -1;
    
    
    /**
     * The maximum entries in the <code>fileCache</code>
     */
    public int maxCacheEntries = 1024;
    
 
    /**
     * The maximum size of a cached resources.
     */
    public long minEntrySize = 2048;
            
               
    /**
     * The maximum size of a cached resources.
     */
    public long maxEntrySize = 537600;
    
    
    /**
     * The maximum cached bytes
     */
    public long maxLargeFileCacheSize = 10485760;
 
    
    /**
     * The maximum cached bytes
     */
    public long maxSmallFileCacheSize = 1048576;
    
    
    /**
     * Is the FileCache enabled.
     */
    public static boolean isEnabled = true;
    
    
    /**
     * Is the large FileCache enabled.
     */
    public boolean isLargeFileCacheEnabled = true;    
    
    
    /**
     * The port used
     */
    public int port = 8080;
    
    
    /**
     * Create a factory per port.
     */
    protected final static ConcurrentHashMap<Integer,FileCacheFactory> cache =
            new ConcurrentHashMap<Integer,FileCacheFactory>();
    
    
    /**
     * The cache manager used by instance of <code>FileCache</code>
     * created by this factory;
     */
    protected ConcurrentLinkedQueue cacheManager;
    
    
    /**
     * Is monitoring enabled
     */
    protected boolean isMonitoringEnabled = false;
    
    
    /**
     * A list of <code>FileCache</code> instance this Factory is owning.
     */
    protected FileCache fileCache;
    
    
    /**
     * The Header ByteBuffer default size.
     */
    private int headerBBSize = 4096;    
    // ---------------------------------------------------------------------//
    
    
    protected FileCacheFactory(){        
    }

    
    /**
     * Configure the factory.
     */
    public static FileCacheFactory newInstance(int currentPort){
        FileCacheFactory fileCacheFactory= new FileCacheFactory();

        fileCacheFactory.port = currentPort;
        cache.put(currentPort, fileCacheFactory);

        ConcurrentLinkedQueue<FileCacheEntry> cacheManager =
            new  ConcurrentLinkedQueue<FileCacheEntry>();
        fileCacheFactory.setCacheManager(cacheManager);  

        return fileCacheFactory;
    }
    
    
    /**
     * Return an instance of this Factory.
     */
    public static FileCacheFactory getFactory(int currentPort){
                
        FileCacheFactory fileCacheFactory = cache.get(currentPort);
        if ( fileCacheFactory == null ){
            fileCacheFactory = newInstance(currentPort); 
        }

        return fileCacheFactory;
    }
    
    
    /**
     * Return an instance of a <code>FileCache</code>
     */
    public FileCache getFileCache(){
        if ( fileCache == null){
            fileCache = new FileCache();
            fileCache.setIsEnabled(isEnabled);
            fileCache.setLargeFileCacheEnabled(isLargeFileCacheEnabled);
            fileCache.setSecondsMaxAge(secondsMaxAge);
            fileCache.setMaxCacheEntries(maxCacheEntries);
            fileCache.setMinEntrySize(minEntrySize);
            fileCache.setMaxEntrySize(maxEntrySize);
            fileCache.setMaxLargeCacheSize(maxLargeFileCacheSize);
            fileCache.setMaxSmallCacheSize(maxSmallFileCacheSize);         
            fileCache.setCacheManager(cacheManager);
            fileCache.setIsMonitoringEnabled(isMonitoringEnabled);
            fileCache.setHeaderBBSize(headerBBSize);
        }
        
        return fileCache;
    } 
    
    
    public void setCacheManager(ConcurrentLinkedQueue cacheManager){
        this.cacheManager = cacheManager;
    }
    
    
    /**
     * Return the FileCache
     */
    public ConcurrentHashMap getCache(){
        if ( fileCache != null ){
            return fileCache.getCache();
        } else {
            return null;
        }
    }
    // ---------------------------------------------------- Monitoring --------//
    
    
    /** 
     * Returns flag indicating whether file cache has been enabled
     * @return 1 if file cache has been enabled, 0 otherwise
     */
    public int getFlagEnabled() {
        return (isEnabled == true?1:0);
    }
    
    
    /** 
     * Return the maximum age of a valid cache entry
     * @return cache entry maximum age
     */
    public int getSecondsMaxAge() {
        return secondsMaxAge;
    }
    
    
    /** 
     * Return the number of current cache entries.  
     * @return current cache entries
     */
    public long getCountEntries() {      
        if (fileCache == null) return 0L;
        return fileCache.getCountEntries();          
    }
    
    
    /** 
     * Return the maximum number of cache entries
     * @return maximum cache entries
     */
    public long getMaxEntries() {
        if (fileCache == null) return 0L;
        return maxCacheEntries;
    }
    
    
    /** 
     * The number of current open cache entries
     * @return open cache entries
     */
    public long getCountOpenEntries() {     
        if (fileCache == null) return 0L;
        return fileCache.getCountOpenEntries();      
    }
    
    
    /** 
     * Return the maximum number of open cache entries
     * @return maximum open cache entries
     */
    public long getMaxOpenEntries() {
        if (fileCache == null) return 0L;
        return fileCache.getMaxOpenEntries();
    }
    
    
    /** 
     * Return the heap space used for cache
     * @return heap size
     */
    public long getSizeHeapCache() {
        if (fileCache == null) return 0L;
        return fileCache.getSizeHeapCache();
    }
    
    
    /** 
     * Return the maximum heap space used for cache
     * @return maximum heap size
     */
    public long getMaxHeapCacheSize() {
        if (fileCache == null) return 0L;
        return fileCache.getMaxHeapCacheSize();
    }
    
    
    /** 
     * Return the size of Mapped memory used for caching
     * @return Mapped memory size
     */
    public long getSizeMmapCache() {
        if (fileCache == null) return 0L;
        return fileCache.getSizeMmapCache();  
    }
    
    
    /** 
     * Return the Maximum Memory Map size to be used for caching
     * @return maximum Memory Map size
     */
    public long getMaxMmapCacheSize() {
        if (fileCache == null) return 0L;
        return fileCache.getMaxMmapCacheSize();   
    }
    
    
    /** 
     * Return the Number of cache lookup hits
     * @return cache hits
     */
    public long getCountHits() {
        if (fileCache == null) return 0L;
        return fileCache.getCountHits(); 
    }
    
    
    /** 
     * Return the Number of cache lookup misses
     * @return cache misses
     */
    public long getCountMisses() {
        if (fileCache == null) return 0L;
        return fileCache.getCountMisses();  
    }
    
    
    /** 
     * The Number of hits on cached file info
     * @return hits on cached file info
     */
    public long getCountInfoHits() {
        if (fileCache == null) return 0L;
        return fileCache.getCountInfoHits();
    }
    
    
    /** 
     * Return the number of misses on cached file info
     * @return misses on cache file info
     */
    public long getCountInfoMisses() {
        if (fileCache == null) return 0L;
        return fileCache.getCountInfoMisses(); 
    }
    
    
    /** 
     * Return the Number of hits on cached file content
     * @return hits on cache file content
     */
    public long getCountContentHits() {
        if (fileCache == null) return 0L;
        return fileCache.getCountContentHits();  
    }
    
    
    /** 
     * Return the Number of misses on cached file content
     * @return missed on cached file content
     */
    public long getCountContentMisses() {
        if (fileCache == null) return 0L;
        return fileCache.getCountContentMisses(); 
    }
    
    // ---------------------------------------------------- Properties ----- //
    
    
    /**
     * Turn monitoring on/off
     */
    public void setIsMonitoringEnabled(boolean isMonitoringEnabled){
        this.isMonitoringEnabled = isMonitoringEnabled;
        FileCache.setIsMonitoringEnabled(isMonitoringEnabled);
    }
    
    
    /**
     * The timeout in seconds before remove a <code>FileCacheEntry</code>
     * from the <code>fileCache</code>
     */
    public void setSecondsMaxAge(int sMaxAges){
        secondsMaxAge = sMaxAges;
    }
    
    
    /**
     * Set the maximum entries this cache can contains.
     */
    public void setMaxCacheEntries(int mEntries){
        maxCacheEntries = mEntries;
    }

    
    /**
     * Return the maximum entries this cache can contains.
     */    
    public int getMaxCacheEntries(){
        return maxCacheEntries;
    }
    
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMinEntrySize(long mSize){
        minEntrySize = mSize;
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMinEntrySize(){
        return minEntrySize;
    }
     
    
    /**
     * Set the maximum size a <code>FileCacheEntry</code> can have.
     */
    public void setMaxEntrySize(long mEntrySize){
        maxEntrySize = mEntrySize;
    }
    
    
    /**
     * Get the maximum size a <code>FileCacheEntry</code> can have.
     */
    public long getMaxEntrySize(){
        return maxEntrySize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxLargeCacheSize(long mCacheSize){
        maxLargeFileCacheSize = mCacheSize;
    }

    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxLargeCacheSize(){
        return maxLargeFileCacheSize;
    }
    
    
    /**
     * Set the maximum cache size
     */ 
    public void setMaxSmallCacheSize(long mCacheSize){
        maxSmallFileCacheSize = mCacheSize;
    }
    
    
    /**
     * Get the maximum cache size
     */ 
    public long getMaxSmallCacheSize(){
        return maxSmallFileCacheSize;
    }    

    
    /**
     * Is the fileCache enabled.
     */
    public static boolean isEnabled(){
        return isEnabled;
    }

    
    /**
     * Is the file caching mechanism enabled.
     */
    public static void setIsEnabled(boolean isE){
        isEnabled = isE;
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public void setLargeFileCacheEnabled(boolean isLargeEnabled){
        this.isLargeFileCacheEnabled = isLargeEnabled;
    }
   
    
    /**
     * Is the large file cache support enabled.
     */
    public boolean getLargeFileCacheEnabled(){
        return isLargeFileCacheEnabled;
    } 

    
    /**
     * Retunr the header size buffer.
     */ 
    public int getHeaderBBSize() {
        return headerBBSize;
    }

    
    /**
     * Set the size of the header ByteBuffer.
     */
    public void setHeaderBBSize(int headerBBSize) {
        this.headerBBSize = headerBBSize;
    }
}
