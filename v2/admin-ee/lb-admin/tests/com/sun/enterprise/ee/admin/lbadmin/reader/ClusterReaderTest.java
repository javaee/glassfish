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

package com.sun.enterprise.ee.admin.lbadmin.reader;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.LbConfigs;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.WebModuleReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.InstanceReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.HealthCheckerReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;
import com.sun.enterprise.ee.admin.lbadmin.reader.impl.ClusterReaderImpl;

public class ClusterReaderTest extends TestCase {
   
    public ClusterReaderTest(String name) {
        super(name);        
    }       

    public void testInstances() throws LbReaderException {                        
        InstanceReader[] readers = impl.getInstances();
        if ( readers == null ) {
            System.out.println("no instances found");
            return;
        }
        for( int i=0; i < readers.length; i++ ) {
            System.out.println(" props[" + i + "] name is " 
                + readers[i].getName() + " lb enabled is " + readers[i].getLbEnabled() + " time out  is " + readers[i].getDisableTimeoutInMinutes() + " listerner value is " + readers[i].getListeners());
        }
        assertTrue(readers.length == 2);
    }

  
    public void testHealthChecker() throws LbReaderException {                        
        HealthCheckerReader reader = impl.getHealthChecker();
        assertTrue(reader != null);
        if ( reader == null ) {
            System.out.println("no health checker found");
            return;
        }
            System.out.println(" hc url  is " 
                + reader.getUrl() + " interval " + reader.getIntervalInSeconds());
    }


    public void testWebModules() throws LbReaderException {
        WebModuleReader[] wmods = impl.getWebModules();
        if ( wmods == null ) {
            System.out.println("no web modules found");
            return;
        }
        for (int i=0; i < wmods.length; i++) {
            System.out.println("wmods[" + i + "] name is " 
                + wmods[i].getContextRoot());
        }
        assertTrue(wmods.length == 0);
    }
    
    protected void setUp() throws ConfigException, LbReaderException{
        // create Config context
        
        ConfigContext ctx = ConfigFactory.createConfigContext(URL);
        LbConfigs lbConfigs=((Domain) ctx.getRootConfigBean()).getLbConfigs();

        if (lbConfigs == null) {
             return ;
         }

        LbConfig lbConfig = lbConfigs.getLbConfigByName(LBNAME);
        ClusterRef cRef = lbConfig.getClusterRefByRef(CLNAME);

        impl = new ClusterReaderImpl(ctx,cRef);

    }

    private final static String LBNAME = "lb1";
    private final static String CLNAME = "ee-synchronization-cluster";
    private final static String URL = "tests/com/sun/enterprise/ee/admin/lbadmin/test-domain.xml";

    private ClusterReaderImpl impl = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ClusterReaderTest.class);
    }
}
