/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
