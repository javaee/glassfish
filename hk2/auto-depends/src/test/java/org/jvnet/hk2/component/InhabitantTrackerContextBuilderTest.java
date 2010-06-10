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
  
  public void testCreate_simpleLdapMatcher_emptyMap() throws Exception {
    String expression = "(OBJECTCLASS=com.oracle.pkg.Aclass)";
    InhabitantTrackerContext itc = InhabitantTrackerContextBuilder.create(null).ldapFilter(expression).build();
    MultiMap<String, String> meta1 = new MultiMap<String, String>();
    ExistingSingletonInhabitant inhab1 = new ExistingSingletonInhabitant(Object.class, this, meta1);
    assertTrue(itc.getFilter().matches(inhab1));
  }
}
