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
package com.sun.enterprise.web.ara;

import com.sun.enterprise.web.connector.grizzly.HtmlHelper;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.Rule;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.sun.enterprise.web.ara.rules.PathRule;

/**
 * This class execute sequentialy <code>Rule</code>s on a <code>Task</code> 
 *
 * @author Jeanfrancois Arcand
 */
public class IsolationRulesExecutor implements RulesExecutor<IsolatedTask> {
    
    public final static int RULE_OK = 0;
    public final static int RULE_DELAY = 1;
    public final static int RULE_BLOCKED = 2;   
    public final static int RULE_OK_NOCACHE = 3;
    public final static int RULE_CONTINUE = 4;
    
            
    private final static String RULE_CLASS = 
            "com.sun.enterprise.web.ara.rules"; 
    
    
    private final static String DELAY_VALUE = 
            "com.sun.enterprise.web.ara.delay"; 
    
    
    /**
     * Initial number of <code>Rule</code> to cache.
     */
    private final static int INITIAL_RULE_COUNT = 5;


    /**
     * The list of <code>Rule</code> to apply to a <code>Task</code>
     */   
    protected ArrayList<Rule> rules = new ArrayList<Rule>();
    
    
    /**
     * The current number of thread used.
     */
    private static int currentThreadCount;
    
    
    /**
     * The time this thread will sleep when a rule is delayed.
     */
    private static long delayValue = 5 * 1000;
    
    
    /**
     * Is caching allowed
     */
    private boolean isCachingAllowed = true;
    
    // ---------------------------------------------------------------------//
    
     
    public IsolationRulesExecutor() {
        loadRules();
        
        if ( System.getProperty(DELAY_VALUE) != null){
            delayValue = Long.valueOf(System.getProperty(DELAY_VALUE));         
        }
    }

    
    /**
     * Load the list of <code>Rules</code> to apply to a <code>Task</code>
     */
    protected void loadRules(){      
        if ( System.getProperty(RULE_CLASS) != null){
            StringTokenizer st = new StringTokenizer(
                    System.getProperty(RULE_CLASS),",");
            while (st.hasMoreTokens()){
                rules.add(loadInstance(st.nextToken()));                
            } 
        }   
        
        if ( rules.size() == 0){
            rules.add(new PathRule());
        }
    }
    
    
    /**
     * Execute the <code>Rule</code>s on the <code>IsolatedTask</code>
     * @param isolatedTask the task used.
     * @return true if the request processing can continue.
     */
    public boolean execute(IsolatedTask isolatedTask) {
        ReadTask task = (ReadTask)isolatedTask.getWrappedTask();
                       
        Integer status = 0;
        int i = 0;
        isCachingAllowed = true;
        while(true) {
            rules.get(i).attach(task);
            
            try{
                status = (Integer)rules.get(i).call();  
            } catch(Exception ex) { 
                SelectorThread.logger().log(Level.SEVERE,"Rule exception",ex);
                return true;
            }
            
            isCachingAllowed = (status == RULE_OK_NOCACHE ? false:true);
            
            if (status == RULE_DELAY){   
                
                // Wait until the delay expire. The current thread will 
                // wait and then re-execute the rules.
                try{
                    Thread.sleep(delayValue);
                } catch (InterruptedException ex) {
                    SelectorThread.logger().
                            log(Level.SEVERE,"Rule delay exception",ex);
                }
                i = 0;
                continue;
            }  else if ( status == RULE_BLOCKED ){
                task.cancelTask("No resources available.", HtmlHelper.OK);
                return true;
            } 
            
            i++;
            if (i == rules.size()){
                break;
            }
        }
        
        return (status == RULE_OK || status == RULE_OK_NOCACHE);
 
    }
    
    
    /**
     * Is caching of <code>Rule</code> results allowed
     */
    public boolean isCachingAllowed(){
        return isCachingAllowed;
    }

    
    // -------------------------------------------------------- Util --------//
    
     /**
     * Instanciate a class based on a property.
     */
    private Rule loadInstance(String property){        
        Class className = null;                               
        try{                              
            className = Class.forName(property);
            return (Rule)className.newInstance();
        } catch (ClassNotFoundException ex){
        } catch (InstantiationException ex){
        } catch (IllegalAccessException ex){
        }
        return new PathRule();
    }   
}
