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
public class PathRule extends ThreadRatioRule{
    
    /***
     * Get the context-root from the <code>ByteBuffer</code>
     */
    protected String getContextRoot(){
        // (1) Get the token the Algorithm has processed for us.
        ByteBuffer byteBuffer = readTask.getByteBuffer();
        byte[] chars = new byte[byteBuffer.limit() - byteBuffer.position()];
               
        byteBuffer.get(chars);
        
        String token = new String(chars);

        // Remove query string.
        int index = token.indexOf(QUERY_STRING);
        if ( index != -1){
            token = token.substring(0,index);
        }
        
        boolean slash = token.endsWith(PATH_STRING);
        if ( slash ){
            token = token.substring(0,token.length() -1);
        }  
        return token;
    }
}
