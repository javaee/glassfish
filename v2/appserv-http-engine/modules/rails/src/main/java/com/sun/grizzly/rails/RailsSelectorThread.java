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
package com.sun.grizzly.rails;

import com.sun.enterprise.web.connector.grizzly.AsyncExecutor;
import com.sun.enterprise.web.connector.grizzly.AsyncFilter;
import com.sun.enterprise.web.connector.grizzly.async.DefaultAsyncHandler;
import com.sun.enterprise.web.connector.grizzly.standalone.StaticStreamAlgorithm;
import java.io.IOException;
import java.util.logging.Level;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;

/**
 * JRuby on rails implementation of Grizzly SelectorThread
 * @author TAKAI Naoto
 * @author Jeanfrancois Arcand
 */
public class RailsSelectorThread extends SelectorThread {
    
    private final static String NUMBER_OF_RUNTIME = 
            "com.sun.grizzly.rails.numberOfRuntime";
    
    
    /**
     * Is Grizzly embedded in GlassFish.
     */
    protected static boolean embeddedInGlassFish = false;

    // Are we running embedded or not.
    static {
        try {
            embeddedInGlassFish = (Class.forName("org.apache.coyote.tomcat5.Constants") != null);
        } catch (Exception ex) {
            ; // Swallow
        }
    }

    private String jrubyLib = null;

    private int numberOfRuntime = 5;

    private RubyObjectPool pool = null;

    private String railsRoot = null;

    public String getRailsRoot() {
        return railsRoot;
    }

    @Override
    public void initEndpoint() throws IOException, InstantiationException {
        if (embeddedInGlassFish) {
            railsRoot = System.getProperty("com.sun.aas.instanceRoot") 
                + "/applications/rails";
        }
        setupSystemProperties();
        initializeRubyRuntime();        
        
        setBufferResponse(false);
        
        asyncExecution = true;
        DefaultAsyncHandler asyncHandler = new DefaultAsyncHandler();
        setAsyncHandler(asyncHandler);
        
        RailAsyncFilter railAsyncFilter = new RailAsyncFilter();
        railAsyncFilter.setRubyRuntimeQueue(pool.getRubyRuntimeQueue());
        asyncHandler.addAsyncFilter(railAsyncFilter);
        
        adapter = new RailsAdapter(pool,railAsyncFilter);
        
        algorithmClassName = StaticStreamAlgorithm.class.getName();
        super.initEndpoint();  
    }

    public void setNumberOfRuntime(int numberOfRuntime) {
        this.numberOfRuntime = numberOfRuntime;
    }

    public void setRailsRoot(String railsRoot) {
        this.railsRoot = railsRoot;
    }

    @Override
    public synchronized void stopEndpoint() {
        pool.stop();

        super.stopEndpoint();
    }

    protected void initializeRubyRuntime() {
        pool = new RubyObjectPool();
        pool.setNumberOfRuntime(numberOfRuntime);
        pool.setJrubyLib(jrubyLib);
        pool.setRailsRoot(railsRoot);

        try {
            pool.start();
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage());
        }
    }

    protected void setupSystemProperties() {
        String jrubyBase = System.getProperty("jruby.base");
        String jrubyHome = System.getProperty("jruby.home");
        String jrubyShell = System.getProperty("jruby.shell");
        String jrubyScript = System.getProperty("jruby.script");
        
        if (System.getProperty(NUMBER_OF_RUNTIME) != null){
            try{
                numberOfRuntime = Integer.parseInt(
                        System.getProperty(NUMBER_OF_RUNTIME));
            } catch (NumberFormatException ex){
                SelectorThread.logger().log(Level.WARNING, 
                                            "Invalid number of Runtime");
            }
        }        

        if (jrubyBase == null) {
            throw new IllegalStateException("Set system property jruby.base to JRuby directory");
        }
        if (jrubyHome == null) {
            System.setProperty("jruby.home", jrubyBase);
        }
        if (jrubyShell == null) {
            System.setProperty("jruby.shell", "");
        }
        if (jrubyScript == null) {
            System.setProperty("jruby.script", "");
        }

        jrubyLib = System.getProperty("jruby.lib");
        if (jrubyLib == null) {
            jrubyLib = jrubyBase + "/lib";
            System.setProperty("jruby.lib", jrubyLib);
        }
    }
}
