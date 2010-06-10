package org.jvnet.hk2.component.matcher;

/**
 * Simple wildcard matcher used as part of RFC 2254 type LDAP searches.
 *
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class WildcardMatcher {

  private final String pattern;
  private final String[] parts;

  public WildcardMatcher(String pattern) {
    this.pattern = pattern;
    this.parts = pattern.split("\\*");
  }

  public boolean matches(String text) {
    if (0 == parts.length) {
      return true;
    }
    
    if (!pattern.startsWith("*") && !text.startsWith(parts[0])) {
      return false;
    }
    
    for (String part : parts) {
      int pos = text.indexOf(part);
      if (pos == -1) {
        return false;
      }
      text = text.substring(pos + part.length());
    }

    return (text.length() == 0 || pattern.endsWith("*"));
  }

}
