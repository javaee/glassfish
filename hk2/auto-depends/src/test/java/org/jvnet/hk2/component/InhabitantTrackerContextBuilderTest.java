/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.component;

import java.util.Collections;

import junit.framework.TestCase;

import org.jvnet.hk2.component.InhabitantTrackerContext;
import org.jvnet.hk2.component.InhabitantTrackerContextBuilder;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * Inhabitant Tracker Builder Tests.  
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
public class InhabitantTrackerContextBuilderTest extends TestCase {

  InhabitantTrackerContextBuilder itcf = new InhabitantTrackerContextBuilder(); 
  
  public void testCreation() throws Exception {
    InhabitantTrackerContextBuilder builder = InhabitantTrackerContextBuilder.create(null);
    assertNotSame(builder, InhabitantTrackerContextBuilder.create(null));
    builder.classNames(Collections.EMPTY_SET);
    try {
      fail("expected exception for build: " + builder.build());
    } catch (Exception e) {
      // expected
    }
  }
  
  public void testCreate_simpleLdapMatcher() throws Exception {
    // only the first expression is valid
    String[] expressions = new String[] {
        "(&(OBJECTCLASS=com.oracle.pkg.Aclass)(prop1=foo)(prop2=bar))",
        "(&(OBJECTCLASS=com.*)(prop1=foo)(prop2=bar))",
        "(|(OBJECTCLASS=com.oracle.pkg.Aclass)(prop1=foo)(prop2=bar))",
        "(&(|(OBJECTCLASS=com.oracle.pkg.Aclass)(OBJECTCLASS=com.oracle.pkg.Bclass))(prop1=foo)(prop2=bar))",
    };
    
    MultiMap<String, String> meta1 = new MultiMap<String, String>();
    meta1.add("prop1", "foo");
    meta1.add("prop2", "bar");
    meta1.add("prop3", "other");
    MultiMap<String, String> meta2 = new MultiMap<String, String>();
    meta2.add("prop1", "foo");
    meta2.add("prop3", "other");
    MultiMap<String, String> meta3 = new MultiMap<String, String>();
    meta3.add("OBJECTCLASS", "com.oracle.pkg.Aclass");

    for (int i = 0; i < expressions.length; i++) {
      String exp = expressions[i];

      try {
        InhabitantTrackerContext itc = InhabitantTrackerContextBuilder.create(null).ldapFilter(exp).build();
        if (i > 0) {
          fail("Expected invalid tracking context: " + exp);
        } else { // i == 0
          assertNotNull(itc);
          
          // check the class names
          assertNotNull(itc.getClassNames());
          assertEquals(1, itc.getClassNames().size());
          assertTrue(itc.getClassNames().contains("com.oracle.pkg.Aclass"));
          
          // check the modified filter
          assertNotNull("filter expected", itc.getFilter());
          ExistingSingletonInhabitant inhab1 = new ExistingSingletonInhabitant(Object.class, this, meta1);
          ExistingSingletonInhabitant inhab2 = new ExistingSingletonInhabitant(Object.class, this, meta2);
          ExistingSingletonInhabitant inhab3 = new ExistingSingletonInhabitant(Object.class, this, meta3);
          assertTrue(itc.getFilter().matches(inhab1));
          assertFalse(itc.getFilter().matches(inhab2));
          assertFalse(itc.getFilter().matches(inhab3));
        }
      } catch (Exception e) {
        if (0 == i) {
          throw e;
        }
      }
    }
  }
  
  /**
   * Verifies matching behavior when this inhabitant has not properties.
   */
  public void testCreate_simpleLdapMatcher_emptyMap() throws Exception {
    String expression = "(OBJECTCLASS=com.oracle.pkg.Aclass)";
    InhabitantTrackerContext itc = InhabitantTrackerContextBuilder.create(null).ldapFilter(expression).build();

    MultiMap<String, String> meta1 = new MultiMap<String, String>();
    ExistingSingletonInhabitant inhab1 = new ExistingSingletonInhabitant(Object.class, this, meta1);
    assertTrue(itc.getFilter().matches(inhab1));

    meta1 = null;
    inhab1 = new ExistingSingletonInhabitant(Object.class, this, meta1);
    assertTrue(itc.getFilter().matches(inhab1));
  }

}
