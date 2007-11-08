/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


/*
  File: Barrier.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
*/

package com.sun.enterprise.ee.synchronization.util.concurrent;

/**
 * Barriers serve
 * as synchronization points for groups of threads that
 * must occasionally wait for each other. 
 * Barriers may support any of several methods that
 * accomplish this synchronization. This interface
 * merely expresses their minimal commonalities:
 * <ul>
 *   <li> Every barrier is defined for a given number
 *     of <code>parties</code> -- the number of threads
 *     that must meet at the barrier point. (In all current
 *     implementations, this
 *     value is fixed upon construction of the Barrier.)
 *   <li> A barrier can become <code>broken</code> if
 *     one or more threads leave a barrier point prematurely,
 *     generally due to interruption or timeout. Corresponding
 *     synchronization methods in barriers fail, throwing
 *     BrokenBarrierException for other threads
 *     when barriers are in broken states.
 * </ul>
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */
public interface Barrier {


  /** 
   * Return the number of parties that must meet per barrier
   * point. The number of parties is always at least 1.
   */
  public int parties();

  /**
   * Returns true if the barrier has been compromised
   * by threads leaving the barrier before a synchronization
   * point (normally due to interruption or timeout). 
   * Barrier methods in implementation classes throw
   * throw BrokenBarrierException upon detection of breakage.
   * Implementations may also support some means
   * to clear this status.
   */
  public boolean broken();
}
