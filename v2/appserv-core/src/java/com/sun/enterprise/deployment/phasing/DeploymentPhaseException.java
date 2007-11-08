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

/*
 * DeploymentPhaseException.java
 *
 * Created on June 4, 2003, 3:18 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentPhaseException.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.deployment.backend.IASDeploymentException;

/**
 * Represents exceptions coming from different deployment phases
 * @author  Sandhya E
 */
public final class DeploymentPhaseException extends IASDeploymentException {
    

    /**
     * Creates a new instance of <code>DeploymentPhaseException</code> without detail message.
     */
    public DeploymentPhaseException(String phaseName, String msg, Throwable t) {
        super(msg, t);
        this.phaseName = phaseName;
    }
    
    public DeploymentPhaseException(String phaseName, String msg) {
        super(msg);
        this.phaseName = phaseName;
    }
        
    /**
     * Returns the phasename
     * @return phaseName name of the phase from where exception is happening
     */
    public String getPhaseName() {
        return phaseName;
    }
    
    /**
     * Returns the level of this exception
     * @return level exception level [FATAL/NON_FATAL]
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Sets the error level of this exception
     * @param level error level [FATAL/NON_FATAL]
     */
    public void setLevel(int level) {
        this.level = level;
    }
    
    private final String phaseName;
    private int level = FATAL;
    
    public static final int FATAL = 0;
    public static final int NON_FATAL = 1;
}
