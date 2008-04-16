/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * BytecodePreprocessor.java
 *
 * Created on May 22, 2002, 4:54 PM
 */

package com.sun.appserv;

import org.jvnet.hk2.annotations.Contract;

import java.util.Hashtable;

/** Third party tool vendors may implement this interface to provide code
 * instrumentation to the application server.
 */
@Contract
public interface BytecodePreprocessor {
    
    /** Initialize the profiler instance.  This method should be called exactly 
     * once before any calls to preprocess.
     * @param parameters Initialization parameters.
     * @return true if initialization succeeded.
     */    
    public boolean initialize(Hashtable parameters);
    
    /** This function profiler-enables the given class.  This method should not 
     * be called until the initialization method has completed.  It is thread-
     * safe.
     * @param classname The name of the class to process.  Used for efficient 
     * filtering.
     * @param classBytes Actual contents of class to process
     * @return The instrumented class bytes.
     */    
    public byte[] preprocess(String classname, byte[] classBytes);
    
}
