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

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.coyote.Adapter;
import org.apache.coyote.RequestGroupInfo;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;

import com.sun.enterprise.web.connector.grizzly.algorithms.NoParsingAlgorithm;
import com.sun.enterprise.web.connector.grizzly.async.DefaultAsyncHandler;
import com.sun.enterprise.web.connector.grizzly.comet.CometAsyncFilter;
import com.sun.enterprise.web.connector.grizzly.FileCache.FileCacheEntry;
import java.util.Enumeration;

/**
 * This class implement an NIO socket HTTP Listener. This class 
 * supports three stagegy:
 *
 * Mode Blocking: This mode uses NIO blocking mode, and doesn't uses any of the 
 *                java.nio.* classes.
 *
 *
 * Mode Non-Blocking: This mode uses NIO non blocking mode and read the entire 
 *         request stream before processing the request. The stragegy used is 
 *         to find the content-lenght header and buffer bytes until the end of 
 *         the stream is read.
 *
 * @author Jean-Francois Arcand
 */
public class SelectorThread extends Thread implements MBeanRegistration{
            
    public final static String SERVER_NAME = 
            System.getProperty("product.name") != null 
                ? System.getProperty("product.name") : "grizzly";
    
        
    private Object[] lock = new Object[0];

    
    protected int serverTimeout = Constants.DEFAULT_SERVER_SOCKET_TIMEOUT;

    protected InetAddress inet;
    protected int port;

    protected ServerSocket serverSocket;
    
    /**
     * The <code>ServerSocketChannel</code> used in blocking mode.
     */
    protected ServerSocketChannel serverSocketChannel;
    
    protected boolean initialized = false;    
    protected volatile boolean running = false;    
    // ----------------------------------------------------- JMX Support ---/
    
    
    protected String domain;
    protected ObjectName oname;
    protected ObjectName globalRequestProcessorName;
    private ObjectName keepAliveMbeanName;
    private ObjectName pwcConnectionQueueMbeanName;
    private ObjectName pwcFileCacheMbeanName;
    protected MBeanServer mserver;
    protected ObjectName processorWorkerThreadName;


    // ------------------------------------------------------Socket setting --/

    protected boolean tcpNoDelay=false;
    
    
    protected int linger=100;
    
    
    protected int socketTimeout=-1;
    
    
    protected int maxKeepAliveRequests = Constants.DEFAULT_MAX_KEEP_ALIVE;
    
    
    protected boolean oOBInline = false;
    // ------------------------------------------------------ Compression ---/


    /**
     * Compression value.
     */
    protected String compression = "off";
    protected String noCompressionUserAgents = null;
    protected String restrictedUserAgents = null;
    protected String compressableMimeTypes = "text/html,text/xml,text/plain";
    protected int compressionMinSize    = 2048;
       
    // ------------------------------------------------------ Properties----/
    
    
    /**
     * Is the socket reuse socket enabled.
     */
    private boolean reuseAddress = true;


    /**
     * Buffer the response until the buffer is full.
     */
    protected boolean bufferResponse = false;
    
    
    /**
     * Default HTTP header buffer size.
     */
    protected int maxHttpHeaderSize = Constants.DEFAULT_HEADER_SIZE;


    /**
     * Number of polled <code>Read*Task</code> instance.
     */
    protected int minReadQueueLength = 10;


    /**
     * Number of polled <code>ProcessorTask</code> instance.
     */
    protected int minProcessorQueueLength = 10;
    
    
    protected int maxPostSize = 2 * 1024 * 1024;


    /**
     * The <code>Selector</code> used by the connector.
     */
    protected Selector selector;


    /**
     * Associated adapter.
     */
    protected Adapter adapter = null;

    
    /**
     * The queue shared by this thread and code>ReadTask</code>
     */ 
    protected Pipeline readPipeline;
    

    /**
     * The queue shared by this thread and the code>ProcessorTask</code>.
     */ 
    protected Pipeline processorPipeline;
    
  
    /**
     * Placeholder for <code>Pipeline</code> statistic.
     */
    protected PipelineStatistic pipelineStat;
    
    /**
     * The default <code>Pipeline</code> used.
     */
    protected String pipelineClassName = 
        com.sun.enterprise.web.connector.grizzly.
            LinkedListPipeline.class.getName();
    
    /**
     * Maximum number of <code>WorkerThread</code>
     */
    protected int maxProcessorWorkerThreads = 5; // By default
    
    
    /**
     * Maximum number of <code>ReadWorkerThread</code>
     */
    protected int maxReadWorkerThreads = -1; // By default

    
    /**
     * Minimum numbers of <code>WorkerThread</code> created
     */
    protected int minWorkerThreads = 5;
    

    /**
     * Minimum numbers of <code>WorkerThread</code> 
     * before creating new thread.
     * <implementation-note>
     * Not used in 9.x
     * </implementation-note>
     */
    protected int minSpareThreads = 2;

    
    /**
     * The number used when increamenting the <code>Pipeline</code> 
     * thread pool.
     */
    protected int threadsIncrement = 1;
    
    
    /**
     * The timeout used by the thread when processing a request.
     */
    protected int threadsTimeout = Constants.DEFAULT_TIMEOUT;

    
    /**
     * Is the <code>ByteBuffer</code> used by the <code>ReadTask</code> use
     * direct <code>ByteBuffer</code> or not.
     */
    protected boolean useDirectByteBuffer = false;
    
  
    /**
     * Monitoring object used to store information.
     */
    protected RequestGroupInfo globalRequestProcessor= new RequestGroupInfo();
    
    
    /**
     * Keep-alive stats
     */
    private KeepAliveStats keepAliveStats = new KeepAliveStats();


    /**
     * If <code>true</code>, display the NIO configuration information.
     */
    protected boolean displayConfiguration = false;
    
    
    /**
     * Is monitoring already started.
     */
    protected boolean isMonitoringEnabled = false;
    

    /**
     * The current number of simulatenous connection.
     */
    protected int currentConnectionNumber;


    /**
     * Is this Selector currently in Wating mode?
     */
    protected volatile boolean isWaiting = false;
    

    /**
     * The input request buffer size.
     */
    protected int requestBufferSize = Constants.DEFAULT_REQUEST_BUFFER_SIZE;
    
    
    /**
     * Create view <code>ByteBuffer</code> from another <code>ByteBuffer</code>
     */
    protected boolean useByteBufferView = false;
    

    /*
     * Number of seconds before idle keep-alive connections expire
     */
    protected int keepAliveTimeoutInSeconds = Constants.DEFAULT_TIMEOUT;

    
    /**
     * Number of seconds before idle keep-alive connections expire
     */
    private int kaTimeout = Constants.DEFAULT_TIMEOUT * 1000;
    
    
    /**
     * Recycle the <code>Task</code> after running them
     */
    protected boolean recycleTasks = Constants.DEFAULT_RECYCLE;
    
    
    /**
     * The <code>Selector</code> timeout value. By default, it is set to 60000
     * miliseconds (as in the j2se 1.5 ORB).
     */
    protected static int selectorTimeout = 1000;


    /**
     * Maximum pending connection before refusing requests.
     */
    protected int maxQueueSizeInBytes = Constants.DEFAULT_QUEUE_SIZE;


    /**
     * The <code>Algorithm</code> used to predict the end of the NIO stream
     */
    protected Class algorithmClass;
    
    
    /**
     * The <code>Algorithm</code> used to parse the NIO stream.
     */
    protected String algorithmClassName = DEFAULT_ALGORITHM;
    
    
    /**
     * The default NIO stream algorithm.
     */
    public final static String DEFAULT_ALGORITHM =
        com.sun.enterprise.web.connector.grizzly.algorithms.
            NoParsingAlgorithm.class.getName();

    
    /**
     * Server socket backlog.
     */
    protected int ssBackLog = 4096;
    
    
    /**
     * Next time the exprireKeys() will delete keys.
     */    
    private long nextKeysExpiration = 0;
    
    
    /**
     * The default response-type
     */
    protected String defaultResponseType = Constants.DEFAULT_RESPONSE_TYPE;


    /**
     * The forced request-type
     */
    protected String forcedRequestType = Constants.FORCED_REQUEST_TYPE;
    
    
    /**
     * The root folder where application are deployed
     */
    protected static String rootFolder = "";
    
    
    // ----------------------------------------------------- Collections --//
    
    
    /**
     * List of <code>SelectionKey</code> event to register next time the 
     * <code>Selector</code> wakeups. This is needed since there a bug
     * in j2se 1.4.x that prevent registering selector event if the call
     * is done on another thread.
     */
    private ConcurrentLinkedQueue<SelectionKey> keysToEnable =
        new ConcurrentLinkedQueue<SelectionKey>();
         
    
    // ---------------------------------------------------- Object pools --//


