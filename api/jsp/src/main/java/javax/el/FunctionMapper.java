/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */

package javax.el;

/**
 * The interface to a map between EL function names and methods.
 *
 * <p>A <code>FunctionMapper</code> maps <code>${prefix:name()}</code> 
 * style functions to a static method that can execute that function.</p>
 *
 * @since JSP 2.1
 */
public abstract class FunctionMapper {
    
  /**
   * Resolves the specified prefix and local name into a 
   * <code>java.lang.Method</code>.
   *
   * <p>Returns <code>null</code> if no function could be found that matches
   * the given prefix and local name.</p>
   * 
   * @param prefix the prefix of the function, or "" if no prefix.
   *     For example, <code>"fn"</code> in <code>${fn:method()}</code>, or
   *     <code>""</code> in <code>${method()}</code>.
   * @param localName the short name of the function. For example,
   *     <code>"method"</code> in <code>${fn:method()}</code>.
   * @return the static method to invoke, or <code>null</code> if no
   *     match was found.
   */
  public abstract java.lang.reflect.Method resolveFunction(String prefix, 
      String localName);
  
}
