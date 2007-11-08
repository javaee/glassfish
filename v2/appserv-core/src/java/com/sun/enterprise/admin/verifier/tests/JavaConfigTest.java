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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.admin.verifier.tests;

/**
 * Test case to check the validity of the JavaConfig fields
 *
 * @author  Venky TV
 * @version $Revision: 1.3 $
 */


import java.io.File;

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
//import com.sun.enterprise.config.serverbeans.Mime;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigContextEvent;

import com.sun.enterprise.admin.verifier.*;
// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

public class JavaConfigTest extends ServerXmlTest implements ServerCheck {
    
     // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public JavaConfigTest() {
    }

    // check method invoked by the command line verifier
    // Does nothing right now
    public Result check(ConfigContext context) {
            Result result;
            result = super.getInitializedResult();
            return result;
    }

    // check method called from the admin GUI and iasadmin
    public Result check(ConfigContextEvent ccce) {
        Object value = ccce.getObject();
        ConfigContext context = ccce.getConfigContext();
        Result result = new Result();
        result.passed("Passed ** ");
        String beanName = ccce.getBeanName();
        if(beanName!=null) {
            String name = ccce.getName();
            result = validateAttribute(name, (String)value);
        }
        return result;
    }

    public Result validateAttribute(String name, String value) {
        Result result = new Result();
        result.passed("Passed **");

        if( name != null && name.equals( ServerTags.JAVA_HOME ) ) {

            if( value == null ) {
                result.failed( "Java Home value is null" );
                return( result );
            }

            /* Check if <java-home>/jre is a valid directory */
            String jreDir = value + File.separator + "jre";
            try {
                File f = new File( jreDir );
                if( ! f.isDirectory() ) {
                    result.failed( "Invalid Java Home: "
                            + "Could not find the jre directory under "
                            + value  );
                    return( result );
                }
            }
            catch( Exception e ) {
                result.failed( e.getMessage() );
                return( result );
            }

        }
        return( result );
    }
}
