/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * AutoDeployConstants.java
 *
 * Created on February 27, 2003, 12:00 AM
 */

package org.glassfish.deployment.autodeploy;

import java.util.ArrayList;
import java.util.List;

/**
 *constants detail
 *
 * @author  vikas
 */
public class AutoDeployConstants {
    
    /**
     * Starting delay between AutoDeployTask activation and actual deployment.
     */
    public static  final long STARTING_DELAY=30; //sec
    /**
     * Max tardiness between schedule and actual execution of task.
     */
    public static  final long MAX_TARDINESS= 10; //sec 
    /**
     * Max tardiness between schedule and actual execution of task.
     */
    public static  final long MIN_POOLING_INTERVAL= 2; //sec

    /**
     * Default autodeploy dir set to "autodeploy" relative to server root
     */
    public static final String DEFAULT_AUTODEPLOY_DIR = "autodeploy";

    /**
     * Default polling interval set to 2sec
     */
    public static final long DEFAULT_POLLING_INTERVAL = 2; //sec

    /**
     * Extension of file, after successful deployment
     */
    public static  final String DEPLOYED="_deployed";
    /**
     * Extension of file, if deployment fails
     */
    public static  final String NOTDEPLOYED="_notdeployed";
    
    /**
     * File type if it is being monitored due to slow growth
     */
    public static final String PENDING = "_pending";

/**
     * common deploy action
     */
    public static final String DEPLOY_METHOD       = "deploy";
    /**
     * common undeploy action
     */
    public static final String UNDEPLOY_METHOD       = "undeploy";

    public static final String DEPLOY_FAILED       = "_deployFailed";
    public static final String UNDEPLOYED       = "_undeployed";
    public static final String UNDEPLOY_FAILED       = "_undeployFailed";
    
    public static final String UNDEPLOY_REQUESTED = "_undeployRequested";
    
    public static final List<String> MARKER_FILE_SUFFIXES = initMarkerFileSuffixes();
    
    private static List<String> initMarkerFileSuffixes() {
        List<String> result = new ArrayList<String>();
        result.add(DEPLOYED);
        result.add(NOTDEPLOYED);
        result.add(PENDING);
        result.add(DEPLOY_FAILED);
        result.add(UNDEPLOYED);
        result.add(UNDEPLOY_FAILED);
        result.add(UNDEPLOY_REQUESTED);
        
        return result;
    }
}
