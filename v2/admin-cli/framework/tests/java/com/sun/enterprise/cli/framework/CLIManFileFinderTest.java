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

import java.util.Iterator;
import java.util.Locale;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.NoSuchElementException;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.4 $
 */

public class CLIManFileFinderTest extends TestCase {
  public void testEndOfIterator() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "UK", "v1");
	  Iterator it = c.getPossibleLocations("command", l);
	  for (int i = 0; i < 71; i++){
		it.next();
	  }
	  assertEquals("help/command.9m", (String) it.next());
	  assertTrue(!it.hasNext());
	  try {
		it.next();
		fail("Expected NoSuchElementException indicating we'd read beyond the end of the interator");
	  }
	  catch (NoSuchElementException nse){
	  }
		
  }
	
  public void testEndOfThirdSearch() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "UK", "v1");
	  Iterator it = c.getPossibleLocations("command", l);
	  for (int i = 0; i < 53; i++){
		it.next();
	  }
	  assertEquals("help/en/command.9m", (String) it.next());
	  assertEquals("help/command.1", (String) it.next());
  }
  public void testEndOfSecondSearch() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "UK", "v1");
	  Iterator it = c.getPossibleLocations("command", l);
	  for (int i = 0; i < 35; i++){
		it.next();
	  }
	  assertEquals("help/en/UK/command.9m", (String) it.next());
	  assertEquals("help/en/command.1", (String) it.next());
  }
  public void testEndOfFirstSearch() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "UK", "v1");
	  Iterator it = c.getPossibleLocations("command", l);
	  for (int i = 0; i < 17; i++){
		it.next();
	  }
	  assertEquals("help/en/UK/v1/command.9m", (String) it.next());
	  assertEquals("help/en/UK/command.1", (String) it.next());
  }
  
  public void testLocations() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "UK", "v1");
	  Iterator it = c.getPossibleLocations("command", l);
	  assertEquals("help/en/UK/v1/command.1", (String) it.next());
	  assertEquals("help/en/UK/v1/command.1m",(String) it.next());
	  assertEquals("help/en/UK/v1/command.2", (String) it.next());
	  for (int i = 0; i < 5; i++){
		it.next();
	  }
	  assertEquals("help/en/UK/v1/command.5", (String) it.next());
  }
  
		
  public void testNoLanguage() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("", "UK", "v1");
	  assertEquals(1,c.getLocaleLocations(l).length);
	  assertEquals("", c.getLocaleLocations(l)[0]);
	}
	
  public void testNoCountry() {
	  CLIManFileFinder c = new CLIManFileFinder();
	  Locale l = new Locale("en", "", "v1");
	  assertEquals(2,c.getLocaleLocations(l).length);
	  assertEquals("/en", c.getLocaleLocations(l)[0]);
	  assertEquals("", c.getLocaleLocations(l)[1]);
	}

  public void testEmptyVariant(){
	CLIManFileFinder c = new CLIManFileFinder();
	Locale l = new Locale("en", "uk", "");
	assertEquals(3,c.getLocaleLocations(l).length);
	assertEquals("/en/UK", c.getLocaleLocations(l)[0]);
	assertEquals("/en", c.getLocaleLocations(l)[1]);
	  assertEquals("", c.getLocaleLocations(l)[2]);
  }
  public void testNoVariant(){
	CLIManFileFinder c = new CLIManFileFinder();
	Locale l = new Locale("en", "uk");
	assertEquals(3,c.getLocaleLocations(l).length);
	assertEquals("/en/UK", c.getLocaleLocations(l)[0]);
	assertEquals("/en", c.getLocaleLocations(l)[1]);
	  assertEquals("", c.getLocaleLocations(l)[2]);
  }
  
  public void testSimpleLocation() {
	CLIManFileFinder c = new CLIManFileFinder();
	Locale l = new Locale("en", "uk", "v1");
	assertEquals(4,c.getLocaleLocations(l).length);
	assertEquals("/en/UK/v1", c.getLocaleLocations(l)[0]);
	assertEquals("/en/UK", c.getLocaleLocations(l)[1]);
	assertEquals("/en", c.getLocaleLocations(l)[2]);
	assertEquals("", c.getLocaleLocations(l)[3]);
	
  }

  public CLIManFileFinderTest(String name){
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
	  junit.textui.TestRunner.run(CLIManFileFinderTest.class);
	} else {
	  junit.textui.TestRunner.run(makeSuite(args));
	}
  }
  private static TestSuite makeSuite(String args[]){
	final TestSuite ts = new TestSuite();
	for (int i = 0; i < args.length; i++){
	  ts.addTest(new CLIManFileFinderTest(args[i]));
	}
	return ts;
  }
}
