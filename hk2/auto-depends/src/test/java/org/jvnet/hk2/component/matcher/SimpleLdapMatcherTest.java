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
