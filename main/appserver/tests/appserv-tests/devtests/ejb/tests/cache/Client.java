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

import com.sun.appserv.util.cache.*;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class Client {

    String cacheName; 
    int    cacheSize, resizeQuantity;
    long   idleTimeout; // idleTimeout value in milliseconds
    float  loadFactor;
    Cache  cache;
    Timer  timer;
    Object[] dummys;

    private boolean debug = false;
    //private boolean debug = true;

    protected final float   DEFAULT_LOAD_FACTOR     = 0.75f;
    protected final int     DEFAULT_CACHE_SIZE      = 8192;
    protected final int     DEFAULT_RESIZE_QUANTITY = 32;
    protected final int     DEFAULT_IDLE_TIMEOUT    = 30 * 1000; // 30 seconds

    protected final int     MAX_TEST_ENTRIES  = 10;

    public Client()  {
        this.cacheName      = "Default Cache";
        this.cacheSize      = 512;
        this.resizeQuantity = DEFAULT_RESIZE_QUANTITY;
        this.idleTimeout    = DEFAULT_IDLE_TIMEOUT;
    }

    public Client( String name, Timer timer )  {
        this.cacheName      = name;
        this.cacheSize      = 512;
        this.resizeQuantity = DEFAULT_RESIZE_QUANTITY;
        this.idleTimeout    = DEFAULT_IDLE_TIMEOUT;
        this.timer          = timer;
    }

    public Client( String name, int cacheSize, int resizeQuantity, long idleTimeout, Timer timer )  {
        this.cacheName      = name;
        this.cacheSize      = cacheSize;
        this.resizeQuantity = resizeQuantity;
        this.idleTimeout    = idleTimeout;
        this.timer          = timer;
    }

    public void testCache() {
        try {
            createCache();
            createObjects();
            addCacheEntries();
            if ( debug )
                displayCacheEntries( "Display items in cache after adding" ); 

            removeCacheEntries();
            if ( debug )
                System.out.println( "Total items in cache (" + cacheName + 
                    ") after remove call = " + getEntryCount() );

            addCacheEntries();
            timer.schedule( new ExpiredItemTask(), 0, 5 * 1000 );
        } catch ( Exception ex ) {
            System.out.println( "testCache()::Exception caught :: " + ex );
            ex.printStackTrace();
        }
    } //testCache()


    private void calculateLoadFactor() {
        if ( cacheSize <= 0 ) {
            loadFactor =  DEFAULT_LOAD_FACTOR;
        } else {
            loadFactor = (float)( 1.0  - (1.0 * resizeQuantity / cacheSize ));
        }
    } //calculateLoadFactor()

    private void createCache() {
        try {
            if ( debug )
                System.out.println( "\n\nIn createCache of Cache(" + cacheName + ")" );
            if ( cacheSize <= 0 && idleTimeout <= 0 ) {
                if ( debug )
                    System.out.println( "\t\tCreating BaseCache for cacheSize = " + cacheSize + 
                        " :resizeQuantity = " + resizeQuantity + " ::loadFactor = " + loadFactor );
                cache = new BaseCache();
                cache.init( DEFAULT_CACHE_SIZE, DEFAULT_LOAD_FACTOR, null );
            } else {
                cacheSize = ( cacheSize <= 0 ) ? DEFAULT_CACHE_SIZE : cacheSize;
                LruCache lru = new LruCache( DEFAULT_CACHE_SIZE);
                calculateLoadFactor();
                if ( debug )
                    System.out.println( "\t\tCreating LruCache for cacheSize = " + cacheSize + 
                        " :resizeQuantity = " + resizeQuantity + " ::loadFactor = " + loadFactor );
                lru.init( cacheSize, idleTimeout, loadFactor, null );
                cache = lru;
            }
        } catch ( Exception ex ) {
            System.out.println( "createCache()::Exception caught :: " + ex );
            ex.printStackTrace();
        }

    } //createCache()

    private void createObjects() {
        dummys = new Object[ MAX_TEST_ENTRIES ];
        for( int i = 0; i < MAX_TEST_ENTRIES; i++ ) {
            dummys[i] = new DummyObject( i );
        }
    } //createObjects() 

    private void addCacheEntries() {
        Object obj;
        for( int i = 0; i < MAX_TEST_ENTRIES; i++ ) {
            obj = (DummyObject) dummys[ i ];
            if ( debug )
                System.out.println( "Creating DummyObject " + obj );
            cache.add( obj, obj );
        }
    } //addCacheEntries()

    public void displayCacheEntries( String displayText ) {
        Iterator cacheItems = cache.values();
        System.out.println( "\n\t\t\t " + displayText + " for Cache(" + cacheName + ")" );
        while ( cacheItems.hasNext() ) {
            DummyObject obj = (DummyObject)cacheItems.next();
            System.out.println( "\t\t\tCache item found - : " + obj );
        }
    } //displayCacheEntries()

    private void removeCacheEntries() {
        Object obj;
        for( int i = 0; i < MAX_TEST_ENTRIES; i++ ) {
            obj = (DummyObject) dummys[ i ];
            if ( debug )
                System.out.println( "Removing DummyObject " + obj );
            cache.remove( obj, obj );
        }
    } //removeCacheEntries()

    public int getEntryCount() {
        return cache.getEntryCount();
    } //getEntryCount()

    class DummyObject {
        int    id;
        String name;

        public DummyObject( int id ) {
            this.id   = id;
            this.name = "Test Object " + id ;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("id = ").append( id ).append( ":name = " ).append( name );
            return buf.toString();
        }
    } //DummyObject{}

    class ExpiredItemTask 
        extends java.util.TimerTask {
        
        public void run() {
            cache.trimExpiredEntries( resizeQuantity ); 
        }
    } //ExpiredItemTask 



    public static void main( String[] args) {
        boolean debug = false;
        System.out.println( "\n\n\nTesting out the cache implementation for different values\n\n");

        Client c0 = new Client();
        Client c1 = new Client();
        Client c2 = new Client();
        Client c3 = new Client();
        Client c4 = new Client();

        Timer timer = new java.util.Timer();
        int totalTests = 5;
        int success    = 0;
        int failure    = 0;

        try {
            c0 = new Client( "c0", timer );
            c0.testCache();
            success++;
        } catch (Exception ex ) {
            failure++;
            System.out.println ( "Error when creating cache " + ex );
            ex.printStackTrace();
        }

        try {
            c1 = new Client(  "c1", 0, 32, 10 * 1000, timer );
            c1.testCache();
            success++;
        } catch (Exception ex ) {
            failure++;
            System.out.println ( "Error when creating cache " + ex );
            ex.printStackTrace();
        }

        try {
            c2 = new Client( "c2", 10, 32, 10 * 1000, timer );
            c2.testCache();
            success++;
        } catch (Exception ex ) {
            failure++;
            System.out.println ( "Error when creating cache " + ex );
            ex.printStackTrace();
        }

        try {
            c3 = new Client( "c3", 32, 32, 10 * 1000, timer );
            c3.testCache();
            success++;
        } catch (Exception ex ) {
            failure++;
            System.out.println ( "Error when creating cache " + ex );
            ex.printStackTrace();
        }

        try {
            c4 = new Client( "c4", 64, 32, 90 * 1000, timer );
            c4.testCache();
            success++;
        } catch (Exception ex ) {
            failure++;
            System.out.println ( "Error when creating cache " + ex );
            ex.printStackTrace();
        }
        System.out.println ( "\n\n\t Results of adding and removing objects from the cache " );
        System.out.println ( "\n\t =======================================================" );
        System.out.println ( "Expected success count = "  + totalTests );
        System.out.println ( "Actual   success count = "  + success );
        System.out.println ( "Actual   failure count = "  + failure );

        System.out.println ( "\n\n\t Please wait as I test the idle timeout feature of the cache" );
        try {
            java.lang.Thread.sleep( 60 * 1000 );
        } catch (Exception ex ) {
            System.out.println ( "Exception caught : " + ex );
            ex.printStackTrace();
        }

        if ( debug ) {
            System.out.println ( 
                "\n\nList all the items in all the caches to ensure that the idle timeout works correctly" );
            
            System.out.println ( "\t\t Total items in cache (c0) = " + c0.getEntryCount() );
            System.out.println ( "\t\t Total items in cache (c1) = " + c1.getEntryCount() );
            System.out.println ( "\t\t Total items in cache (c2) = " + c2.getEntryCount() );
            System.out.println ( "\t\t Total items in cache (c3) = " + c3.getEntryCount() );
            System.out.println ( "\t\t Total items in cache (c4) = " + c4.getEntryCount() );

            c0.displayCacheEntries( "List items in cache after timeout " ); 
            c1.displayCacheEntries( "List items in cache after timeout " ); 
            c2.displayCacheEntries( "List items in cache after timeout " ); 
            c3.displayCacheEntries( "List items in cache after timeout " ); 
            c4.displayCacheEntries( "List items in cache after timeout " ); 
        }
        
        success = 0;
        failure = 0;

        if ( c0.getEntryCount() == 0 ) {
            success++;
        } else {
            failure++;
        }
        if ( c1.getEntryCount() == 0 ) {
            success++;
        } else {
            failure++;
        }
        if ( c2.getEntryCount() == 0 ) {
            success++;
        } else {
            failure++;
        }
        if ( c3.getEntryCount() == 0 ) {
            success++;
        } else {
            failure++;
        }
        if ( c4.getEntryCount() == 10 ) {
            success++;
        } else {
            failure++;
        }


        System.out.println ( "\n\n\t\t Results of idle timeout on the cache " );
        System.out.println ( "\n\t\t =======================================================" );
        System.out.println ( "\nExpected success count = "  + totalTests );
        System.out.println ( "\nActual   success count = "  + success );
        System.out.println ( "\nActual   failure count = "  + failure );

        timer.cancel();
        
    } //main()

} //Client {}
