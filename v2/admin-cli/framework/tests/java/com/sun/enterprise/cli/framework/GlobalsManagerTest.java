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

package com.sun.enterprise.cli.framework;

import junit.framework.*;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.5 $
 */

public class GlobalsManagerTest extends TestCase {
      // For test purposes there're two properties files, one in this
      // directory (and package) and one in the directory above. The
      // one in this directory is for the framework package, the one
      // in the directory above is for the command package.
    private static final String getCommandPackageName(){
        return "com.sun.enterprise.cli";
    }
    
    public void testCommandStringLocalization() throws Exception {
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        GlobalsManager.setInstance(gm);
        GlobalsManager.setBasePackage(getCommandPackageName());
        GlobalsManager.setPropertyFile("CommandPropertyFile");
        assertEquals("command property", GlobalsManager.getString("a"));
        assertEquals("command property", GlobalsManager.getString("a", (Object []) null));
        GlobalsManager.setBasePackage((String) null);
    }
        
    public void testCommandStringLocalizationNoBundle() {
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        GlobalsManager.setInstance(gm);
        try {
            assertEquals("", GlobalsManager.getString("a"));
            fail("Expected error indicating no base package could be found");
        }
        catch (CommandException ce){
//            assertEquals("Can't find bundle for base name null.CommandPropertyFile, locale en_US", ce.getMessage());
            assertEquals("Can't find bundle for base name null.CommandPropertyFile, locale "+Locale.getDefault(), ce.getMessage());            
            assertNull(ce.getCause());
        }
    }

    public void testFrameworkStringLocalization() throws Exception {
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        GlobalsManager.setInstance(gm);
        assertEquals("framework property", GlobalsManager.getFrameworkString("a", (Object []) null));
        assertEquals("framework property", GlobalsManager.getFrameworkString("a"));
    }
    
    
        
    public void testEnvironment(){
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        final ICommandEnvironment env2 = new Env();
        gm.setGlobalsEnv(env2);
        assertEquals(env2, gm.getGlobalsEnv());
    }
    
    public void testSingletonMethods(){
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        GlobalsManager.setInstance(gm);
        assertEquals(gm, GlobalsManager.getInstance());
    }
    
    public void testGetSetRemove() {
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        gm.setOption("key", "value");
        assertEquals("value", gm.getOption("key"));
        gm.removeOption("key");
        assertNull(gm.getOption("key"));
    }
    
        
    public void testSimpleConstructionAndObservers() {
        final ICommandEnvironment env = new Env();
        final GlobalsManager gm = new GlobalsManager(env);
        assertEquals(env, gm.getGlobalsEnv());
    }

    public GlobalsManagerTest(String name){
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(GlobalsManagerTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new GlobalsManagerTest(args[i]));
        }
        return ts;
    }

    private class Env implements ICommandEnvironment 
    {
        HashMap h = new HashMap();
        
        public void setEnvironment(String name, String value){
            h.put(name, value);
        }
        
        public Object removeEnvironment( String name ){
            final Object result = h.get(name);
            h.remove(name);
            return result;
        }
        
        public HashMap getEnvironments(){
            return new HashMap(h);
        }
        
        public Object getEnvironmentValue(String key){
            return h.get(key);
        }
        
        public String toString(){
            return h.toString();
        }
        
        public int getNumEnvironments(){
            return h.size();
        }
    }
    
}
