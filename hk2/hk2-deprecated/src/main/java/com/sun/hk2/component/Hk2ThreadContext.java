/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.hk2.component;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.Callable;

import org.jvnet.hk2.component.InjectionPoint;

/**
 * FOR INTERNAL USE ONLY.
 * 
 * <p/>
 * Maintains thread local information used for the inner workings of Hk2.
 * 
 * @author Jeff Trent
 */
public final class Hk2ThreadContext {
  private static final ThreadLocal<Hk2ThreadContext> tlc = new ThreadLocal<Hk2ThreadContext>();

  private AccessControlContext acc;
  private Stack<InjectionPoint> injectionPoints = new Stack<InjectionPoint>();
  
  
  /**
   * Performs a runnable action while managing the callers AccessControlContext in thread local storage
   */
  public static void captureACCandRun(Runnable runnable) {
    Hk2ThreadContext ts = tlc.get();
    if (null != ts && null != ts.acc) {
      // caller's original context already set
      runnable.run();
      return;
    }
    
    boolean created = (null == ts);
    if (created) {
      ts = new Hk2ThreadContext();
      tlc.set(ts);
    }
    
    ts.acc = AccessController.getContext();
    
    try {
      runnable.run();
    } finally {
      ts.acc = null;
      if (created) {
        tlc.set(null);
      }
    }
  }
  
  /**
   * Retrieves the active caller AccessControlContext
   */
  static AccessControlContext getCallerACC() {
    Hk2ThreadContext ts = tlc.get();
    return (null == ts) ? null : ts.acc;
  }
  
  /**
   * Performs a callable action while managing the injection resolver's injection point in thread local storage
   */
  static <V> V captureIPandRun(InjectionPoint ip, Callable<V> callable) throws Exception {
    Hk2ThreadContext ts = tlc.get();
    boolean created = (null == ts);
    if (created) {
      ts = new Hk2ThreadContext();
      tlc.set(ts);
    }
    
    ts.injectionPoints.push(ip);
    
    try {
      return callable.call();
    } finally {
      ts.injectionPoints.pop();
      if (created) {
        tlc.set(null);
      }
    }
  }
  
  /**
   * Retrieves the active InjectionPoint
   */
  static InjectionPoint getCallerIP() {
    Hk2ThreadContext ts = tlc.get();
    try {
      return (null == ts) ? null : ts.injectionPoints.peek();
    } catch (EmptyStackException e) {
      return null;
    }
  }
  
}
