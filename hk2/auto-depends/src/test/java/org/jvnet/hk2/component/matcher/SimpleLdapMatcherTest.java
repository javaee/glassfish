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
package org.jvnet.hk2.component.matcher;

import java.util.HashMap;
import java.util.Map;

import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.Constants;
import org.jvnet.hk2.component.matcher.SimpleLdapMatcher;

import junit.framework.TestCase;

/**
 * Simple LDAP Matcher Tests. 
 * 
 * @author Jeff Trent
 */
public class SimpleLdapMatcherTest extends TestCase {
  private final static String NAME_KEY = "Name";
  private final static String PROP_KEY = "Prop";

  private final static String ALT_NAME_KEY = "namE";

  private final static String ALICE = "Alice";
  private final static String BOB = "Bob";

  public void testAndFilterMatch_ObjectClass() {
    Map<String, Object> properties = new HashMap<String, Object>();
    String objectClasses[] = new String[2];

    objectClasses[0] = SimpleLdapMatcherTest.class.getName();
    objectClasses[1] = TestCase.class.getName();

    properties.put(Constants.OBJECTCLASS, objectClasses);
    properties.put(NAME_KEY, ALICE);

    String matchingFilter1 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(" + ALT_NAME_KEY + "=" + ALICE + "))";
    String matchingFilter2 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(" + ALT_NAME_KEY + "=*))";
    String nonMatchingFilter1 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(" + ALT_NAME_KEY + "=" + BOB + "))";
    String nonMatchingFilter2 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(foo=" + BOB + "))";

    assertTrue(SimpleLdapMatcher.filterMatch(matchingFilter1, properties));
    assertTrue(SimpleLdapMatcher.filterMatch(matchingFilter2, properties));
    assertFalse(SimpleLdapMatcher.filterMatch(nonMatchingFilter1, properties));
    assertFalse(SimpleLdapMatcher.filterMatch(nonMatchingFilter2, properties));
  }

  public void testAndFilterMatch_WildCards() {
    Map<String, Object> properties = new HashMap<String, Object>();
    String objectClasses[] = new String[2];

    objectClasses[0] = SimpleLdapMatcherTest.class.getName();
    objectClasses[1] = TestCase.class.getName();

    properties.put(Constants.OBJECTCLASS, objectClasses);
    properties.put(NAME_KEY, ALICE);
    properties.put(PROP_KEY, "test");

    String nonMatchingFilter1 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(" + ALT_NAME_KEY + "=" + ALICE + ")"
        + "(" + PROP_KEY + "=test1" + "))";
    String matchingFilter2 = "(&(" + Constants.OBJECTCLASS + "="
        + SimpleLdapMatcherTest.class.getName() + ")(" + ALT_NAME_KEY + "=*)"
        + "(" + PROP_KEY + "=*test*" + "))";

    assertFalse(SimpleLdapMatcher.filterMatch(nonMatchingFilter1, properties));
    assertTrue(SimpleLdapMatcher.filterMatch(matchingFilter2, properties));
  }
  
  public void testMatches() {
    MultiMap<String, String> props = new MultiMap<String, String>();
    props.add("prop1", "alpha");
    props.add("prop1", "beta");
    props.add("prop2", "foo");
    props.add("prop2", "bar");
    props.add("prop3", "biz");
    props.add("prop3", "baz");
    
    SimpleLdapMatcher matcher = new SimpleLdapMatcher("(&(prop1=alpha)(prop2=bar)(prop3=b*))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(prop1=*a)(prop3=*z))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(prop1=*a)(prop2=*)(prop3=*z))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(prop1=*a)(prop2=blah)(prop3=*z))");
    assertFalse(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(prop1=*a)(propX=*z))");
    assertFalse(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(|(propX=*z)(prop1=*a))(prop3=baz))");
    assertTrue(matcher.matches(props));
  }
  
  /**
   * Case-insensitivity only applies to the keys, not the values
   */
  public void testCaseInsensitiveMatches() {
    MultiMap<String, String> props = new MultiMap<String, String>();
    props.add("Prop1", "alpha");
    props.add("proP1", "beta");
    props.add("Prop2", "foo");
    props.add("proP2", "bar");
    props.add("Prop3", "biz");
    props.add("proP3", "baz");
    
    SimpleLdapMatcher matcher = new SimpleLdapMatcher("(&(pRop1=alpha)(pRop2=bar)(pRop3=b*))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(pRop1=*a)(pRop3=*z))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(pRop1=*a)(pRop2=*)(pRop3=*z))");
    assertTrue(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(pRop1=*a)(pRop2=blah)(pRop3=*z))");
    assertFalse(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(pRop1=*a)(pRopX=*z))");
    assertFalse(matcher.matches(props));

    matcher = new SimpleLdapMatcher("(&(|(pRopX=*z)(pRop1=*a))(pRop3=baz))");
    assertTrue(matcher.matches(props));
  }
}
