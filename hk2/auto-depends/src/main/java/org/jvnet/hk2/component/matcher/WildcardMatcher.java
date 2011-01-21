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