    /**
     * <code>ConcurrentLinkedQueue</code> used as an object pool.
     * If the list becomes empty, new <code>ProcessorTask</code> will be
     * automatically added to the list.
     */
    protected ConcurrentLinkedQueue<ProcessorTask> processorTasks =
        new ConcurrentLinkedQueue<ProcessorTask>();
              
    
    /**
     * <code>ConcurrentLinkedQueue</code> used as an object pool.
     * If the list becomes empty, new <code>ReadTask</code> will be
     * automatically added to the list.
     */
    protected ConcurrentLinkedQueue<ReadTask> readTasks =
        new ConcurrentLinkedQueue<ReadTask>();

    
    /**
     * List of active <code>ProcessorTask</code>.
     */
    protected ConcurrentLinkedQueue<ProcessorTask> activeProcessorTasks =
        new ConcurrentLinkedQueue<ProcessorTask>();
    
    // -----------------------------------------  Multi-Selector supports --//

    /**
     * The number of <code>SelectorReadThread</code>
     */
    protected int multiSelectorsCount = 0;

    
    /**
     * The <code>Selector</code> used to register OP_READ
     */    
    protected MultiSelectorThread[] readThreads;
    
    
    /**
     * The current <code>readThreads</code> used to process OP_READ.
     */
    int curReadThread;

    
    /**
     * The logger used by the grizzly classes.
     */
    protected static Logger logger = Logger.getLogger("GRIZZLY");
    
    
    /**
     * Flag to disable setting a different time-out on uploads.
     */
    protected boolean disableUploadTimeout = true;    
    
    
    /**
     * Maximum timeout on uploads. 5 minutes as in Apache HTTPD server.
     */
    protected int uploadTimeout = 30000;
            
            
    // -----------------------------------------  Keep-Alive subsystems --//
    
     
    /**
     * Keep-Alive subsystem. If a client opens a socket but never close it,
     * the <code>SelectionKey</code> will stay forever in the 
     * <code>Selector</code> keys, and this will eventualy produce a 
     * memory leak.
     */
    protected KeepAlivePipeline keepAlivePipeline;
    
    
    // ------------------------------------------------- FileCache support --//
   
    
    /**
     * The FileCacheFactory associated with this Selector
     */ 
    protected FileCacheFactory fileCacheFactory;
    
        /**
     * Timeout before remove the static resource from the cache.
     */
    protected int secondsMaxAge = -1;
    
    
    /**
     * The maximum entries in the <code>fileCache</code>
     */
    protected int maxCacheEntries = 1024;
    
 
    /**
     * The maximum size of a cached resources.
     */
    protected long minEntrySize = 2048;
            
               
    /**
     * The maximum size of a cached resources.
     */
    protected long maxEntrySize = 537600;
    
    
    /**
     * The maximum cached bytes
     */
    protected long maxLargeFileCacheSize = 10485760;
 
    
    /**
     * The maximum cached bytes
     */
    protected long maxSmallFileCacheSize = 1048576;
    
    
    /**
     * Is the FileCache enabled.
     */
    protected boolean isFileCacheEnabled = true;
    
    
    /**
     * Is the large FileCache enabled.
     */
    protected boolean isLargeFileCacheEnabled = true;    
    
    // --------------------------------------------- Asynch supports -----//
    
    /**
     * Is asynchronous mode enabled?
     */
    protected boolean asyncExecution = false;
    
    
    /**
     * When the asynchronous mode is enabled, the execution of this object
     * will be delegated to the <code>AsyncHandler</code>
     */
    protected AsyncHandler asyncHandler;
    
    
    /**
     * Is the DEFAULT_ALGORITHM used.
     */
    protected static boolean defaultAlgorithmInstalled = true;
    
    
    /**
     * The JMX Management class.
     */
    private Management jmxManagement = null;
    
    
    /**
     * The Classloader used to load instance of StreamAlgorithm.
     */
    private ClassLoader classLoader;
    
    
    /**
     * Grizzly own debug flag.
     */
    protected boolean enableNioLogging = false;
    
    
    /**
     * Static list of current instance of this class.
     */
    private final static ConcurrentHashMap<Integer,SelectorThread> 
            selectorThreads = new ConcurrentHashMap<Integer,SelectorThread>();
    
    
    /**
     * Banned SelectionKey registration.
     */
    protected ConcurrentLinkedQueue<SelectionKey> bannedKeys =
        new ConcurrentLinkedQueue<SelectionKey>();    
    
    
    // ---------------------------------------------------- Constructor --//
    
    
    /**
     * Create the <code>Selector</code> object. Each instance of this class
     * will listen to a specific port.
     */
    public SelectorThread(){
    }
    
    // ------------------------------------------------------ Selector hook --/
    
    
    /**
     * Return the <code>SelectorThread</code> which listen on port, or null
     * if there is no <code>SelectorThread</code>.
     */
    public final static SelectorThread getSelector(int port){
        return selectorThreads.get(port);
    }
    
    
    /**
     * Return an <code>Enumeration</code> of the active 
     * <code>SelectorThread</code>s
     */
    public final static Enumeration<SelectorThread> getSelectors(){
        return selectorThreads.elements();
    }
    
    
    // ----------------------------------------------------------------------/
    
   /**
     * Enable all registered interestOps. Due a a NIO bug, all interestOps
     * invokation needs to occurs on the same thread as the selector thread.
     */
    public void enableSelectionKeys(){
        SelectionKey selectionKey;
        int size = keysToEnable.size();
        long currentTime = 0L;
        if (size > 0){
            currentTime = (Long)System.currentTimeMillis();
        }

        for (int i=0; i < size; i++) {
            selectionKey = keysToEnable.poll();
            
            // If the SelectionKey is used for continuation, do not allow
            // the key to be registered.
            if (asyncExecution && !bannedKeys.isEmpty() 
                    && bannedKeys.remove(selectionKey)){
                continue;
            }
            
            if (!selectionKey.isValid() || !keepAlivePipeline.trap(selectionKey)){
                cancelKey(selectionKey);
                continue;
            }

            selectionKey.interestOps(
                    selectionKey.interestOps() | SelectionKey.OP_READ);

            if (selectionKey.attachment() == null)
                selectionKey.attach(currentTime);
        } 
    } 
    
    
    /**
     * Add a <code>SelectionKey</code> to the banned list of SelectionKeys. 
     * A SelectionKey is banned when new registration aren't allowed on the 
     * Selector.
     */
    public void addBannedSelectionKey(SelectionKey key){
        bannedKeys.offer(key);
    }
    
    
    /**
     * Register a <code>SelectionKey</code> to this <code>Selector</code>
     * running of this thread.
     */
    public void registerKey(SelectionKey key){
        if (key == null) return;
        
        if (keepAlivePipeline.dropConnection()) {
            cancelKey(key);
            return;
        }
        
        if (defaultAlgorithmInstalled){
            key.attach(null);
        }
        
        if (enableNioLogging){
            logger.log(Level.INFO,
                    "Registering SocketChannel for keep alive " +  
                    key.channel());
        }         
        // add SelectionKey & Op to list of Ops to enable
        keysToEnable.add(key);
        // tell the Selector Thread there's some ops to enable
        selector.wakeup();
        // wakeup() will force the SelectorThread to bail out
        // of select() to process your registered request
    } 

   // -------------------------------------------------------------- Init // 


    /**
     * initialized the endpoint by creating the <code>ServerScoketChannel</code>
     * and by initializing the server socket.
     */
    public void initEndpoint() throws IOException, InstantiationException {
        SelectorThreadConfig.configure(this);
        
        initFileCacheFactory();
        initAlgorithm();
        initPipeline();
        initMonitoringLevel();
        
        setName("SelectorThread-" + port);
        
        try{
            // Create the socket listener
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();

            serverSocket = serverSocketChannel.socket();
            serverSocket.setReuseAddress(reuseAddress);
            if ( inet == null)
                serverSocket.bind(new InetSocketAddress(port),ssBackLog);
            else
                serverSocket.bind(new InetSocketAddress(inet,port),ssBackLog);

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (SocketException ex){
            throw new BindException(ex.getMessage() + ": " + port);
        }
        
        serverSocket.setSoTimeout(serverTimeout);
        
        if ( multiSelectorsCount > 1 ){
            readThreads = new MultiSelectorThread[multiSelectorsCount];
            initMultiSelectors();
        } 
        initProcessorTask(maxProcessorWorkerThreads);
        initReadTask(minReadQueueLength);                
        SelectorFactory.maxSelectors = maxProcessorWorkerThreads;

        initialized = true;           
        logger.log(Level.FINE,"Initializing Grizzly Non-Blocking Mode");                     
    }
     
    
    /**
     * Create a new <code>Pipeline</code> instance using the 
     * <code>pipelineClassName</code> value.
     */
    protected Pipeline newPipeline(int maxThreads,
                                   int minThreads,
                                   String name, 
                                   int port,
                                   int priority){
        
        Class className = null;                               
        Pipeline pipeline = null;                               
        try{           
            if ( classLoader == null ){
                className = Class.forName(pipelineClassName);
            } else {
                className = classLoader.loadClass(pipelineClassName);
            }
            pipeline = (Pipeline)className.newInstance();
        } catch (ClassNotFoundException ex){
            logger.log(Level.WARNING,
                       "Unable to load Pipeline: " + pipelineClassName);
            pipeline = new LinkedListPipeline();
        } catch (InstantiationException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Pipeline: "
                       + pipelineClassName);
            pipeline = new LinkedListPipeline();
        } catch (IllegalAccessException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Pipeline: "
                       + pipelineClassName);
            pipeline = new LinkedListPipeline();
        }
        
