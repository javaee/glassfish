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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.IdempotentUrlPatternReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;
import com.sun.enterprise.ee.admin.lbadmin.reader.impl.SunWebModuleReaderImpl;

import com.sun.enterprise.tools.common.dd.webapp.SunWebApp;

public class SunWebModuleReaderTest extends TestCase {
   
    public SunWebModuleReaderTest(String name) {
        super(name);        
    }       

    public void testPatterns() throws LbReaderException {                        
        // test with the hard coded, values in test-domain2.xml for quickstart 
        //  application

        String cRoot = impl.getContextRoot();
        assertTrue(cRoot.equals("/hello"));

        String eUrl = impl.getErrorUrl();
        assertTrue(eUrl.equals("/errorpage"));

        boolean b = impl.getLbEnabled();
        assertTrue(b);

        String tOut = impl.getDisableTimeoutInMinutes();
        assertTrue(tOut.equals("96"));

        IdempotentUrlPatternReader[] iUrls = impl.getIdempotentUrlPattern();

        assertTrue(iUrls.length == 2);
        System.out.println("number of urls " + iUrls.length);

        for (int i = 0; i <iUrls.length; i++) {
            System.out.println( " Url pattern is " + iUrls[i].getUrlPattern() +
                    " Number of retries " + iUrls[i].getNoOfRetries());
        }
    }

  
    protected void setUp() throws ConfigException, LbReaderException{
        
        ConfigContext ctx = ConfigFactory.createConfigContext(URL);
        Servers servers=((Domain) ctx.getRootConfigBean()).getServers();

        FileInputStream in = null;
        try {
            in = new FileInputStream( new File(URL2));
        } catch (FileNotFoundException fne) {
            fne.printStackTrace();
            System.out.println(" File could not opened " + URL2);
        }

        SunWebApp sunWebApp = null;
        try {
            sunWebApp = SunWebApp.createGraph(in);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" Sun web app bean creation failed  " );
        }

        ApplicationRef ref = 
            servers.getServerByName(SERVERNAME).getApplicationRefByRef(APPNAME);
        impl = new SunWebModuleReaderImpl(ctx, ref, sunWebApp);
    }

    private final static String URL2 = "tests/com/sun/enterprise/ee/admin/lbadmin/test-sun-web.xml";

    private final static String URL = "tests/com/sun/enterprise/ee/admin/lbadmin/test-domain2.xml";

    private final static String SERVERNAME = "lb-test-server-1";

    private final static String APPNAME = "quickstart";

    private SunWebModuleReaderImpl impl = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SunWebModuleReaderTest.class);
    }
}
