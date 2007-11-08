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

/**
 * A <code>Task</code> that wraps the execution of an asynchronous execution
 * of a <code>ProcessorTask</code>. 
 *
 * @author Jean-Francois Arcand
 */
public interface AsyncTask extends Task{
    
    public final static int PRE_EXECUTE = 0;
    public final static int INTERRUPTED = 1;
    public final static int POST_EXECUTE = 2;
    public final static int COMPLETED = 3;
    public final static int EXECUTE = 4;    
    
    /**
     * Get the <code>AsyncExecutor</code>.
     */
    public AsyncExecutor getAsyncExecutor();

    
    /**
     * Return the <code>ProcessorTask</code>.
     */
    public ProcessorTask getProcessorTask();

    
    /**
     * Return the <code>stage</code> of the current execution.
     */
    public int getStage();

    
    /**
     * Set the <code>AsyncExecutor</code> used by this <code>Task</code>
     * to delegate the execution of a <code>ProcessorTask</code>.
     */
    public void setAsyncExecutor(AsyncExecutor asyncExecutor);

    
    /**
     * Set the <code>ProcessorTask</code> that needs to be executed
     * asynchronously.
     */
    public void setProcessorTask(ProcessorTask processorTask);

}
