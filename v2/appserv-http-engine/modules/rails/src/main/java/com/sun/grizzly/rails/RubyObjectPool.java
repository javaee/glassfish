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

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.load.LoadService;

/**
 * An object pool for ruby runtime.
 * 
 * @author TAKAI Naoto
 */
public class RubyObjectPool {

    /** How long to wait before given up. */
    private static long DEFAULT_TIMEOUT = 1000L;

    /** JRUBY_LIB directory */
    private String jrubyLib = null;

    /** The number of runtime. */
    private int numberOfRuntime = 5;

    /** Runtime queue */
    private BlockingQueue<Ruby> queue = new LinkedBlockingQueue<Ruby>();

    /** RAILS_ROOT directory */
    private String railsRoot = null;

    /**
     * Retrives ruby runtime from the object pool.
     * 
     * @return JRuby runtime.
     */
    public Ruby bollowRuntime() {
        return queue.poll();
    }

    /**
     * Returns runtime to the object pool.
     * 
     * @param runtime
     */
    public void returnRuntime(Ruby runtime) {
        queue.offer(runtime);
    }

    /**
     * Sets JRUBY_LIB directory.
     * 
     * @param jrubyLib
     *            JRUBY_LIB directory.
     */
    public void setJrubyLib(String jrubyLib) {
        this.jrubyLib = jrubyLib;
    }

    /**
     * Sets the number of pooling runtime.
     * 
     * @param numberOfRuntime
     *            the number of runtime.
     */
    public void setNumberOfRuntime(int numberOfRuntime) {
        this.numberOfRuntime = numberOfRuntime;
    }

    /**
     * Sets RAILS_ROOT directory.
     * 
     * @param railsRoot
     *            RAILS_ROOT directory.
     */
    public void setRailsRoot(String railsRoot) {
        this.railsRoot = railsRoot;
    }

    /**
     * Starts the object pool.
     */
    public void start() {
        if (jrubyLib == null || railsRoot == null) {
            throw new IllegalStateException("jrubyLib or railsRoot can not be null.");
        }
        for (int i = 0; i < numberOfRuntime; i++) {
            Ruby runtime = initializeRubyRuntime();
            loadRubyLibraries(runtime);

            queue.offer(runtime);
        }
    }

    /**
     * Shutdowns the object pool.
     */
    public void stop() {
        for (Ruby ruby : queue) {
            ruby.tearDown();
        }
        queue.clear();
    }

    protected Ruby initializeRubyRuntime() {
        return JavaEmbedUtils.initialize(new ArrayList<String>());
    }

    protected void loadRubyLibraries(Ruby runtime) {
        LoadService loadService = runtime.getLoadService();

        // load rails
        loadService.require(railsRoot + "/config/environment.rb");
    }

    /**
     * Gets JRUBY_LIB directory.
     * 
     * @return JRUBY_LIB directory.
     */
    public String getJrubyLib() {
        return jrubyLib;
    }

    /**
     * Gets the number of directory.
     * 
     * @return the number of directory;
     */
    public int getNumberOfRuntime() {
        return numberOfRuntime;
    }

    /**
     * Gets RAILS_ROOT directory.
     * 
     * @return RAILS_ROOT directory.
     */
    public String getRailsRoot() {
        return railsRoot;
    }
    
    protected BlockingQueue<Ruby> getRubyRuntimeQueue(){
        return queue;
    }
}
