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
import java.util.Vector;
import junit.textui.TestRunner;
import junit.framework.TestResult;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class ValidCommandTest extends TestCase {
    public static void assertFalse(boolean v){
         assertTrue(!v);
    }

    public void testToStringNonEmpty(){
        final ValidCommand vc = new ValidCommand();
        vc.addValidOption(new ValidOption("vo", "type", 0, "default"));
        vc.addRequiredOption(new ValidOption("ro", "type", 0, "default"));
//         vc.setProperty("key", "value");
        assertEquals("null null | {vo type  default,} | ro type  default,} |  | } | null {}", vc.toString());        
    }
    
        

    public void testToString(){
        final ValidCommand vc = new ValidCommand();
        assertEquals("null null | {} | } |  | } | null {}", vc.toString());
    }
    
    public void testHasProperty(){
        final ValidCommand vc = new ValidCommand();
        vc.setProperty("key", "name");
        assertTrue(vc.hasProperty("key"));
        assertFalse(vc.hasProperty("name"));
    }
    
    public void testHasRequiredObject(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        final ValidOption ro = new ValidOption("req", "type", 0, "default");
        vc.addValidOption(vo);
        vc.addRequiredOption(ro);
        assertTrue(vc.hasRequiredOption(ro));
        assertFalse(vc.hasRequiredOption(vo));
    }
        
    public void testHasRequiredObjectByName(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        final ValidOption ro = new ValidOption("req", "type", 0, "default");
        vc.addValidOption(vo);
        vc.addRequiredOption(ro);
        vc.addRequiredOption(new ValidOption("np", "t", 0, "d"));
        assertTrue(vc.hasRequiredOption("req"));
        assertFalse(vc.hasRequiredOption("name"));
    }
    
    public void testHasValidObject(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        assertTrue(vc.hasValidOption(vo));
        assertFalse(vc.hasValidOption(new ValidOption()));
        assertFalse(vc.hasValidOption((ValidOption) null));
    }
    
    
        
    public void testHasValidObjectNullName(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption();
        vc.addValidOption(vo);
        assertFalse(vc.hasValidOption((String) null));
    }

    public void testHasValidObjectByNameNoOptions(){
        final ValidCommand vc = new ValidCommand();
        assertFalse(vc.hasValidOption("name"));
    }
    
    public void testHasValidObjectByName(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        vc.addValidOption(new ValidOption("np", "far", 0, "default"));
        assertTrue(vc.hasValidOption("name"));
        assertFalse(vc.hasValidOption("foo"));
        assertFalse(vc.hasValidOption((String) null));
    }
    
    public void testCantDeleteRequiredOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        vc.addRequiredOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        assertTrue(vc.getRequiredOptions().contains(vo));
        vc.deleteOption(vo);
        assertTrue(vc.getValidOptions().isEmpty());
        assertTrue(vc.getRequiredOptions().contains(vo));
    }

    public void testDeleteNullOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        vc.deleteOption(null);
        assertTrue(vc.getValidOptions().contains(vo));
    }
    public void testDeleteOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        vc.deleteOption(vo);
        assertTrue(vc.getValidOptions().isEmpty());
    }

    public void testAddRequiredOptionTwice(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addRequiredOption(vo);
        vc.addRequiredOption(vo);
        assertTrue(vc.getRequiredOptions().contains(vo));
        assertEquals(1, vc.getRequiredOptions().size());
        assertTrue(vc.getValidOptions().isEmpty());
    }
        
    public void testAddNullRequiredOption(){
        final ValidCommand vc = new ValidCommand();
        vc.addRequiredOption(null);
        assertEquals(0, vc.getRequiredOptions().size());
    }
    
    public void testAddRequiredOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addRequiredOption(vo);
        assertTrue(vc.getRequiredOptions().contains(vo));
        assertEquals(1, vc.getRequiredOptions().size());
        assertTrue(vc.getValidOptions().isEmpty());
    }
    
        
    public void testAddSimpleValidOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption();
        vc.addValidOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        assertEquals(1, vc.getValidOptions().size());
        assertTrue(vc.getRequiredOptions().isEmpty());
    }

    public void testAddValidOptionTwice(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        vc.addValidOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        assertEquals(1, vc.getValidOptions().size());
        assertTrue(vc.getRequiredOptions().isEmpty());
    }

    public void testAddNullValidOption(){
        final ValidCommand vc = new ValidCommand();
        vc.addValidOption(null);
        assertEquals(0, vc.getValidOptions().size());
    }
    
    public void testAddValidOptionWithNamedOption(){
        final ValidCommand vc = new ValidCommand();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        vc.addValidOption(vo);
        assertTrue(vc.getValidOptions().contains(vo));
        assertEquals(1, vc.getValidOptions().size());
        assertTrue(vc.getRequiredOptions().isEmpty());
    }
    
    public void testGetOptionsContainsOptions(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final ValidOption vo = new ValidOption();
        v.add(vo);
        vc.setValidOptions(v);
        final Vector r = new Vector();
        final ValidOption ro = new ValidOption();
        r.add(ro);
        vc.setRequiredOptions(r);
        assertTrue(vc.getOptions().contains(vo));
        assertTrue(vc.getOptions().contains(ro));
        assertEquals(2, vc.getOptions().size());
    }
    

    public void testGetOptionsSameVector(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        vc.setValidOptions(v);
        vc.setRequiredOptions(v);
        assertTrue(vc.getOptions().isEmpty());
    }
    
    public void testGetOptionsIsEmpty(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final Vector r = new Vector();
        vc.setValidOptions(v);
        vc.setRequiredOptions(r);
        assertTrue(vc.getOptions().isEmpty());
    }

    public void testGetNonExistentOption(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        v.add(vo);
        vc.setValidOptions(v);
        assertNull(vc.getOption("Name"));
    }
        
    
    public void testValidOptionsFromMany(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final ValidOption vo = new ValidOption("name", "type", 0, "default");
        v.add(vo);
        v.add(new ValidOption("foo", "far", 1, "default"));
        vc.setValidOptions(v);
        assertEquals(vo, vc.getOption("name"));
    }

    public void testGetOptionFromEmptySet(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        assertNull(vc.getOption("anything"));
    }
    
    
    public void testValidOptionsNullCase(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final ValidOption vo = new ValidOption();
        v.add(vo);
        vc.setValidOptions(v);
        assertEquals(null, vc.getOption((String)null));
    }
    
    public void testValidOptionsWithAString(){
        final ValidCommand vc = new ValidCommand();
        final Vector v = new Vector();
        final ValidOption vo = new ValidOption("opt1", "string", 1, "opt1");
        v.add(vo);
        vc.setValidOptions(v);
        assertEquals(vo, vc.getOption("opt1"));
    }

    public void testReplaceAllOptionsWithValidOption() {
        final ValidCommand vc = new ValidCommand();
        vc.addValidOption(new ValidOption("option1", "string", 1, "default"));
        vc.addValidOption(new ValidOption("option2", "boolean", 0, "true"));
        vc.addRequiredOption(new ValidOption("option3", "string", 1, "default"));
        vc.addRequiredOption(new ValidOption("option4", "string", 1, "4848"));
        ValidOption ro = new ValidOption("option2", "boolean", 0, "false");
        vc.replaceAllOptions(ro);
        Vector vtr = vc.getValidOptions();
        ValidOption vo = (ValidOption)vtr.get(1);
        assertEquals("false", vo.getDefaultValue());
    }
    

    public void testReplaceAllOptionsWithRquiredOption() {
        final ValidCommand vc = new ValidCommand();
        vc.addValidOption(new ValidOption("option1", "string", 1, "default"));
        vc.addValidOption(new ValidOption("option2", "boolean", 0, "true"));
        vc.addRequiredOption(new ValidOption("option3", "string", 1, "default"));
        vc.addRequiredOption(new ValidOption("option4", "string", 1, "4848"));
        ValidOption ro = new ValidOption("option4", "string", 0, "4949");
        vc.replaceAllOptions(ro);
        Vector vtr = vc.getRequiredOptions();
        ValidOption vo = (ValidOption)vtr.get(1);
        assertEquals("4949", vo.getDefaultValue());
    }

    
    public void testReplaceAllOptionsWithDeprecatedOption() {
        final ValidCommand vc = new ValidCommand();
        vc.addValidOption(new ValidOption("option1", "string", 1, "default"));
        vc.addValidOption(new ValidOption("option2", "boolean", 0, "true"));
        vc.addRequiredOption(new ValidOption("option3", "string", 1, "default"));
        vc.addRequiredOption(new ValidOption("option4", "string", 1, "4848"));
        vc.addDeprecatedOption(new ValidOption("option5", "string", 1, "admin"));
        vc.addDeprecatedOption(new ValidOption("option6", "string", 1, "test"));

        ValidOption ro = new ValidOption("option5", "boolean", 2, "true");
        vc.replaceAllOptions(ro);
        Vector vtr = vc.getDeprecatedOptions();
        ValidOption vo = (ValidOption)vtr.get(0);
        assertEquals("boolean", vo.getType());
        assertEquals(2, vo.isValueRequired());
        assertEquals("true", vo.getDefaultValue());
    }


    public void testMutatorsAndAccessors(){
        final ValidCommand vc = new ValidCommand();
        final String name = "name";
        vc.setName(name);
        assertEquals(name, vc.getName());
        final String nops = "nops";
        vc.setNumberOfOperands(nops);
        assertEquals(nops, vc.getNumberOfOperands());
        final Vector vo = new Vector();
        vc.setValidOptions(vo);
        assertEquals(vo, vc.getValidOptions());
        final Vector ro = new Vector();
        vc.setRequiredOptions(ro);
        assertEquals(ro, vc.getRequiredOptions());
        final String clazz = "class";
        vc.setClassName(clazz);
        assertEquals(clazz, vc.getClassName());
        final String use = "use";
        vc.setUsageText(use);
        assertEquals(use, vc.getUsageText());
        vc.setProperty("key", "value");
        assertEquals("value", vc.getProperty("key"));
    }
    
    public void testComplexConstruction(){
        final Vector vo = new Vector();
        final Vector ro = new Vector();
        final Vector dos = new Vector();
		
        final ValidCommand vc = new ValidCommand("name", "number of ops", vo, ro, dos, "usage");
        assertEquals("name", vc.getName());
        assertEquals("number of ops", vc.getNumberOfOperands());
        assertEquals(vo, vc.getValidOptions());
        assertEquals(ro, vc.getRequiredOptions());
        assertEquals("usage", vc.getUsageText());
    }
    
    public void testBasicConstruction() {
        final ValidCommand vc = new ValidCommand();
        assertNotNull(vc.getValidOptions());
        assertTrue(vc.getValidOptions().isEmpty());
        assertNotNull(vc.getRequiredOptions());
        assertTrue(vc.getRequiredOptions().isEmpty());
        assertNotNull(vc.getProperties());
        assertTrue(vc.getProperties().isEmpty());
    }

    public ValidCommandTest(String name){
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
            junit.textui.TestRunner.run(ValidCommandTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ValidCommandTest(args[i]));
        }
        return ts;
    }
}
