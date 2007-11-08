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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.http.MimeHeaders;


/**
 * This class implements a file caching mechanism used to cache static resources. 
 *
 * @author Jeanfrancois Arcand
 * @author Scott Oaks
 */
public class FileCache{
    
    public final static String DEFAULT_SERVLET_NAME = "default";
   
    
    /**
     * A <code>ByteBuffer</code> cache of static pages.
     */   
    private final ConcurrentHashMap<String,FileCacheEntry> fileCache = 
            new ConcurrentHashMap<String,FileCacheEntry>();
    
    
    /**
     * A dummy instance of <code>ByteBuffer</code>
     */
    private final static ByteBuffer nullByteBuffer = 
                            (ByteBuffer) ByteBuffer.allocate(0);
  
    
    /**
     * A connection: close of <code>ByteBuffer</code>
     */
    protected final static ByteBuffer connectionCloseBB = 
            ByteBuffer.wrap("Connection: close\r\n\r\n".getBytes());

    /**
     * A connection: keep-alive of <code>ByteBuffer</code>
     */
    protected final static ByteBuffer connectionKaBB = 
            ByteBuffer.wrap("Connection: keep-alive\r\n\r\n".getBytes());
    
    
    /**
     * HTTP end line.
     */
    private final static String NEWLINE = "\r\n";


    /**
     * HTTP OK header
     */
    public final static String OK = "HTTP/1.1 200 OK" + NEWLINE;    

    /**
     * The port associated with this cache.
     */
    private int port = 8080;    
    
    
    /**
     * Scheduled Thread that clean the cache every XX seconds.
     */
    private ScheduledThreadPoolExecutor cacheResourcesThread
        = new ScheduledThreadPoolExecutor(1,
            new GrizzlyThreadFactory("FileCacheThread-" + port,
                1,Thread.NORM_PRIORITY)); 
    
    
    /**
     * FileCacheEntry cache
     */
    private ConcurrentLinkedQueue<FileCacheEntry> cacheManager;

    
    /**
     * Timeout before remove the static resource from the cache.
     */
    private int secondsMaxAge = -1;
    
    
    /**
     * The maximum entries in the <code>fileCache</code>
     */
    private int maxCacheEntries = 1024;
    
 
    /**
     * The maximum size of a cached resources.
     */
    private long minEntrySize = 2048;
            
               
    /**
     * The maximum size of a cached resources.
     */
    private long maxEntrySize = 537600;
    
    
    /**
     * The maximum memory mapped bytes
     */
    private long maxLargeFileCacheSize = 10485760;
            
    
    /**
     * The maximum cached bytes
     */
    private long maxSmallFileCacheSize = 1048576;
    
    
    /**
     * The current cache size in bytes
     */
    private static long mappedMemorySize = 0;
    
    
    /**
     * The current cache size in bytes
     */
    private static long heapSize = 0;  
    
            
    /**
     * Is the file cache enabled.
     */
    private boolean isEnabled = true;
        
    
    /**
     * Is the large FileCache enabled.
     */
    private boolean isLargeFileCacheEnabled = true;   
    
    
    /**
     * Is monitoring enabled.
     */
    private static boolean isMonitoringEnabled = false;
    
    
    /**
     * The number of current open cache entries
     */
    private int openCacheEntries = 0;
   
       
    /**
     * The number of max current open cache entries
     */
    private int maxOpenCacheEntries = 0;
    
    
    /**
     * Max heap space used for cache
     */
    private long maxHeapCacheSize = 0;   
    
    
    /**
     * Max mapped memory used for cache
     */
    private long maxMappedMemory = 0;   
    
    
    /**
     * Number of cache lookup hits
     */
    private int countHits = 0;
    
    
    /**
     * Number of cache lookup misses
     */
    private int countMisses = 0;
    
    
    /**
     * Number of hits on cached file info
     */
    private int countCacheHits;
    
    
    /**
     * Number of misses on cached file info
     */
    private int countCacheMisses;
        
    
    /**
     * Number of hits on cached file info
     */
    private int countMappedHits;
    
    
    /**
     * Number of misses on cached file info
     */
    private int countMappedMisses;

    
    /**
     * The Header ByteBuffer default size.
     */
    private int headerBBSize = 4096;

