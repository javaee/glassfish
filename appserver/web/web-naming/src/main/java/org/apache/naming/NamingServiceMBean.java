/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.naming;

/**
 * Naming MBean interface.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.2 $
 */

public interface NamingServiceMBean {
    
    
    // -------------------------------------------------------------- Constants
    
    
    public static enum State { STOPPED, STOPPING, STARTING, STARTED }
    
    
    /**
     * Component name.
     */
    public static final String NAME = "Apache JNDI Naming Service";
    
    
    /**
     * Object name.
     */
    public static final String OBJECT_NAME = ":service=Naming";
    
    
    // ------------------------------------------------------ Interface Methods
    
    
    /**
     * Retruns the JNDI component name.
     */
    public String getName();
    
    
    /**
     * Returns the state.
     */
    public State getState();
    
    
    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception;
    
    
    /**
     * Stop the servlet container.
     */
    public void stop();
    
    
    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy();
    
    
}
