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
 * PEMainTest.java
 *
 * Created on October 2, 2003, 1:47 PM
 */

package com.sun.enterprise.server;

import java.util.Locale;
import java.util.StringTokenizer;
import junit.framework.*;
import junit.textui.TestRunner;
import com.sun.enterprise.server.PEMain;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * JUnit test for PEMain.java
 * @author  Rob Ruyak
 */
public class PEMainTest extends TestCase {
    
    public PEMainTest(String name) throws Exception
    {
        super(name);
    }


    public void testSetSystemLocale() {
        String [] array = {"en_US","en_US__567_89","sp_SP","fr_FR_WIN","","FR_FR_UNIX"};
        for(int i = 0;i < array.length;i++) {
            System.setProperty(SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY,array[i]);
            PEMain.setSystemLocale();
            if(array[i].equals("")) {
                //System.out.println("Blank string reached..Locale should not change!");
                Assert.assertTrue(Locale.getDefault().toString().
                        equalsIgnoreCase(array[i-1]));
            } else {
                Assert.assertTrue(Locale.getDefault().toString().
                        equalsIgnoreCase(System.getProperty(
                                SystemPropertyConstants.DEFAULT_LOCALE_PROPERTY)));
            }
            //System.out.println("Locale -> " + Locale.getDefault());
        }
    }
/*
    public void testSplitMethod() {
        String [] array = {"en_US","en_US_______567_89","sp_SP","fr_FR_WIN",""};
        for(int i = 0;i < array.length;i++) {
            String[] tokens = array[i].split("_",3);
            switch(tokens.length) {
                case 1:
                    System.out.println("Token 1: " + tokens[0]);
                    break;
                case 2:
                    System.out.println("Token 1: " + tokens[0]);
                    System.out.println("Token 2: " + tokens[1]);
                    break;
                case 3:
                    System.out.println("Token 1: " + tokens[0]);
                    System.out.println("Token 2: " + tokens[1]);
                    System.out.println("Token 3: " + tokens[2]);
                    break;
            }
        }
    }
    public void testStringTokenization() {
        String str = "sp_SP______WIN";
        try { 
            StringTokenizer t = new StringTokenizer(str,"_");
            switch(t.countTokens()) {
                case 0:
                    break;
                case 1:
                    Locale.setDefault(new Locale(t.nextToken()));
                    break;
                case 2:
                    Locale.setDefault(new Locale(t.nextToken(),t.nextToken()));
                    break;
                default:
                    String tkn1 = null,tkn2 = null,tkn3 = null;
                    tkn1 = t.nextToken();
                    tkn2 = t.nextToken();
                    int lftPos = str.indexOf(tkn2);
                    int rgtPos = lftPos + tkn2.length();
                    tkn3 = str.substring(rgtPos + 1,str.length());
                    Locale.setDefault(new Locale(tkn1,tkn2,tkn3));
                    break;   
            }
        } catch(Exception e) {
                System.out.println("Caught the exception: " + e.getMessage());
                e.printStackTrace();
        }
        Assert.assertEquals(str,Locale.getDefault().toString());
    }
  */  
    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(PEMainTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(PEMainTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
    
}
