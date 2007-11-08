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

import com.sun.enterprise.web.connector.grizzly.comet.CometEngine;
import com.sun.enterprise.web.connector.grizzly.ssl.SSLUtils;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class SelectorThreadConfig{  
    
    /**
     * System property for the selector timeout value.
     */
    private static final String SELECTOR_TIMEOUT = 
                "com.sun.enterprise.web.connector.grizzly.selector.timeout";
    
    /**
     * The minimum number of threads used when creating a new 
     * <code>Pipeline</code>
     */
    private static final String MIN_THREAD=
               "com.sun.enterprise.web.connector.grizzly.minWorkerThreads";
    
    /**
     * The maximum number of threads used when creating a new 
     * <code>Pipeline</code>
     */
    private static final String MAX_THREAD=
               "com.sun.enterprise.web.connector.grizzly.maxThreads";
    
    
    /**
     * Property used to turn on/off NIO blocking mode.
     */
    private final static String DISPLAY_CONFIGURATION=
                "com.sun.enterprise.web.connector.grizzly.displayConfiguration";


    private final static String MAX_KEEP_ALIVE_REQUEST =
               "com.sun.enterprise.web.connector.grizzly.maxKeepAliveRequests"; 


    private final static String ENABLE_COMET_SUPPORT =
               "com.sun.enterprise.web.connector.grizzly.enableCometSupport"; 
    /**
     * Is the <code>ByteBuffer</code> used by the <code>ReadTask</code> use
     * direct <code>ByteBuffer</code> or not.
     */
    private final static String DIRECT_BYTE_BUFFER_READ =
               "com.sun.enterprise.web.connector.grizzly.useDirectByteBuffer";    
 
    /**
     * Always attach a <code>ProcessorTask</code> when creating 
     * a <code>ReadTask</code>
     */
    private final static String PIPELINE_CLASS =
               "com.sun.enterprise.web.connector.grizzly.pipelineClass";

    private final static String MAX_SELECTOR_READ_THREAD=
               "com.sun.enterprise.web.connector.grizzly.maxSelectorReadThread"; 
    private final static String HTTP_HEADER_BUFFER_SIZE =
               "com.sun.enterprise.web.connector.grizzly.maxHttpHeaderSize"; 
    
    private final static String BYTE_BUFFER_VIEW =
               "com.sun.enterprise.web.connector.grizzly.useByteBufferView"; 

    private final static String ALGORITHM_CLASS_NAME=
        "com.sun.enterprise.web.connector.grizzly.algorithmClassName";
    
    private final static String MAX_SELECTOR = 
        "com.sun.enterprise.web.connector.grizzly.maxSelectors";
    
    private final static String FACTORY_TIMEOUT = 
        "com.sun.enterprise.web.connector.grizzly.factoryTimeout";
    
    
    private final static String ASYNCH_HANDLER_CLASS =
        "com.sun.enterprise.web.connector.grizzly.asyncHandlerClass";   
    
    private final static String ASYNCH_HANDLER_PORT =
        "com.sun.enterprise.web.connector.grizzly.asyncHandler.ports";   
    
    private final static String SNOOP_LOGGING = 
        "com.sun.enterprise.web.connector.grizzly.enableSnoop";

    private final static String TEMPORARY_SELECTOR_TIMEOUT = 
        "com.sun.enterprise.web.connector.grizzly.readTimeout"; 
    
    private final static String WRITE_TIMEOUT = 
        "com.sun.enterprise.web.connector.grizzly.writeTimeout";     

    private final static String NOTIFICATION_HANDLER = 
        "com.sun.grizzly.comet.notificationHandlerClassName";   
    
    private final static String BUFFER_RESPONSE = 
        "com.sun.grizzly.http.bufferResponse"; 
    
    private final static String OOBInline = 
        "com.sun.enterprise.web.connector.grizzly.OOBInline"; 
    // --------------------------------------------------------- Static -----//


   /**
     * Read systems properties and configure the <code>SelectorThread</code>.
     */
    protected static void configureProperties(SelectorThread selectorThread){
        if (System.getProperty(SELECTOR_TIMEOUT) != null){
            try{
                selectorThread.selectorTimeout = 
                      Integer.parseInt(System.getProperty(SELECTOR_TIMEOUT));
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, "selectorThread.invalidSelectorTimeout");
            }
        }

        if (System.getProperty(TEMPORARY_SELECTOR_TIMEOUT) != null){
            try{
                int timeout =  Integer.parseInt(
                        System.getProperty(TEMPORARY_SELECTOR_TIMEOUT));
                ByteBufferInputStream.setDefaultReadTimeout(timeout);
                SSLUtils.setReadTimeout(timeout);
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, 
                                            "selectorThread.invalidReadTimeout");
            }
        }             

        if (System.getProperty(WRITE_TIMEOUT) != null){
            try{
                int timeout =  Integer.parseInt(
                        System.getProperty(WRITE_TIMEOUT));
                OutputWriter.setDefaultWriteTimeout(timeout);
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, 
                                            "selectorThread.invalidWriteTimeout");
            }
        } 
        
        if (System.getProperty(MIN_THREAD) != null){
            try{
                selectorThread.minWorkerThreads = 
                    Integer.parseInt(System.getProperty(MIN_THREAD));
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, "selectorThread.invalidMinThreads");
            }
        }    
        
        
        if (System.getProperty(MAX_THREAD) != null){
            try{
                selectorThread.maxProcessorWorkerThreads = 
                    Integer.parseInt(System.getProperty(MAX_THREAD));
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, "selectorThread.invalidMaxThreads");
            }
        }  
        
        if (System.getProperty(DISPLAY_CONFIGURATION)!= null){
            selectorThread.displayConfiguration = 
                Boolean.valueOf(System.getProperty(DISPLAY_CONFIGURATION))
                                                                .booleanValue();
        }
        
        if (System.getProperty(OOBInline)!= null){
            selectorThread.oOBInline = 
                Boolean.valueOf(System.getProperty(OOBInline)).booleanValue();
        }
        
        if (System.getProperty(ENABLE_COMET_SUPPORT) != null){
            selectorThread.enableCometSupport(
                Boolean.valueOf(
                    System.getProperty(ENABLE_COMET_SUPPORT)).booleanValue());
        }       
        
        if (System.getProperty(ASYNCH_HANDLER_PORT) != null){
            String ports = System.getProperty(ASYNCH_HANDLER_PORT);
            StringTokenizer st = new StringTokenizer(ports,",");
            while(st.hasMoreTokens()){
                
                if ( st.nextToken()
                        .equals(String.valueOf(selectorThread.getPort()))
                        && System.getProperty(ASYNCH_HANDLER_CLASS)!= null){
                    
                    selectorThread.asyncHandler = (AsyncHandler)
                        loadClassAndInstanciate(
                            System.getProperty(ASYNCH_HANDLER_CLASS)); 
                    selectorThread.asyncExecution = true;
                }
            }
        }           
        
        if (System.getProperty(DIRECT_BYTE_BUFFER_READ)!= null){
            selectorThread.useDirectByteBuffer = 
                Boolean.valueOf(
                    System.getProperty(DIRECT_BYTE_BUFFER_READ)).booleanValue();
        }        
       
        if (System.getProperty(MAX_KEEP_ALIVE_REQUEST) != null){
            try{
                selectorThread.maxKeepAliveRequests = 
                  Integer.parseInt(System.getProperty(MAX_KEEP_ALIVE_REQUEST));
            } catch (NumberFormatException ex){
                ;
            }
        }
        
        if (System.getProperty(PIPELINE_CLASS)!= null){
            selectorThread.pipelineClassName = 
                                            System.getProperty(PIPELINE_CLASS);
        }    
        
        if (System.getProperty(NOTIFICATION_HANDLER)!= null){
            CometEngine.setNotificationHandlerClassName( 
                System.getProperty(NOTIFICATION_HANDLER));
        }    
                
        if (System.getProperty(ALGORITHM_CLASS_NAME)!= null){
            selectorThread.algorithmClassName = 
                                      System.getProperty(ALGORITHM_CLASS_NAME);
        }   

        if (System.getProperty(BYTE_BUFFER_VIEW)!= null){
            selectorThread.useByteBufferView = 
                Boolean.valueOf(
                            System.getProperty(BYTE_BUFFER_VIEW)).booleanValue();
        }    
             
        if (System.getProperty(MAX_SELECTOR_READ_THREAD) != null){
            try{
                selectorThread.multiSelectorsCount = 
                  Integer.parseInt(System.getProperty(MAX_SELECTOR_READ_THREAD));
            } catch (NumberFormatException ex){
                ;
            }
        }
        
        if (System.getProperty(MAX_SELECTOR) != null){
            try{
                SelectorFactory.maxSelectors = 
                  Integer.parseInt(System.getProperty(MAX_SELECTOR));
            } catch (NumberFormatException ex){
                ;
            }
        }

        if (System.getProperty(FACTORY_TIMEOUT) != null){
            try{
                SelectorFactory.timeout = 
                  Integer.parseInt(System.getProperty(FACTORY_TIMEOUT));
            } catch (NumberFormatException ex){
                ;
            }
        }     
        
        if (System.getProperty(SNOOP_LOGGING)!= null){
            selectorThread.setEnableNioLogging( 
                Boolean.valueOf(
                            System.getProperty(SNOOP_LOGGING)).booleanValue());
        } 
               
        if (System.getProperty(BUFFER_RESPONSE)!= null){
            selectorThread.setBufferResponse( 
                Boolean.valueOf(
                            System.getProperty(BUFFER_RESPONSE)).booleanValue());
        }  
    }

    
    /**
     * Configure properties on <code>SelectorThread</code>
     */
    public static void configure(SelectorThread selectorThread){
        configureProperties(selectorThread);
    }

    
    private static Object loadClassAndInstanciate(String className){
         try{
            Class clazz = Class.forName(className);
            return clazz.newInstance();
        } catch (Throwable ex){
            SelectorThread.logger().log(Level.SEVERE,ex.getMessage()
                + ":" + className, ex);          
        }
        return null;   
        
    }

}
