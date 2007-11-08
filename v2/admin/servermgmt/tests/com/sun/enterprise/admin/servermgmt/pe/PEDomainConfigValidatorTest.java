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

package com.sun.enterprise.admin.servermgmt.pe;

import junit.framework.*;
import java.util.HashMap;
import java.util.Map;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import java.util.Properties;
import com.sun.enterprise.admin.servermgmt.InvalidConfigException;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class PEDomainConfigValidatorTest extends TestCase {
  public void testNumPorts(){
	assertEquals(7, dc2.getPorts().size());
  }
  
  public void testDuplicatesInDomainConfig() {
	try {
	  p.uniquePorts(dc2);
	  fail("Expectged an InvalidConfigException indicating that there were duplicate ports");
	}
	catch (InvalidConfigException e){
	  assertEquals("Duplicate ports were found: 1 -> {domain.adminPort, domain.instancePort, http.ssl.port, jms.port}, 2 -> {orb.listener.port, orb.ssl.port}", e.getMessage());
	}
  }
  
  public void testNoDuplicatesInDomainConfig() throws Exception {
	p.uniquePorts(dc1);
  }
  
  public void testNoDuplicatePorts(){
	final Map ports = new HashMap();
	ports.put("a", "1");
	assertEquals("", p.getDuplicatePorts(ports));
  }
  
  public void testGetDuplicatePorts() {
	final Map ports = new HashMap();
	ports.put("a", "1");
	ports.put("b", "1");
	ports.put("c", "2");
	ports.put("d", "2");
	ports.put("e", "3");
	assertEquals("1 -> {a, b}, 2 -> {c, d}", p.getDuplicatePorts(ports));
  }

  public PEDomainConfigValidatorTest(String name){
	super(name);
  }

  PEDomainConfigValidator p;
  DomainConfig dc1, dc2;
  
  protected void setUp() throws Exception {
	p = new PEDomainConfigValidator();
	dc1 = new DomainConfig("domainName",
						  new Integer(1),
						  "domainRoot", 
						  "adminUser",
						  "adminPassword",
						  "masterPassword",
						  new Integer(2),
						  "jmsUser",
						  "jmsPassword",
						  new Integer(3), 
						  new Integer(4),
						  new Integer(5), 
						  new Integer(6),
						  new Integer(7),
						  new Properties()){
		protected String getFilePath(String p){
		  return p;
		}
	  };
	dc2 = new DomainConfig("domainName",
						  new Integer(1),
						  "domainRoot", 
						  "adminUser",
						  "adminPassword",
						  new Integer(1),
						  "jmsUser",
						  "jmsPassword",
						  new Integer(1), 
						  new Integer(2),
						  new Integer(1), 
						  new Integer(2),
						  new Integer(3),
						  new Properties()){
		protected String getFilePath(String p){
		  return p;
		}
	  };
  }
  

  protected void tearDown() {
  }

  private void nyi(){
	fail("Not Yet Implemented");
  }

  public static void main(String args[]){
	if (args.length == 0){
	  junit.textui.TestRunner.run(PEDomainConfigValidatorTest.class);
	} else {
	  junit.textui.TestRunner.run(makeSuite(args));
	}
  }
  private static TestSuite makeSuite(String args[]){
	final TestSuite ts = new TestSuite();
	for (int i = 0; i < args.length; i++){
	  ts.addTest(new PEDomainConfigValidatorTest(args[i]));
	}
	return ts;
  }
	
}