    // ---------------------------------------------------- Methods ----------//
             
            
    /**
     * Add a resource to the cache. Currently, only static resources served
     * by the DefaultServlet can be cached.
     */
    public synchronized void add(String mappedServlet, String baseDir, 
            String requestURI, MimeHeaders headers, boolean xPoweredBy){
        
        if ( fileCache.get(requestURI) != null) return;
        
        // cache is full.
        if ( fileCache.size() > maxCacheEntries) {
            return;
        }
        
        if ( mappedServlet.equals(DEFAULT_SERVLET_NAME) ){                                     
            File file = new File(baseDir + requestURI);
            ByteBuffer bb = mapFile(file);

            // Always put the answer into the map. If it's null, then
            // we know that it doesn't fit into the cache, so there's no
            // reason to go through this code again.
            if (bb == null)
                bb = nullByteBuffer;
            
            FileCacheEntry entry = cacheManager.poll();
            if ( entry == null){
                entry = new FileCacheEntry();
            }
            entry.bb = bb;
            entry.requestURI = requestURI;
            
            if ( bb != nullByteBuffer){
                entry.lastModified = headers.getHeader("Last-Modified");
                entry.contentType = headers.getHeader("content-type");
                entry.xPoweredBy = xPoweredBy;
                entry.isInHeap = (file.length() < minEntrySize);
                entry.date = headers.getHeader("Date");
                entry.Etag = headers.getHeader("Etag");

                configHeaders(entry);

                if ( isMonitoringEnabled ) {
                    openCacheEntries++;   

                    if ( openCacheEntries > maxOpenCacheEntries){
                        maxOpenCacheEntries = openCacheEntries;
                    }

                    if ( heapSize > maxHeapCacheSize){
                        maxHeapCacheSize = heapSize;
                    }

                    if ( mappedMemorySize > maxMappedMemory){
                        maxMappedMemory = mappedMemorySize;
                    }
                }

                if ( secondsMaxAge > 0 ) {
                    entry.future = cacheResourcesThread.schedule(entry, 
                                                secondsMaxAge, TimeUnit.SECONDS);
                }
            }
            fileCache.put(requestURI,entry);
        }            
    }
       
    
    /**
     * Map the file to a <code>ByteBuffer</code>
     * @return the <code>ByteBuffer</code>
     */
    private final ByteBuffer mapFile(File file){
        FileChannel fileChannel = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            fileChannel = stream.getChannel();
             
            long size = fileChannel.size();
            
            if ( !isLargeFileCacheEnabled ) {
                // Large file support are not enabled
                if ( size > minEntrySize ) {
                    return null;
                }
            } else if ( size > maxEntrySize){
                return null;
            }

            if ( size > minEntrySize )
                mappedMemorySize+= size;
            else
                heapSize+= size;
 
            // Cache full
            if ( mappedMemorySize > maxLargeFileCacheSize ) {
                mappedMemorySize-= size;
                return null;
            } else  if ( heapSize > maxSmallFileCacheSize ) {
                heapSize-= size;
                return null;
            }        
            
            ByteBuffer bb = 
                    fileChannel.map(FileChannel.MapMode.READ_ONLY,0,size);
                                 
            if ( size < minEntrySize) {
                ((MappedByteBuffer)bb).load();
            }
            return bb;
        } catch (IOException ioe) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
        
    
    /**
     * Return <code>true</code> if the file is cached.
     */
    private final FileCacheEntry map(byte[] requestBytes,int start, int length){
        String uri = "";
        FileCacheEntry entry = null;
        
        if ( !fileCache.isEmpty() ){
            uri = new String(requestBytes,start,length);
            entry = fileCache.get(uri);
            
            if ( isMonitoringEnabled) {
                if (entry != null && entry.bb != null 
                        && entry.bb != nullByteBuffer){
                    if ( entry.isInHeap ) 
                        countCacheHits++;
                    else
                        countMappedHits++;

                    countHits++;
                
                } else {
                  countMisses++;
                }
            }
        }
        return entry;
    }
      
    
    /**
     * Send the cache.
     */
    public boolean sendCache(byte[] req, int start, int length,
            SocketChannel socketChannel, boolean keepAlive){

        try{
            FileCacheEntry entry = map(req,start,length);
            if ( entry != null && entry.bb != nullByteBuffer){
                sendCache(socketChannel,entry,keepAlive); 
                return true;
            }
        } catch (IOException ex){
            SelectorThread.logger()
                .fine("File Cache: " + ex.getMessage());
            return true;
        } catch (Throwable t){
            // If an unexpected exception occurs, try to serve the page
            // as if it wasn't in a cache.
            SelectorThread.logger()
                .fine("File Cache thread race: " + t.getMessage());
        }
        return false;
    }    
     
    
    /**
     * Set the cache manager used by this instance.
     */
    public void setCacheManager(ConcurrentLinkedQueue cacheManager){
        this.cacheManager = cacheManager;
    }   
    
    
    // -------------------------------------------------- Static cache -------/
    
    
    /**
     * Send the cached resource.
     */
    protected void sendCache(SocketChannel socketChannel,  FileCacheEntry entry,
            boolean keepAlive) throws IOException{
  
        OutputWriter.flushChannel(socketChannel, entry.headerBuffer.slice());
        ByteBuffer keepAliveBuf = keepAlive ? connectionKaBB.slice():
               connectionCloseBB.slice();
        OutputWriter.flushChannel(socketChannel, keepAliveBuf);        
        OutputWriter.flushChannel(socketChannel, entry.bb.slice());
    }

    
    /**
     * Return a <code>ByteBuffer</code> contains the server header.
     */
    private void configHeaders(FileCacheEntry entry) {
        if ( entry.headerBuffer == null ) {
            entry.headerBuffer = 
                    ByteBuffer.allocate(getHeaderBBSize());
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append(OK);
        if ( entry.xPoweredBy){
            appendHeaderValue(sb,"X-Powered-By", "Servlet/2.5");
        }     
        appendHeaderValue(sb, "ETag", entry.Etag);   
        appendHeaderValue(sb,"Last-Modified", entry.lastModified);
        appendHeaderValue(sb,"Content-Type", entry.contentType);
        appendHeaderValue(sb,"Content-Length", entry.bb.capacity() + "");
        appendHeaderValue(sb,"Date", entry.date);
        appendHeaderValue(sb,"Server", SelectorThread.SERVER_NAME);
        entry.headerBuffer.put(sb.toString().getBytes());
        entry.headerBuffer.flip();
    }   
       
    
    /**
     * Utility to add headers to the HTTP response.
     */
    private void appendHeaderValue(StringBuffer sb,String name, String value) {
        sb.append(name);
        sb.append(": ");
        sb.append(value);
        sb.append(NEWLINE);
    }   

    
    public final class FileCacheEntry implements Runnable{       
        public String requestURI;
        public String lastModified;
        public String contentType;
        public ByteBuffer bb;
        public ByteBuffer headerBuffer;        
        public boolean xPoweredBy;
        public boolean isInHeap = false;
        public String date;
        public String Etag;
        public Future future;
             
        public void run(){                          
            fileCache.remove(requestURI);
            
            if (requestURI == null) return;
            
            if (headerBuffer != null) {

                /**
                 * If the position !=0, it means the ByteBuffer has a view
                 * that is still used. If that's the case, wait another 10 seconds
                 * before marking the ByteBuffer for garbage collection
                 */
                if ( headerBuffer.position() !=0 || bb.position() != 0 ){        
                    future = cacheResourcesThread
                                .schedule(this, 10, TimeUnit.SECONDS);
                    return;
                } 

                if ( !isInHeap )
                    mappedMemorySize -= bb.limit();
                else
                    heapSize -= bb.limit();

                bb = null;
                headerBuffer = null;
                openCacheEntries--;
            }
            
            if ( future != null ) {
                future.cancel(false);
                future = null;
            }
            requestURI = null;
            cacheManager.offer(this);
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
        return fileCache.size();
    }
    
    
    /** 
     * Return the maximum number of cache entries
     * @return maximum cache entries
     */
    public long getMaxEntries() {
        return maxCacheEntries;
    }
    
    
    /** 
     * The number of current open cache entries
     * @return open cache entries
     */
    public long getCountOpenEntries() {
        return openCacheEntries;
    }
    
    
    /** 
     * Return the maximum number of open cache entries
     * @return maximum open cache entries
     */
    public long getMaxOpenEntries() {
       return maxOpenCacheEntries;        
    }
    
    
    /** 
     * Return the heap space used for cache
     * @return heap size
     */
    public long getSizeHeapCache() {
        return heapSize;
    }
    
    
    /** 
     * Return the maximum heap space used for cache
     * @return maximum heap size
     */
    public long getMaxHeapCacheSize() {
        return maxHeapCacheSize;
    }
    
    
    /** 
     * Return the size of Mapped memory used for caching
     * @return Mapped memory size
     */
    public static long getSizeMmapCache() {
        return mappedMemorySize;
    }
    
    
    /** 
     * Return the Maximum Memory Map size to be used for caching
     * @return maximum Memory Map size
     */
    public long getMaxMmapCacheSize() {
        return maxMappedMemory;
    }
    
    
    /** 
     * Return the Number of cache lookup hits
     * @return cache hits
     */
    public long getCountHits() {
        return countHits;
    }
    
    
    /** 
     * Return the Number of cache lookup misses
     * @return cache misses
     */
    public long getCountMisses() {
        return countMisses;
    }
    
    
    /** 
     * The Number of hits on cached file info
     * @return hits on cached file info
     */
    public long getCountInfoHits() {
        return countCacheHits;
    }
    
    
    /** 
     * Return the number of misses on cached file info
     * @return misses on cache file info
     */
    public long getCountInfoMisses() {
        return countCacheMisses;
    }
    
    
    /** 
     * Return the Number of hits on cached file content
     * @return hits on cache file content
     */
    public long getCountContentHits() {
        return countMappedHits;
    }
    
    
    /** 
     * Return the Number of misses on cached file content
     * @return missed on cached file content
     */
    public int getCountContentMisses() {
        return countMappedMisses;
    }
    
    // ---------------------------------------------------- Properties ----- //
    
    
    /**
     * Turn monitoring on/off
     */
    public static void setIsMonitoringEnabled(boolean isMe){
        isMonitoringEnabled = isMe;
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
    public boolean isEnabled(){
        return isEnabled;
    }

    
    /**
     * Is the file caching mechanism enabled.
     */
    public void setIsEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
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
     * Return the FileCache
     */
    public ConcurrentHashMap getCache(){
        return fileCache;
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
