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

package com.sun.enterprise.web.ara.rules;

import com.sun.enterprise.web.ara.IsolationRulesExecutor;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.LinkedListPipeline;
import com.sun.enterprise.web.connector.grizzly.Rule;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Based on the application context-root, configure the <code>ReadTask</code>
 * <code>Pipeline</code>. Based on the thread-ratio defined in domain.xml, an 
 * application can have privileged <code>Pipeline</code>, configured to 
 * use specific percentage of the maximum number of threads. This  
 * <code>Rule</code> instanciate two types of <code>Pipeline</code>
 *
 * <code>privilegedPipeline</code> is will be used to execute privileged
 * applications.
 *
 * <code>victimsPipeline</code> is will be used to execute others
 * application that aren't included within the privileged tokens.
 *
 * An application is marked privileged if the set of <code>Rule</code> applied
 * to the application requests is matched. 
 *
 * @author Jeanfrancois Arcand
 */
public class ThreadRatioRule implements Rule<ReadTask> {
    
    protected final static String RESERVE = "reserve";
    protected final static String CEILING = "ceiling";
    
    protected final static String ALLOCATION_MODE = 
      "com.sun.enterprise.web.ara.policyMethod";
    
    protected final static String RULE_TOKENS = 
      "com.sun.enterprise.web.ara.policyMetric";
    
    protected final static String QUERY_STRING="?";   
    protected final static String PATH_STRING="/";
    
    /**
     * The <code>ReadTask</code> attached to this <code>Rule</code>
     */
    protected ReadTask readTask;
    
    
    /**
     * The <code>Pipeline</code> configured based on the 
     * <code>threadRatio</code>. This <code>Pipeline</code> is only used
     * by privileged application.
     */
    protected final static ConcurrentHashMap<String,Pipeline> pipelines 
            = new ConcurrentHashMap<String,Pipeline>();
    
    
    /**
     * The list of privileged token used to decide if a request can be 
     * serviced by the privileged <code>Pipeline</code>.
     */
    protected final static ConcurrentHashMap<String,Double> 
            privilegedTokens = new ConcurrentHashMap<String,Double>();
    
    
    /**
     * The thread ratio used when an application isn't listed as a privileged
     * application.
     */
    protected static double leftRatio = 1;
      
    
    /**
     * The allocation mode used: celling or Reserve. With Ceiling policy, 
     * the strategy is to wait until all apps queus are showing some slack.     
     * With Reserve policiy, if 100% reservation is made by other apps, 
     * cancel the request processing.
     */
    protected static String allocationPolicy = RESERVE;
        
   
    static {
        try{
            if ( System.getProperty(RULE_TOKENS) != null){
                StringTokenizer privList = 
                        new StringTokenizer(System.getProperty(RULE_TOKENS),",");
                StringTokenizer privElement;
                String tokens;
                double countRatio = 0;
                double tokenValue;
                while (privList.hasMoreElements()){
                    privElement = new StringTokenizer(privList.nextToken()); 
                    
                    while (privElement.hasMoreElements()){
                        tokens = privElement.nextToken();
                        int index = tokens.indexOf("|");
                        tokenValue = Double.valueOf(tokens.substring(index+1));
                        privilegedTokens.put
                                (tokens.substring(0, index),tokenValue);
                        countRatio += tokenValue;
                    }
                }
                if ( countRatio > 1 ) {
                    SelectorThread.logger().log(Level.WARNING,
                     "Thread ratio too high. The total must be lower or equal to 1");
                }  else {
                    leftRatio = 1 - countRatio;
                }     
            }
        } catch (Exception ex){
             SelectorThread.logger()
                .log(Level.WARNING,"Unable to parse thread ratio", ex);
        } 
        
        if ( System.getProperty(ALLOCATION_MODE) != null){
            allocationPolicy = System.getProperty(ALLOCATION_MODE);   
            if ( !allocationPolicy.equals(RESERVE) && 
                    !allocationPolicy.equals(CEILING) ){
                SelectorThread.logger()
                    .log(Level.WARNING,"Invalid allocation policy");
                allocationPolicy = RESERVE;
            }
        }      
    }
        
    
    
    /**
     * Creates a new ThreadRationRule.
     */
    public ThreadRatioRule() {
    }

    
    /**
     * Attach a <code>ReadTask</code> to this rule.
     */
    public void attach(ReadTask o) {
        this.readTask = o;
    }


