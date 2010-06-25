package org.jvnet.hk2.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jvnet.hk2.component.matcher.Constants;
import org.jvnet.hk2.component.matcher.SimpleLdapMatcher;

/**
 * Builder for constructing InhabitantTrackerContext types.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class InhabitantTrackerContextBuilder {

  private InhabitantFilter filter;
  private Boolean presence;
  private Set<String> classNames;

  
  protected InhabitantTrackerContextBuilder() {
  }

  /**
   * Creates a builder appropriate for the given habitat
   * 
   * @param h the habitat
   * 
   * @return the factory
   */
  public static InhabitantTrackerContextBuilder create(Habitat h) {
    // the habitat is not needed to distinguish the implementation at present
    return new InhabitantTrackerContextBuilder();
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
        "(" + build() + ")";
  }
  
  public InhabitantTrackerContextBuilder presence(Boolean presence) {
    this.presence = presence;
    return this;
  }
  
  public Boolean getPresence() {
    return presence;
  }

  public InhabitantTrackerContextBuilder classNames(String... classNames) {
    this.classNames = new HashSet<String>(Arrays.asList(classNames));
    return this;
  }
  
  public InhabitantTrackerContextBuilder classNames(Set<String> classNames) {
    this.classNames = new HashSet<String>(classNames);
    return this;
  }

  public Set<String> getClassNames() {
    return Collections.unmodifiableSet(classNames);
  }

  public InhabitantTrackerContextBuilder filter(InhabitantFilter filter) {
    this.filter = filter;
    return this;
  }

  public InhabitantFilter getFilter() {
    return filter;
  }
  
  public InhabitantTrackerContextBuilder ldapFilter(String ldapExpression) 
        throws ComponentException {
    SimpleLdapMatcher matcher = SimpleLdapMatcher.create(ldapExpression);
    Set<String> classNames = matcher.getTheAndSetFor(Constants.OBJECTCLASS, true);
    if (classNames.isEmpty()) throw new IllegalArgumentException("invalid expression");
    for (String item : classNames) {
      if (hasWildcard(item)) {
        throw new ComponentException("no wilcards permitted");
      }
    }
    return filter(new AlteredLdapMatcherFilter(matcher)).classNames(classNames);
  }

  protected boolean hasWildcard(String item) {
    return (item.indexOf('*') >= 0);
  }

  public InhabitantTrackerContext build() {
    if (null == classNames || classNames.isEmpty()) throw new IllegalStateException();
    return new InhabitantTrackerContextImpl(filter, presence, classNames);
  }
  
  
  protected static class AlteredLdapMatcherFilter implements InhabitantFilter {

    private final SimpleLdapMatcher matcher;
    
    protected AlteredLdapMatcherFilter(SimpleLdapMatcher matcher) {
      this.matcher = matcher;
    }
    
    @Override
    public boolean matches(Inhabitant<?> i) {
      MultiMap<String, String> map = i.metadata();
      if (null == map || map.size() == 0) {
        return true;
      }
      return matcher.matches(map);
    }
    
    @Override
    public String toString() {
      return matcher.toString();
    }
  }

}
