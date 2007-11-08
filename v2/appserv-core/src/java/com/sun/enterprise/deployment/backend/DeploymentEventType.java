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
 * DeploymentEventType.java
 *
 * Created on June 17, 2003, 12:38 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/backend/DeploymentEventType.java,v $
 *
 */

package com.sun.enterprise.deployment.backend;


/**
 *
 * @author  Sandhya E
 */
public class DeploymentEventType {
     
    
    /** Creates a new instance of DeploymentEventType */
    private DeploymentEventType(String theName) {
        assert theName != null;
	name = theName;
    }
        
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString() {
        return name;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Event types
    public	static final int PRE_DEPLOY		= 1;
    public	static final int POST_DEPLOY		= 2;

    public	static final int PRE_UNDEPLOY	        = 3;
    public	static final int POST_UNDEPLOY	        = 4;
   
    public	static final int PRE_ASSOCIATE		= 11;
    public	static final int POST_ASSOCIATE	   = 12;
    
    public	static final int PRE_APP_START	        = 5;
    public	static final int POST_APP_START	        = 6;
    
    public	static final int PRE_APP_STOP	        = 7;
    public	static final int POST_APP_STOP	        = 8;
    
    public	static final int PRE_DISASSOCIATE	= 9;
    public	static final int POST_DISASSOCIATE	= 10;    
    
    public      static final int PRE_RA_START          = 11;
    public      static final int POST_RA_START         = 12;

    public      static final int PRE_RA_STOP           = 13;
    public      static final int POST_RA_STOP          = 14;

    public      static final int PRE_RES_CREATE          = 15;
    public      static final int POST_RES_CREATE        = 16;

    public      static final int PRE_RES_DELETE           = 17;
    public      static final int POST_RES_DELETE          = 18;

    private final			String					name;
}
