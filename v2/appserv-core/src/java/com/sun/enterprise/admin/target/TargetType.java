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
 * $Id: TargetType.java,v 1.3 2005/12/25 04:14:39 tcfujii Exp $
 */

package com.sun.enterprise.admin.target;

public final class TargetType
{
    /**
     * A named configuration
     */
    public static final TargetType CONFIG = new TargetType(1, "configuration");
    /**
     * A named server of any type: not including the domain admin server, a clustered
     * server instance, an unclustered server instance
     */
    public static final TargetType SERVER = new TargetType(2, "server instance");
    /**
     * The domain
     */
    public static final TargetType DOMAIN = new TargetType(3, "domain");
    /**
     * A named cluster
     */
    public static final TargetType CLUSTER = new TargetType(4, "cluster");
    /**
     * A named node agent
     */
    public static final TargetType NODE_AGENT = new TargetType(5, "node agent");             
    
    /**
     * A stand alone server instance
     */
    public static final TargetType STANDALONE_SERVER = new TargetType(6, "standalone server instance");   
    
    /**
     * An unclustered server instance
     */
    public static final TargetType UNCLUSTERED_SERVER = new TargetType(7, "unclustered server instance");   
    
    /**
     * A stand alone cluster
     */
    public static final TargetType STANDALONE_CLUSTER = new TargetType(8, "standalone cluster");  
    
    /**
     * The domain administration server
     */
    public static final TargetType DAS = new TargetType(9, "domain administration server");  

    private int _type;
    //Note: it does not seem as if the name needs to be localizable, hence we are
    //not using the string manager here.
    private String _name;
    
    private TargetType(int type, String name)
    {
        _type = type;
        _name = name;
    }
    
    public String getName() {
        return _name;
    }    
    
    int getType() {
        return _type;
    }    
    
    public boolean equals(Object obj)
    {
        if (obj instanceof TargetType) {
            return ((TargetType)obj).getType() == this.getType() ? true : false;
        }
        return false;
    }
}
