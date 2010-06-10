package org.jvnet.hk2.component.matcher;

import org.jvnet.hk2.component.matcher.WildcardMatcher;

import junit.framework.TestCase;

/**
 * Wildcard Matcher Tests.
 * 
 * @author Jeff Trent
 */
public class WildcardMatcherTest extends TestCase {

  public void testItAll() {
    WildcardMatcher matcher = new WildcardMatcher("*");
    assertTrue(matcher.matches("this is a test"));
    assertTrue(matcher.matches("this is a test."));

    matcher = new WildcardMatcher("this*test");
    assertTrue(matcher.matches("this is a test"));
    assertFalse(matcher.matches("this is a test."));
    
    matcher = new WildcardMatcher("*test*");
    assertTrue(matcher.matches("this is a test"));
    assertTrue(matcher.matches("this is a test."));
    
    matcher = new WildcardMatcher("test*");
    assertFalse(matcher.matches("this is a test"));
    assertFalse(matcher.matches("this is a test."));
    
    matcher = new WildcardMatcher("*test");
    assertTrue(matcher.matches("this is a test"));
    assertFalse(matcher.matches("this is a test."));

    matcher = new WildcardMatcher("*test*");
    assertTrue(matcher.matches("this is a test. this is another test"));
    assertFalse(matcher.matches("this is a Test. this is another Test"));

    matcher = new WildcardMatcher("this*is*test*");
    assertTrue(matcher.matches("this is a test. this is another test"));
    assertFalse(matcher.matches("thisis a Test. thisis another Test"));
  }
}
