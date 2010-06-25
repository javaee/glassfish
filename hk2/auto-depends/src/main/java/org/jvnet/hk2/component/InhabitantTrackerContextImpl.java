package org.jvnet.hk2.component;

import java.util.Collections;
import java.util.Set;

/**
 * Default implementation of InhabitantTrackerContext.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class InhabitantTrackerContextImpl implements InhabitantTrackerContext {

  protected final InhabitantFilter filter;
  protected final Boolean presence;
  protected final Set<String> classNames;
  
  public InhabitantTrackerContextImpl(InhabitantFilter filter,
      Boolean presence,
      Set<String> classNames) {
    this.filter = filter;
    this.presence = presence;
    this.classNames = Collections.unmodifiableSet(classNames);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this) +
        "(" + filter + ")";
  }
  
  @Override
  public Set<String> getClassNames() {
    return classNames;
  }

  @Override
  public InhabitantFilter getFilter() {
    return filter;
  }

  @Override
  public Boolean getPresenceFlag() {
    return presence;
  }

}