    /**
     * Return the current attachement.
     */
    public ReadTask attachement(){
        return readTask;
    }

    
    /**
     * Invoke the rule. Based on the result of the 
     * <code>ContextRootAlgorithm</code>, configure the <code>ReadTask</code>
     * <code>Pipeline</code>.
     */
    public Integer call() throws Exception{
        boolean noCache = false;
        if ( leftRatio == 0 ) {
            if ( allocationPolicy.equals(RESERVE) )
                return IsolationRulesExecutor.RULE_BLOCKED;
            else if ( allocationPolicy.equals(CEILING) ) {
                
                // If true, then we need to wait for free space. If false, then
                // we can go ahead and let the task execute with its default
                // pipeline
                if ( isPipelineInUse() )
                    return IsolationRulesExecutor.RULE_DELAY;   
                else
                    noCache = true;
            }
        } 
             
        String token = getContextRoot();
        
        // Lazy instanciation
        Pipeline pipeline = pipelines.get(token);
        if ( pipeline == null ){
            pipeline = applyRule(token);
            pipelines.put(token,pipeline);
        }
                
        readTask.setPipeline(pipeline);
        if (!noCache)
            return IsolationRulesExecutor.RULE_OK;
        else
            return IsolationRulesExecutor.RULE_OK_NOCACHE;
    }

    
    // ------------------------------------------------ Pipeline ------------//

    
    /***
     * Get the context-root from the <code>ByteBuffer</code>
     */
    protected String getContextRoot(){
        // (1) Get the token the Algorithm has processed for us.
        ByteBuffer byteBuffer = readTask.getByteBuffer();
        byte[] chars = new byte[byteBuffer.limit() - byteBuffer.position()];
               
        byteBuffer.get(chars);
        
        String token = new String(chars);
  
        int index = token.indexOf(PATH_STRING);
        if ( index != -1){
            token = token.substring(0,index);
        }
               
        // Remove query string.
        index = token.indexOf(QUERY_STRING);
        if ( index != -1){
            token = token.substring(0,index);
        }
        
        boolean slash = token.endsWith(PATH_STRING);
        if ( slash ){
            token = token.substring(0,token.length() -1);
        }  
        return token;
    }
    
    
    /**
     * Apply the thread ratio.
     */
    protected Pipeline applyRule(String token){  
        Pipeline p = readTask.getPipeline();
        int maxThreads = p.getMaxThreads();
        
        Double threadRatio = privilegedTokens.get(token);
        if (threadRatio == null) {
            threadRatio = (leftRatio == 0? 0.5:leftRatio);
        }
        
        int privilegedCount  = (threadRatio==1 ? maxThreads : 
            (int) (maxThreads * threadRatio) + 1);
               
        return newPipeline(privilegedCount,p);
    }
    
    
    /**
     * Creates a new <code>Pipeline</code>
     */
    protected Pipeline newPipeline(int threadCount,Pipeline p){
        // Run the Task on the SelectorThread
        if ( threadCount == 0){
            return null;
        }
        Pipeline pipeline = new LinkedListPipeline();
        pipeline.setMinThreads(1);
        pipeline.setMaxThreads(threadCount);
        pipeline.setName(p.getName());
        pipeline.setQueueSizeInBytes(
                readTask.getSelectorThread().getQueueSizeInBytes());
        pipeline.initPipeline();   
        pipeline.startPipeline();
        return pipeline;
    }

    
    /**
     * Check to see if the privileged pipeline are in-use right now.
     */
    protected boolean isPipelineInUse(){
        Collection<Pipeline> collection = pipelines.values();
        for (Pipeline pipeline: collection){
            if (pipeline.size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    // ---------------------------------------------------------------------//
    
    
    /**
     * Cancel execution of this rule.
     */    
    public void cancel() {
        readTask = null;
    }

    
    /**
     * Return the time in second before this rule will be executed.
     */    
    public int getExecutionTime() {
        return -1; // now
    }
    
    
    /**
     * Set the interval in seconds to wait before executing this rule.
     */
    public void setExecutionTime(int time) {
        ; 
    }
    
    
    /**
     * Set the <code>Future</code> associated with this execution of this rule.
     */    
    public void setFuture(java.util.concurrent.Future future) {
        ;
    }
}