        if (logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE,
                       "http-listener " + port + " uses pipeline: "
                       + pipeline.getClass().getName());
        }
        
        pipeline.setMaxThreads(maxThreads);
        pipeline.setMinThreads(minThreads);    
        pipeline.setName(name);
        pipeline.setPort(port);
        pipeline.setPriority(priority);
        pipeline.setQueueSizeInBytes(maxQueueSizeInBytes);
        pipeline.setThreadsIncrement(threadsIncrement);
        pipeline.setThreadsTimeout(threadsTimeout);
        
        return pipeline;
    }
    
    
    /**
     * Initialize the fileCacheFactory associated with this instance
     */
    protected void initFileCacheFactory(){        
        if (asyncExecution){
            isFileCacheEnabled = false;
            isLargeFileCacheEnabled = false;
        }
        
        fileCacheFactory = FileCacheFactory.getFactory(port);
        fileCacheFactory.setIsEnabled(isFileCacheEnabled);
        fileCacheFactory.setLargeFileCacheEnabled(isLargeFileCacheEnabled);
        fileCacheFactory.setSecondsMaxAge(secondsMaxAge);
        fileCacheFactory.setMaxCacheEntries(maxCacheEntries);
        fileCacheFactory.setMinEntrySize(minEntrySize);
        fileCacheFactory.setMaxEntrySize(maxEntrySize);
        fileCacheFactory.setMaxLargeCacheSize(maxLargeFileCacheSize);
        fileCacheFactory.setMaxSmallCacheSize(maxSmallFileCacheSize);         
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);
        fileCacheFactory.setHeaderBBSize(requestBufferSize);
    }
       
    
    /**
     * Injects <code>PipelineStatistic</code> into every
     * <code>Pipeline</code>, for monitoring purposes.
     */
    protected void enablePipelineStats(){
        pipelineStat.start();

        processorPipeline.setPipelineStatistic(pipelineStat);       
        pipelineStat.setProcessorPipeline(processorPipeline);

        if (keepAlivePipeline != null){
            keepAlivePipeline.setKeepAliveStats(keepAliveStats);
        }
    }
    

    /**
     * Removes <code>PipelineStatistic</code> from every
     * <code>Pipeline</code>, when monitoring has been turned off.
     */
    protected void disablePipelineStats(){
        pipelineStat.stop();
        
        processorPipeline.setPipelineStatistic(null);
        pipelineStat.setProcessorPipeline(null);

        if (keepAlivePipeline != null){
            keepAlivePipeline.setKeepAliveStats(null);
        }

    }

    
    /**
     * Load using reflection the <code>Algorithm</code> class.
     */
    protected void initAlgorithm(){
        try{    
            if (classLoader == null){
                algorithmClass = Class.forName(algorithmClassName);
            } else {
                algorithmClass = classLoader.loadClass(algorithmClassName);
            }
            logger.log(Level.FINE,
                       "Using Algorithm: " + algorithmClassName);   
        } catch (ClassNotFoundException ex){
            logger.log(Level.FINE,
                       "Unable to load Algorithm: " + algorithmClassName);        
        }  finally {
            if ( algorithmClass == null ){
                algorithmClass = NoParsingAlgorithm.class;
            }
        }

        defaultAlgorithmInstalled = 
                algorithmClassName.equals(DEFAULT_ALGORITHM) ? true:false;
    }
    
    
    /**
     * Initialize the keep-alive mechanism.
     */
    protected void initKeepAlivePipeline(){
        keepAlivePipeline = new KeepAlivePipeline();
        keepAlivePipeline.setMaxKeepAliveRequests(maxKeepAliveRequests);
        keepAlivePipeline
            .setKeepAliveTimeoutInSeconds(keepAliveTimeoutInSeconds);
        keepAlivePipeline.setPort(port);
        keepAlivePipeline.setThreadsTimeout(threadsTimeout);

        keepAliveStats.setMaxConnections(maxKeepAliveRequests);
        keepAliveStats.setSecondsTimeouts(keepAliveTimeoutInSeconds);        
    }
    
    
    /**
     * Init the <code>Pipeline</code>s used by the <code>WorkerThread</code>s.
     */
    protected void initPipeline(){     
                
        selectorThreads.put(port,this);
        
        initKeepAlivePipeline();
        
        processorPipeline = newPipeline(maxProcessorWorkerThreads, 
                                        minWorkerThreads, "http",
                                        port,Thread.MAX_PRIORITY);  
        processorPipeline.initPipeline();

        if ( maxReadWorkerThreads == 0){
            maxReadWorkerThreads = -1;
            logger.log(Level.WARNING,
                       "http-listener " + port + 
                       " is security-enabled and needs at least 2 threads");
        }
        
        // Only creates the pipeline if the max > 0, and the async mechanism
        // must not be enabled.
        if ( maxReadWorkerThreads > 0 && !asyncExecution){                        
            readPipeline = newPipeline(maxReadWorkerThreads, 
                                       minWorkerThreads, "read", 
                                       port,Thread.NORM_PRIORITY);
            readPipeline.initPipeline();
        } else {
            readPipeline = (maxReadWorkerThreads == 0 ? null:processorPipeline);
        }
    }
    
    
    /**
     * Create a pool of <code>ReadTask</code>
     */
    protected void initReadTask(int size){         
        ReadTask task;
        for (int i=0; i < size; i++){
            task = newReadTask();             
            readTasks.offer(task);    
        }
    }
    
    
    /**
     * Return a new <code>ReadTask</code> instance
     */
    protected ReadTask newReadTask(){
        StreamAlgorithm streamAlgorithm = null;
        
        try{
            streamAlgorithm = (StreamAlgorithm)algorithmClass.newInstance();
        } catch (InstantiationException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Algorithm: "+ algorithmClassName);
        } catch (IllegalAccessException ex){
            logger.log(Level.WARNING,
                       "Unable to instantiate Algorithm: " + algorithmClassName);
        } finally {
            if ( streamAlgorithm == null)
                streamAlgorithm = new NoParsingAlgorithm();
        }       
        streamAlgorithm.setPort(port);
        
        ReadTask task = null;
        
        /**
         * For performance reason, we need to avoid calling newInstance() when
         * the default configuration is used.
         */
        if ( !defaultAlgorithmInstalled ) {
            try{
                task = (ReadTask)streamAlgorithm.getReadTask(this).newInstance();
            } catch (InstantiationException ex){
                logger.log(Level.WARNING,
                           "Unable to instantiate Algorithm: "
                        + algorithmClassName);
            } catch (IllegalAccessException ex){
                logger.log(Level.WARNING,
                           "Unable to instantiate Algorithm: " 
                        + algorithmClassName);
            } finally {
                if ( task == null)
                    task = new DefaultReadTask();
            }  
        } else if ( maxReadWorkerThreads > 0 || asyncExecution ){
            task = new AsyncReadTask();
        } else {
            task = new DefaultReadTask();
        }
        task.setSelectorThread(this);     
        task.setPipeline(readPipeline);  
        task.setRecycle(recycleTasks);
        task.initialize(streamAlgorithm, useDirectByteBuffer,useByteBufferView);
       
        return task;
    }


    /**
     * Initialize <code>SelectorReadThread</code> used to process
     * OP_READ operations.
     */
    protected void initMultiSelectors() throws IOException, 
                                                 InstantiationException {
        for (int i = 0; i < readThreads.length; i++) {
            readThreads[i] = new SelectorReadThread();
            ((SelectorReadThread)readThreads[i]).countName = i;
            configureReadThread((SelectorReadThread)readThreads[i]);
        }
        curReadThread = 0;
    }
    
    
    protected void configureReadThread(SelectorThread multiSelector)
            throws IOException, InstantiationException {
        multiSelector.setMaxThreads(maxProcessorWorkerThreads);
        multiSelector.setBufferSize(requestBufferSize);
        multiSelector.setMaxKeepAliveRequests(maxKeepAliveRequests);
        multiSelector
                .setKeepAliveTimeoutInSeconds(keepAliveTimeoutInSeconds);
        multiSelector.maxQueueSizeInBytes = maxQueueSizeInBytes;
        multiSelector.fileCacheFactory = fileCacheFactory;     
        multiSelector.maxReadWorkerThreads = maxReadWorkerThreads;
        multiSelector.defaultResponseType = defaultResponseType;
        multiSelector.forcedRequestType = forcedRequestType;          
        multiSelector.minReadQueueLength = minReadQueueLength;
        multiSelector.maxHttpHeaderSize = maxHttpHeaderSize;
        multiSelector.isMonitoringEnabled = isMonitoringEnabled();
        multiSelector.pipelineStat = pipelineStat;
        multiSelector.globalRequestProcessor = globalRequestProcessor;

        if ( asyncExecution ) {
            multiSelector.asyncExecution = asyncExecution;
            multiSelector.asyncHandler = asyncHandler;
        }

        multiSelector.threadsIncrement = threadsIncrement;
        multiSelector.setPort(port);
        multiSelector.setAdapter(adapter);

        multiSelector.processorPipeline = processorPipeline;
        multiSelector.readPipeline = readPipeline;            
        multiSelector.readTasks = readTasks;
        multiSelector.processorTasks = processorTasks;   
        multiSelector.keepAlivePipeline = keepAlivePipeline;
        multiSelector.domain = domain;
        multiSelector.bufferResponse = bufferResponse;

        multiSelector.initEndpoint();
        multiSelector.start();
    }


    /**
     * Return an instance of <code>SelectorReadThread</code> to use
     * for registering OP_READ
     */
    private synchronized MultiSelectorThread getSelectorReadThread() {
        if (curReadThread == readThreads.length)
            curReadThread = 0;
        return readThreads[curReadThread++];
    }
    
    
    /**
     * Create a pool of <code>ProcessorTask</code>
     */
    protected void initProcessorTask(int size){
        for (int i=0; i < size; i++){           
            processorTasks.offer(newProcessorTask(false));
        }
    }  


    /**
     * Initialize <code>ProcessorTask</code>
     */
    protected void rampUpProcessorTask(){
        Iterator<ProcessorTask> iterator = processorTasks.iterator();
        while (iterator.hasNext()) {
            iterator.next().initialize();
        }
    }  
    

    /**
     * Create <code>ProcessorTask</code> objects and configure it to be ready
     * to proceed request.
     */
    protected ProcessorTask newProcessorTask(boolean initialize){                                                      
        DefaultProcessorTask task = 
                new DefaultProcessorTask(initialize, bufferResponse);
        return configureProcessorTask(task);       
    }
    
    
    protected ProcessorTask configureProcessorTask(DefaultProcessorTask task){
        task.setAdapter(adapter);
        task.setMaxHttpHeaderSize(maxHttpHeaderSize);
        task.setBufferSize(requestBufferSize);
        task.setSelectorThread(this);               
        task.setRecycle(recycleTasks);
        task.setDefaultResponseType(defaultResponseType);
        task.setForcedRequestType(forcedRequestType);
        task.setMaxPostSize(maxPostSize);
        task.setTimeout(uploadTimeout);
        task.setDisableUploadTimeout(disableUploadTimeout);
 
        if ( keepAlivePipeline.dropConnection() ) {
            task.setDropConnection(true);
        }
        
        // Asynch extentions
        if ( asyncExecution ) {
            task.setEnableAsyncExecution(asyncExecution);
            task.setAsyncHandler(asyncHandler);          
        }
                
        task.setPipeline(processorPipeline);         
        configureCompression(task);
        
        return (ProcessorTask)task;        
    }
 
    
    /**
     * Reconfigure Grizzly Asynchronous Request Processing(ARP) internal 
     * objects.
     */
    protected void reconfigureAsyncExecution(){
        for(ProcessorTask task :processorTasks){
            if (task instanceof DefaultProcessorTask) {
                ((DefaultProcessorTask)task)
                    .setEnableAsyncExecution(asyncExecution);
                ((DefaultProcessorTask)task).setAsyncHandler(asyncHandler);  
            }
        }
        
        readTasks.clear();
        initReadTask(minReadQueueLength);   
    }
    
 
    /**
     * Return a <code>ProcessorTask</code> from the pool. If the pool is empty,
     * create a new instance.
     */
    public ProcessorTask getProcessorTask(){
        ProcessorTask processorTask = null;
        if (recycleTasks) {
            processorTask = processorTasks.poll();
        }
        
        if (processorTask == null){
            processorTask = newProcessorTask(false);
        } 
        
        if ( isMonitoringEnabled() ){
           activeProcessorTasks.offer(processorTask); 
        }

        
        return processorTask;
    }
        
    
    /**
     * Return a <code>ReadTask</code> from the pool. If the pool is empty,
     * create a new instance.
     */
    public ReadTask getReadTask(SelectionKey key) throws IOException{
        ReadTask task = null;
        if ( recycleTasks ) {
            task = readTasks.poll();
        }
        
        if (task == null){
            task = newReadTask(); 
        }           

        task.setSelectionKey(key);
        return task;
    }
 
    
    // --------------------------------------------------------- Thread run --/
    
    
    /**
     * Start the endpoint (this)
     */
    public void run(){
        try{
            startEndpoint();
        } catch (Exception ex){
            logger.log(Level.SEVERE,"selectorThread.errorOnRequest", ex);
        }
    }

    
    // ------------------------------------------------------------Start ----/
    
    
    /**
     * Start the Acceptor Thread and wait for incoming connection, in a non
     * blocking mode.
     */
    public void startEndpoint() throws IOException, InstantiationException {
        running = true;
        
        kaTimeout = keepAliveTimeoutInSeconds * 1000;
        rampUpProcessorTask();
        registerComponents();
 
        displayConfiguration();

        startPipelines();
        startListener();
    }
    
    
    /**
     * Starts the <code>Pipeline</code> used by this <code>Selector</code>
     */
    protected void startPipelines(){
        if (readPipeline != null){
            readPipeline.startPipeline();
        }

        processorPipeline.startPipeline();        
    }

    
    /**
     * Stop the <code>Pipeline</code> used by this <code>Selector</code>
     */
    protected void stopPipelines(){
        if ( keepAlivePipeline != null )
            keepAlivePipeline.stopPipeline();        

        if (readPipeline != null){
            readPipeline.stopPipeline();
        }
        
        processorPipeline.stopPipeline();

    }
    
    /**
     * Start a non blocking <code>Selector</code> object.
     */
    protected void startListener(){
        synchronized(lock){
            while (running && selector.isOpen()) {
                doSelect();
            }       

            try{
                closeActiveConnections();
                stopPipelines();
                stopSelectorReadThread();

                try{
                    if ( serverSocket != null )
                        serverSocket.close();
                } catch (Throwable ex){
                    logger.log(Level.SEVERE,
                            "selectorThread.closeSocketException",ex);
                }

                try{
                    if ( serverSocketChannel != null)
                        serverSocketChannel.close();
                } catch (Throwable ex){
                    logger.log(Level.SEVERE,
                            "selectorThread.closeSocketException",ex);
                }

                try{
                    if ( selector != null)
                        selector.close();
                } catch (Throwable ex){
                    logger.log(Level.SEVERE,
                            "selectorThread.closeSocketException",ex);
                }
                unregisterComponents();
            } catch (Throwable t){
                logger.log(Level.SEVERE,"selectorThread.stopException",t);
            } 
        }
    }
    
    
    /**
     * Execute a <code>Selector.select()</code> operation.
     */
    protected void doSelect(){
        SelectionKey key = null;
        Set readyKeys;
        Iterator<SelectionKey> iterator;
        int selectorState; 

        try{
            selectorState = 0;
            enableSelectionKeys();                

            try{                
                selectorState = selector.select(selectorTimeout);
            } catch (CancelledKeyException ex){
                ;
            }

            if (!running) return;

            readyKeys = selector.selectedKeys();
            iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();
                if (key.isValid()) {
                    handleConnection(key);
                } else {
                    cancelKey(key);
                }
            }
            
            expireIdleKeys();
            
            if (selectorState <= 0){
                selector.selectedKeys().clear();
                return;
            }
        } catch (Throwable t){
            if (key != null && key.isValid()){
                logger.log(Level.SEVERE,"selectorThread.errorOnRequest",t);
            } else {
                logger.log(Level.FINE,"selectorThread.errorOnRequest",t);
            }

            if ( key != null ){
                key.attach(null);
                key.cancel();
            }
        }
    }
    
    
    /**
     * Cancel keep-alive connections.
     */
    protected void expireIdleKeys(){
        if ( keepAliveTimeoutInSeconds <= 0 || !selector.isOpen()) return;
        long current = System.currentTimeMillis();

        if (current < nextKeysExpiration) {
            return;
        }
        nextKeysExpiration = current + kaTimeout;
        
        Set<SelectionKey> readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        SelectionKey key;
        while (running && iterator.hasNext()) {
            key = iterator.next();          
            // Keep-alive expired
            Object attachment = key.attachment();
            if (attachment != null) {
                
                if ( !defaultAlgorithmInstalled 
                        && !(attachment instanceof Long)) { 
                    continue; 
                }
                    
                try{
                    long expire = (Long)attachment;
                    if (current - expire >= kaTimeout) {
                        if (enableNioLogging){
                            logger.log(Level.INFO,
                                    "Keep-Alive expired for SocketChannel " + 
                                    key.channel());
                        }          
                        cancelKey(key);
                    } else if (expire + kaTimeout < nextKeysExpiration){
                        nextKeysExpiration = expire + kaTimeout;
                    }
                } catch (ClassCastException ex){                            
                    if (logger.isLoggable(Level.FINEST)){
                        logger.log(Level.FINEST,
                                   "Invalid SelectionKey attachment",ex);
                    }
                }
            }
        }                    
    }  

    
    /**
     * Handle an incoming operation on the channel. It is always an ACCEPT or
     * a READ.
     */
    protected void handleConnection(SelectionKey key) throws IOException,
                                                           InterruptedException{
                                                    
        Task task = null;
        if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
            handleAccept(key);
            return;
        } else if ((key.readyOps() & SelectionKey.OP_READ) 
                                                    == SelectionKey.OP_READ) {
            task = handleRead(key);
        } 
 
        if ( ((SocketChannel)key.channel()).isOpen() ) {
            task.execute();
        } else {
            cancelKey(key);
        }
    }
        
      
    
    /**
     * Handle OP_ACCEPT
     */
    protected void handleAccept(SelectionKey key) throws IOException{
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel channel = server.accept();

        if (enableNioLogging){
            logger.log(Level.INFO,"Handling OP_ACCEPT on SocketChannel " + 
                    channel);
        }
        
        if (channel != null) {
            if ( multiSelectorsCount > 1 ) {
                MultiSelectorThread srt = getSelectorReadThread();
                srt.addChannel(channel);
            } else {
                channel.configureBlocking(false);
                SelectionKey readKey = 
                        channel.register(selector, SelectionKey.OP_READ);
                setSocketOptions(((SocketChannel)readKey.channel()).socket());
                readKey.attach(System.currentTimeMillis());
            }

            if (isMonitoringEnabled()) {
                getRequestGroupInfo().increaseCountOpenConnections();
                pipelineStat.incrementTotalAcceptCount();
            }
        }
    }
    
    
    /**
     * Handle OP_READ
     */ 
    protected ReadTask handleRead(SelectionKey key) throws IOException{                   
        // disable OP_READ on key before doing anything else 
        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
        
        if (enableNioLogging){
            logger.log(Level.INFO,"Handling OP_READ on SocketChannel " + 
                    key.channel());
        }      
        
        Object attach = key.attachment();
        if (!defaultAlgorithmInstalled) {
            if (key.isValid() && attach != null && attach instanceof ReadTask){
                key.attach(null);
                return (ReadTask)attach;
            }
        } 
        return getReadTask(key);
    }
    
    
    /**
     * Cancel the current <code>SelectionKey</code>
     */
    public void cancelKey(SelectionKey key){
        if (key == null){
            return;
        }

        keepAlivePipeline.untrap(key);       
        if (!processorPipeline.expireKey(key)){
            return;
        }

        if (enableNioLogging){
            logger.log(Level.INFO,"Closing SocketChannel " + 
                    key.channel());
        }

        Socket socket = ((SocketChannel)key.channel()).socket();
        try{
            if (!socket.isInputShutdown()){
                socket.shutdownInput();
            }
        } catch (IOException ex){
            ;
        }
        
        try{
            if (!socket.isOutputShutdown()){
                socket.shutdownOutput();
            }
        } catch (IOException ex){
            ;
        }

        try{
            if (!socket.isClosed()){
                socket.close();
            }
        } catch (IOException ex){
            ;
        } finally {
            try{
                // This is not needed but just to make sure the JDK isn't 
                // leaking any fd.
                if (key.channel().isOpen()){
                    key.channel().close();
                }
            } catch (IOException ex){
                logger.log(Level.FINEST,"selectorThread.unableToCloseKey", key);
            }
            if (isMonitoringEnabled()) {
                getRequestGroupInfo().decreaseCountOpenConnections();
            }
        }

        
        // Set the attachement to null so the Selector.java handleConnection
        // stop processing this key.
        key.attach(null);
        key.cancel();
        key = null;
    }
    
    
    /**
     * Returns the <code>Task</code> object to the pool.
     */
    public void returnTask(Task task){
        // Returns the object to the pool.
        if (task != null) {
            if (task.getType() == Task.PROCESSOR_TASK){
                                
                if ( isMonitoringEnabled() ){
                   activeProcessorTasks.remove(((DefaultProcessorTask)task));
                }  
                
                processorTasks.offer((DefaultProcessorTask)task);
            } else if (task.getType() == Task.READ_TASK){
                readTasks.offer((ReadTask)task);               
            }
        }
    }
    

    /**
     * Wakes up the <code>Selector</code> associated with this thread.
     */
    public void wakeup(){
        selector.wakeup();
    }

    
    /**
     * Clear all cached <code>Tasks</code> 
     */
    protected void clearTasks(){
        processorTasks.clear();
        readTasks.clear();
    }

    
    /**
     * Cancel the <code>threadID</code> execution. Return <code>true</code>
     * if it is successful.
     *
     * @param id the thread name to cancel
     */
    public boolean cancelThreadExecution(long cancelThreadID){
   
        if ( multiSelectorsCount > 1 ){
            boolean cancelled = false;
            for (MultiSelectorThread readSelector : readThreads) {
                cancelled = ((SelectorReadThread)readSelector).
                        cancelThreadExecution(cancelThreadID);   
                if (cancelled) return true;
            }       
            return false;
        }
                       
        if (activeProcessorTasks.size() == 0) return false;
        
        Iterator<ProcessorTask> iterator = activeProcessorTasks.iterator();
        ProcessorTask processorTask;
        long threadID;
        while( iterator.hasNext() ){
            processorTask = iterator.next();
            threadID = processorTask.getWorkerThreadID();
            if (threadID == cancelThreadID){
                processorTask.cancelTask("Request cancelled.","500");
                logger.log(Level.WARNING,
                        "Thread Request Cancelled: " + threadID);     
                return processorTask.getPipeline().interruptThread(threadID);
            }
        }
        return false;
    }

    
    /**
     * Stop the extra <code>SelectorReadThread</code>
     */
    private void stopSelectorReadThread(){
        if ( readThreads != null ){
            for (int i = 0; i < readThreads.length; i++) {
                readThreads[i].stopEndpoint();       
            }
        }
    }
    
    
    private void closeActiveConnections(){
        Set<SelectionKey> readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
            key = iterator.next();
            if (key.channel() instanceof SocketChannel){
                cancelKey(key);
            } else {
                key.cancel();
            }
        }        
    }
    
    
    // ---------------------------------------------------Endpoint Lifecycle --/
 
    public void stopEndpoint() {
        if (!running) return;
        running = false;
        synchronized(lock){
            // Wait for the main thread to stop.
            clearTasks();
        }
    }
    
    // ------------------------------------------------------Public methods--/
 

    public void setMaxThreads(int maxThreads) {
        if ( maxThreads == 1 ) {
            maxProcessorWorkerThreads = 5;
        } else {
            maxProcessorWorkerThreads = maxThreads;
        }
    }

    public int getMaxThreads() {
        return maxProcessorWorkerThreads;
    }

    public void setMaxSpareThreads(int maxThreads) {
    }

    public int getMaxSpareThreads() {
        return maxProcessorWorkerThreads;
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public int getMinSpareThreads() {
        return  minSpareThreads;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port ) {
        this.port=port;
    }

    public InetAddress getAddress() {
        return inet;
    }

    public void setAddress(InetAddress inet) {
        this.inet=inet;
    }


    public boolean isRunning() {
        return running;
    }
 
    
    /**
     * Provides the count of request threads that are currently
     * being processed by the container
     *
     * @return The count of busy threads 
     */
    public int getCurrentBusyProcessorThreads() {
        int busy = 0;
        
        // multi selector support
        if (multiSelectorsCount > 1) {
            for (MultiSelectorThread readSelector : readThreads) {
                busy += ((SelectorReadThread)readSelector)
                                .getCurrentBusyProcessorThreads();   
            }

        } else {
            busy = processorPipeline.getCurrentThreadsBusy();
        }

        return busy;  
    }


    /**
     * Sets the timeout in ms of the server sockets created by this
     * server. This method allows the developer to make servers
     * more or less responsive to having their server sockets
     * shut down.
     *
     * <p>By default this value is 1000ms.
     */
    public void setServerTimeout(int timeout) {
        this.serverTimeout = timeout;
    }

    public boolean getTcpNoDelay() {
        return tcpNoDelay;
    }
    
    public void setTcpNoDelay( boolean b ) {
        tcpNoDelay=b;
    }

    public int getSoLinger() {
        return linger;
    }
    
    public void setSoLinger( int i ) {
        linger=i;
    }

    public int getSoTimeout() {
        return socketTimeout;
    }
    
    public void setSoTimeout( int i ) {
        socketTimeout=i;
    }
    
    public int getServerSoTimeout() {
        return serverTimeout;
    }  
    
    public void setServerSoTimeout( int i ) {
        serverTimeout=i;
    }
 
    // ------------------------------------------------------ Connector Methods


    /**
     * Get the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public int getQueueSizeInBytes(){
        return maxQueueSizeInBytes;
    }
    
    
    
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    
    
    /** 
     * Set the maximum number of Keep-Alive requests that we will honor.
     */
    public void setMaxKeepAliveRequests(int mkar) {
        maxKeepAliveRequests = mkar;
    }
    

    /** 
     * Sets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @param timeout Keep-alive timeout in number of seconds
     */    
    public void setKeepAliveTimeoutInSeconds(int timeout) {
        keepAliveTimeoutInSeconds = timeout;
        keepAliveStats.setSecondsTimeouts(timeout);
    }


    /** 
     * Gets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @return Keep-alive timeout in number of seconds
     */    
    public int getKeepAliveTimeoutInSeconds() {
        return keepAliveTimeoutInSeconds;
    }


    /** 
     * Sets the number of keep-alive threads.
     *
     * @param threadCount Number of keep-alive threads
     */    
    public void setKeepAliveThreadCount(int threadCount) {
        keepAlivePipeline.setMaxThreads(threadCount);
    }


    /**
     * Set the associated adapter.
     * 
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }


    /**
     * Get the associated adapter.
     * 
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }
    
    
    protected void setSocketOptions(Socket socket){
        try{
            if(linger >= 0 ) {
                socket.setSoLinger( true, linger);
            }
        } catch (SocketException ex){
            logger.log(Level.WARNING,
                        "setSoLinger exception ",ex);
        }
        
        try{
            if( tcpNoDelay )
                socket.setTcpNoDelay(tcpNoDelay);
        } catch (SocketException ex){
            logger.log(Level.WARNING,
                        "setTcpNoDelay exception ",ex);
        }
        
        try{
            if ( maxReadWorkerThreads != 0)
                socket.setReuseAddress(reuseAddress);
        } catch (SocketException ex){
            logger.log(Level.WARNING,
                        "setReuseAddress exception ",ex);
        }   
        
        try{
            if(oOBInline){
                socket.setOOBInline(oOBInline);
            }
        } catch (SocketException ex){
            logger.log(Level.WARNING,
                        "setOOBInline exception ",ex);
        }        
        
    }
    
    // ------------------------------- JMX and Monnitoring support --------//
    
    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
        // Do nothing
    }

    public void preDeregister() throws Exception {
        // Do nothing
    }

    public void postDeregister() {
        // Do nothing
    }  


    /**
     * Register JMX components.
     */
    protected void registerComponents(){

        if( this.domain != null  && jmxManagement != null) {

            try {
                globalRequestProcessorName = new ObjectName(
                    domain + ":type=GlobalRequestProcessor,name=http" + port);
                jmxManagement.registerComponent(globalRequestProcessor,
                                                globalRequestProcessorName,
                                                null);
 
                keepAliveMbeanName = new ObjectName(
                    domain + ":type=PWCKeepAlive,name=http" + port);
                jmxManagement.registerComponent(keepAliveStats,
                                                keepAliveMbeanName,
                                                null);

                pwcConnectionQueueMbeanName = new ObjectName(
                    domain + ":type=PWCConnectionQueue,name=http" + port);
                jmxManagement.registerComponent(pipelineStat,
                                                pwcConnectionQueueMbeanName,
                                                null);
                
                pwcFileCacheMbeanName = new ObjectName(
                    domain + ":type=PWCFileCache,name=http" + port);
                jmxManagement.registerComponent(fileCacheFactory,
                                                pwcFileCacheMbeanName,
                                                null);                
            } catch (Exception ex) {
                logger.log(Level.WARNING,
                           "selectorThread.mbeanRegistrationException",
                           new Object[]{new Integer(port),ex});
            }
        }

    }
    
    
    /**
     * Unregister components.
     **/
    protected void unregisterComponents(){

        if (this.domain != null && jmxManagement != null) {
            try {
                if (globalRequestProcessorName != null) {
                    jmxManagement.unregisterComponent(globalRequestProcessorName);
                }
                if (keepAliveMbeanName != null) {
                    jmxManagement.unregisterComponent(keepAliveMbeanName);
                }
                if (pwcConnectionQueueMbeanName != null) {
                    jmxManagement.unregisterComponent(pwcConnectionQueueMbeanName);
                }
                if (pwcFileCacheMbeanName != null) {
                    jmxManagement.unregisterComponent(pwcFileCacheMbeanName);
                }                    
            } catch (Exception ex) {
                logger.log(Level.WARNING,
                           "mbeanDeregistrationException",
                           new Object[]{new Integer(port),ex});
            }
        }
    }

    
    /**
     * Enable gathering of monitoring datas.
     */
    public void enableMonitoring(){
        isMonitoringEnabled = true;
        enablePipelineStats();      
        if ( readThreads != null ) {
            for (int i = 0; i < readThreads.length; i++) {
                ((SelectorReadThread)readThreads[i]).isMonitoringEnabled = true;
            }
        }
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);
    }
    
    
    /**
     * Disable gathering of monitoring datas. 
     */
    public void disableMonitoring(){
        disablePipelineStats();  
        if ( readThreads != null ) {
            for (int i = 0; i < readThreads.length; i++) {
                ((SelectorReadThread)readThreads[i]).isMonitoringEnabled = false;
            }
        }
        fileCacheFactory.setIsMonitoringEnabled(isMonitoringEnabled);        
    }

    
    /**
     * Returns <code>true</code> if monitoring has been enabled, 
     * <code>false</code> otherwise.
     */
    public boolean isMonitoringEnabled() {
        return isMonitoringEnabled;
    }

    
    public RequestGroupInfo getRequestGroupInfo() {
        return globalRequestProcessor;
    }


    public KeepAliveStats getKeepAliveStats() {
        return keepAliveStats;
    }


    /*
     * Initializes the web container monitoring level from the domain.xml.
     */
    protected void initMonitoringLevel() {
        pipelineStat = new PipelineStatistic(port);
        pipelineStat.setQueueSizeInBytes(maxQueueSizeInBytes);
    }
 
    
    public int getMaxHttpHeaderSize() {
        return maxHttpHeaderSize;
    }
    
    public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
        this.maxHttpHeaderSize = maxHttpHeaderSize;
    }
    
        
    /**
     * The minimun threads created at startup.
     */ 
    public void setMinThreads(int minWorkerThreads){
        this.minWorkerThreads = minWorkerThreads;
    }


    /**
     * Set the request input buffer size
     */
    public void setBufferSize(int requestBufferSize){
        this.requestBufferSize = requestBufferSize;
    }
    

    /**
     * Return the request input buffer size
     */
    public int getBufferSize(){
        return requestBufferSize;
    }
    
    
    public Selector getSelector(){
        return selector;
    }

    /************************* PWCThreadPool Stats *************************/

    public int getCountThreadsStats() {

        int ret = processorPipeline.getCurrentThreadCount();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getCurrentThreadCount();
        }

        return ret;
    }


    public int getCountThreadsIdleStats() {

        int ret = processorPipeline.getWaitingThread();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getWaitingThread();
        }

        return ret;
    }


    /************************* HTTPListener Stats *************************/

    public int getCurrentThreadCountStats() {

        int ret = processorPipeline.getCurrentThreadCount();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getCurrentThreadCount();
        }

        return ret;
    }


    public int getCurrentThreadsBusyStats() {

        int ret = processorPipeline.getCurrentThreadsBusy();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getCurrentThreadsBusy();
        }
 
        return ret;
    }

    public int getMaxSpareThreadsStats() {

        int ret = processorPipeline.getMaxSpareThreads();
 
        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getMaxSpareThreads();
        }

        return ret;
    }


    public int getMinSpareThreadsStats() {

        int ret = processorPipeline.getMinSpareThreads();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getMinSpareThreads();
        }

        return ret;
    }


    public int getMaxThreadsStats() {

        int ret = processorPipeline.getMaxThreads();

        if (readPipeline != null
                && readPipeline != processorPipeline) {
            ret += readPipeline.getMaxThreads();
        }
        
        return ret;
    }


    //------------------------------------------------- FileCache config -----/

    
    /**
     * Remove a context path from the <code>FileCache</code>.
     */
    public void removeCacheEntry(String contextPath){  
        ConcurrentHashMap<String,FileCacheEntry> 
                cachedEntries = fileCacheFactory.getCache();
        
        if ( cachedEntries == null){
            return;
        }
        
        Iterator<String> iterator = cachedEntries.keySet().iterator();
        String cachedPath;
        while (iterator.hasNext()){
            cachedPath = iterator.next();
            if ( cachedPath.startsWith(contextPath) ){
                cachedEntries.remove(cachedPath).run();
            }            
        }
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
    public boolean isFileCacheEnabled(){
        return isFileCacheEnabled;
    }

    
    /**
     * Is the file caching mechanism enabled.
     */
    public void setFileCacheIsEnabled(boolean isFileCacheEnabled){
        this.isFileCacheEnabled = isFileCacheEnabled;
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

    // --------------------------------------------------------------------//
    
    /**
     * Enable the <code>AsyncHandler</code> used when asynchronous
     */
    public void setEnableAsyncExecution(boolean asyncExecution){
        this.asyncExecution = asyncExecution;     
        if (running){
            reconfigureAsyncExecution();
        }
    }
    
       
    /**
     * Return true when asynchronous execution is 
     * enabled.
     */    
    public boolean getEnableAsyncExecution(){
        return asyncExecution;
    }
    
    
    /**
     * Set the <code>AsyncHandler</code> used when asynchronous execution is 
     * enabled.
     */
    public void setAsyncHandler(AsyncHandler asyncHandler){
        this.asyncHandler = asyncHandler;     
    }
    
       
    /**
     * Return the <code>AsyncHandler</code> used when asynchronous execution is 
     * enabled.
     */    
    public AsyncHandler getAsyncHandler(){
        return asyncHandler;
    }
    
    
    /**
     * Set the logger used by this instance.
     */
    public static void setLogger(Logger l){
        if ( l != null )
            logger = l;
    }

    
    /**
     * Return the logger used by the Grizzly classes.
     */
    public static Logger logger(){
        return logger;
    }
    
    
    /**
     * Set the document root folder
     */
    public static void setWebAppRootPath(String rf){
        rootFolder = rf;
    }
    
    
    /**
     * Return the folder's root where application are deployed.
     */
    public static String getWebAppRootPath(){
        return rootFolder;
    }
    
    
    public int getMaxReadWorkerThreads(){
        return maxReadWorkerThreads;
    }
    
    
    /**
     * Return the <code>Pipeline</code> used to handle OP_READ.
     */
    public Pipeline getReadPipeline(){
        return readPipeline;
    }
    
    // ------------------------------------------------------ Compression ---//


    protected void configureCompression(DefaultProcessorTask processorTask){
        processorTask.setCompression(compression);
        processorTask.addNoCompressionUserAgent(noCompressionUserAgents);
        processorTask.addCompressableMimeType(compressableMimeTypes);
        processorTask.setCompressionMinSize(compressionMinSize);
        processorTask.addRestrictedUserAgent(restrictedUserAgents);
    }


    // ------------------------------------------------------ Debug ---------//
    
        
    /**
     * Display the Grizzly configuration parameters.
     */
    private void displayConfiguration(){
       if (displayConfiguration){
            logger.log(Level.INFO,
                    "\n Grizzly configuration for port " 
                    + port 
                    + "\n\t maxThreads: " 
                    + maxProcessorWorkerThreads 
                    + "\n\t minThreads: " 
                    + minWorkerThreads 
                    + "\n\t ByteBuffer size: " 
                    + requestBufferSize 
                    + "\n\t useDirectByteBuffer: "
                    + useDirectByteBuffer                       
                    + "\n\t useByteBufferView: "                        
                    + useByteBufferView                   
                    + "\n\t maxHttpHeaderSize: " 
                    + maxHttpHeaderSize
                    + "\n\t maxKeepAliveRequests: "
                    + maxKeepAliveRequests
                    + "\n\t keepAliveTimeoutInSeconds: "
                    + keepAliveTimeoutInSeconds
                    + "\n\t Static File Cache enabled: "                        
                    + isFileCacheEnabled                    
                    + "\n\t Stream Algorithm : "                        
                    + algorithmClassName        
                    + "\n\t Pipeline : "                        
                    + pipelineClassName                    
                    + "\n\t Round Robin Selector Algorithm enabled: "                        
                    + ( multiSelectorsCount > 1 )
                    + "\n\t Round Robin Selector pool size: "
                    + multiSelectorsCount
                    + "\n\t recycleTasks: "                        
                    + recycleTasks
                    + "\n\t Asynchronous Request Processing enabled: " 
                    + asyncExecution); 
        }
    }

    
    /**
     * Return <code>true</code> if the reponse is buffered.
     */
    public boolean getBufferResponse() {
        return bufferResponse;
    }

    
    /**
     * <code>true</code>if the reponse willk be buffered.
     */
    public void setBufferResponse(boolean bufferResponse) {
        this.bufferResponse = bufferResponse;
    }
    
    
    /**
     * Enable Comet/Poll request support.
     */
    public void enableCometSupport(boolean enableComet){
        if ( enableComet ){
            asyncExecution = true;
            setBufferResponse(false);    
            isFileCacheEnabled = false;
            isLargeFileCacheEnabled = false;
            asyncHandler = new DefaultAsyncHandler();
            asyncHandler.addAsyncFilter(new CometAsyncFilter()); 
            
            SelectorThread.logger()
                .log(Level.INFO,"Enabling Grizzly ARP Comet support.");
            
        } else {
            asyncExecution = false;
        }
    }
    
       
    /**
     * Enable Application Resource Allocation Grizzly Extension.
     */
    public void enableRcmSupport(boolean rcmSupport){
        if (rcmSupport){
            // Avoid dependency on that extension. If the package is renamed
            // an exception will be logged later when the SelectorThread start.
            pipelineClassName = "com.sun.enterprise.web.ara.IsolationPipeline";
        }        
    }
    

    // ----------------------------------------------Setter/Getter-----------//
    
    
    public int getServerTimeout() {
        return serverTimeout;
    }

    public InetAddress getInet() {
        return inet;
    }

    public void setInet(InetAddress inet) {
        this.inet = inet;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel){
        this.serverSocketChannel = serverSocketChannel;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ObjectName getOname() {
        return oname;
    }

    public void setOname(ObjectName oname) {
        this.oname = oname;
    }

    public ObjectName getGlobalRequestProcessorName() {
        return globalRequestProcessorName;
    }

    public void setGlobalRequestProcessorName
            (ObjectName globalRequestProcessorName) {
        this.globalRequestProcessorName = globalRequestProcessorName;
    }

    public ObjectName getKeepAliveMbeanName() {
        return keepAliveMbeanName;
    }

    public void setKeepAliveMbeanName(ObjectName keepAliveMbeanName) {
        this.keepAliveMbeanName = keepAliveMbeanName;
    }

    public ObjectName getPwcConnectionQueueMbeanName() {
        return pwcConnectionQueueMbeanName;
    }

    public void setPwcConnectionQueueMbeanName
            (ObjectName pwcConnectionQueueMbeanName) {
        this.pwcConnectionQueueMbeanName = pwcConnectionQueueMbeanName;
    }

    public ObjectName getPwcFileCacheMbeanName() {
        return pwcFileCacheMbeanName;
    }

    public void setPwcFileCacheMbeanName(ObjectName pwcFileCacheMbeanName) {
        this.pwcFileCacheMbeanName = pwcFileCacheMbeanName;
    }

    public MBeanServer getMserver() {
        return mserver;
    }

    public void setMserver(MBeanServer mserver) {
        this.mserver = mserver;
    }

    public ObjectName getProcessorWorkerThreadName() {
        return processorWorkerThreadName;
    }

    public void setProcessorWorkerThreadName
            (ObjectName processorWorkerThreadName) {
        this.processorWorkerThreadName = processorWorkerThreadName;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int getLinger() {
        return linger;
    }

    public void setLinger(int linger) {
        this.linger = linger;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }

    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }

    public String getRestrictedUserAgents() {
        return restrictedUserAgents;
    }

    public void setRestrictedUserAgents(String restrictedUserAgents) {
        this.restrictedUserAgents = restrictedUserAgents;
    }

    public String getCompressableMimeTypes() {
        return compressableMimeTypes;
    }

    public void setCompressableMimeTypes(String compressableMimeTypes) {
        this.compressableMimeTypes = compressableMimeTypes;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public boolean isBufferResponse() {
        return bufferResponse;
    }

    public int getMinReadQueueLength() {
        return minReadQueueLength;
    }

    public void setMinReadQueueLength(int minReadQueueLength) {
        this.minReadQueueLength = minReadQueueLength;
    }

    public int getMinProcessorQueueLength() {
        return minProcessorQueueLength;
    }

    public void setMinProcessorQueueLength(int minProcessorQueueLength) {
        this.minProcessorQueueLength = minProcessorQueueLength;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public int getSelectorReadThreadsCount() {
        return multiSelectorsCount;
    }

    public void setSelectorReadThreadsCount(int multiSelectorsCount) {
        this.multiSelectorsCount = multiSelectorsCount;
    }

    public void setReadPipeline(Pipeline readPipeline) {
        this.readPipeline = readPipeline;
    }

    public Pipeline getProcessorPipeline() {
        return processorPipeline;
    }

    public void setProcessorPipeline(Pipeline processorPipeline) {
        this.processorPipeline = processorPipeline;
    }

    public PipelineStatistic getPipelineStat() {
        return pipelineStat;
    }

    public void setPipelineStat(PipelineStatistic pipelineStat) {
        this.pipelineStat = pipelineStat;
    }

    public String getPipelineClassName() {
        return pipelineClassName;
    }

    public void setPipelineClassName(String pipelineClassName) {
        this.pipelineClassName = pipelineClassName;
    }

    public int getMaxProcessorWorkerThreads() {
        return maxProcessorWorkerThreads;
    }

    public void setMaxProcessorWorkerThreads(int maxProcessorWorkerThreads) {
        this.maxProcessorWorkerThreads = maxProcessorWorkerThreads;
    }

    public void setMaxReadWorkerThreads(int maxReadWorkerThreads) {
        this.maxReadWorkerThreads = maxReadWorkerThreads;
    }

    public int getMinWorkerThreads() {
        return minWorkerThreads;
    }

    public void setMinWorkerThreads(int minWorkerThreads) {
        this.minWorkerThreads = minWorkerThreads;
    }

    public int getThreadsIncrement() {
        return threadsIncrement;
    }

    public void setThreadsIncrement(int threadsIncrement) {
        this.threadsIncrement = threadsIncrement;
    }

    public int getThreadsTimeout() {
        return threadsTimeout;
    }

    public void setThreadsTimeout(int threadsTimeout) {
        this.threadsTimeout = threadsTimeout;
    }

    public boolean isUseDirectByteBuffer() {
        return useDirectByteBuffer;
    }

    public void setUseDirectByteBuffer(boolean useDirectByteBuffer) {
        this.useDirectByteBuffer = useDirectByteBuffer;
    }

    public RequestGroupInfo getGlobalRequestProcessor() {
        return globalRequestProcessor;
    }

    public void setGlobalRequestProcessor(RequestGroupInfo globalRequestProcessor) {
        this.globalRequestProcessor = globalRequestProcessor;
    }

    public void setKeepAliveStats(KeepAliveStats keepAliveStats) {
        this.keepAliveStats = keepAliveStats;
    }

    public boolean isDisplayConfiguration() {
        return displayConfiguration;
    }

    public void setDisplayConfiguration(boolean displayConfiguration) {
        this.displayConfiguration = displayConfiguration;
    }

    public boolean isIsMonitoringEnabled() {
        return isMonitoringEnabled;
    }

    public void setIsMonitoringEnabled(boolean isMonitoringEnabled) {
        this.isMonitoringEnabled = isMonitoringEnabled;
    }

    public int getCurrentConnectionNumber() {
        return currentConnectionNumber;
    }

    public void setCurrentConnectionNumber(int currentConnectionNumber) {
        this.currentConnectionNumber = currentConnectionNumber;
    }

    public void setIsWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    public boolean isUseByteBufferView() {
        return useByteBufferView;
    }

    public void setUseByteBufferView(boolean useByteBufferView) {
        this.useByteBufferView = useByteBufferView;
    }

    public int getKaTimeout() {
        return kaTimeout;
    }

    public void setKaTimeout(int kaTimeout) {
        this.kaTimeout = kaTimeout;
    }

    public boolean isRecycleTasks() {
        return recycleTasks;
    }

    public void setRecycleTasks(boolean recycleTasks) {
        this.recycleTasks = recycleTasks;
    }

    public static int getSelectorTimeout() {
        return selectorTimeout;
    }

    public static void setSelectorTimeout(int aSelectorTimeout) {
        selectorTimeout = aSelectorTimeout;
    }

    public int getMaxQueueSizeInBytes() {
        return maxQueueSizeInBytes;
    }

    public void setMaxQueueSizeInBytes(int maxQueueSizeInBytes) {
        this.maxQueueSizeInBytes = maxQueueSizeInBytes;
    }

    public Class getAlgorithmClass() {
        return algorithmClass;
    }

    public void setAlgorithmClass(Class algorithmClass) {
        this.algorithmClass = algorithmClass;
    }

    public String getAlgorithmClassName() {
        return algorithmClassName;
    }

    public void setAlgorithmClassName(String algorithmClassName) {
        this.algorithmClassName = algorithmClassName;
    }

    public int getSsBackLog() {
        return ssBackLog;
    }

    public void setSsBackLog(int ssBackLog) {
        this.ssBackLog = ssBackLog;
    }

    public long getNextKeysExpiration() {
        return nextKeysExpiration;
    }

    public void setNextKeysExpiration(long nextKeysExpiration) {
        this.nextKeysExpiration = nextKeysExpiration;
    }

    public String getDefaultResponseType() {
        return defaultResponseType;
    }

    public void setDefaultResponseType(String defaultResponseType) {
        this.defaultResponseType = defaultResponseType;
    }

    public String getForcedRequestType() {
        return forcedRequestType;
    }

    public void setForcedRequestType(String forcedRequestType) {
        this.forcedRequestType = forcedRequestType;
    }

    public static String getRootFolder() {
        return rootFolder;
    }

    public static void setRootFolder(String aRootFolder) {
        rootFolder = aRootFolder;
    }

    public ConcurrentLinkedQueue<SelectionKey> getKeysToEnable() {
        return keysToEnable;
    }

    public void setKeysToEnable(ConcurrentLinkedQueue<SelectionKey> keysToEnable) {
        this.keysToEnable = keysToEnable;
    }

    public ConcurrentLinkedQueue<ProcessorTask> getProcessorTasks() {
        return processorTasks;
    }

    public void setProcessorTasks(ConcurrentLinkedQueue<ProcessorTask> 
            processorTasks) {
        this.processorTasks = processorTasks;
    }

    public ConcurrentLinkedQueue<ReadTask> getReadTasks() {
        return readTasks;
    }

    public void setReadTasks(ConcurrentLinkedQueue<ReadTask> readTasks) {
        this.readTasks = readTasks;
    }

    public ConcurrentLinkedQueue<ProcessorTask> getActiveProcessorTasks() {
        return activeProcessorTasks;
    }

    public void setActiveProcessorTasks(ConcurrentLinkedQueue<ProcessorTask> 
            activeProcessorTasks) {
        this.activeProcessorTasks = activeProcessorTasks;
    }

    public int getCurReadThread() {
        return curReadThread;
    }

    public void setCurReadThread(int curReadThread) {
        this.curReadThread = curReadThread;
    }

    public static Logger getLogger() {
        return logger;
    }

    public Management getManagement() {
        return jmxManagement;
    }

    public void setManagement(Management jmxManagement) {
        this.jmxManagement = jmxManagement;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    
    /**
     * Set the <code>ClassLoader</code> used to load configurable
     * classes (Pipeline, StreamAlgorithm).
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public boolean isEnableNioLogging() {
        return enableNioLogging;
    }

    public void setEnableNioLogging(boolean enableNioLogging) {
        this.enableNioLogging = enableNioLogging;
    }

    public int getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(int maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    public void setReuseAddress(boolean reuseAddress){
        this.reuseAddress = reuseAddress;
    }

    public boolean getReuseAddress(){
        return reuseAddress;
    }

    public KeepAlivePipeline getKeepAlivePipeline() {
        return keepAlivePipeline;
    }

    public void setKeepAlivePipeline(KeepAlivePipeline keepAlivePipeline) {
        this.keepAlivePipeline = keepAlivePipeline;
    }

    
    /**
     * Set the flag to control upload time-outs.
     */
    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }

    
    /**
     * Get the flag that controls upload time-outs.
     */
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }
    
    
    /**
     * Set the upload timeout.
     */
    public void setUploadTimeout(int uploadTimeout) {
        this.uploadTimeout = uploadTimeout ;
    }

    
    /**
     * Get the upload timeout.
     */
    public int getTimeout() {
        return uploadTimeout;
    }
}
