/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package samples.jms.soaptojms;



/**
 * This class implements an exception which can wrapped a lower-level exception.
 *
 */
public class ServiceLocatorException extends Exception {
  private Exception exception;

  /**
   * Creates a new ServiceLocatorException wrapping another exception, and with a detail message.
   * @param message the detail message.
   * @param exception the wrapped exception.
   */
  public ServiceLocatorException(String message, Exception exception) {
    super(message);
    this.exception = exception;
    return;
  }

  /**
   * Creates a ServiceLocatorException with the specified detail message.
   * @param message the detail message.
   */
  public ServiceLocatorException(String message) {
    this(message, null);
    return;
  }

  /**
   * Creates a new ServiceLocatorException wrapping another exception, and with no detail message.
   * @param exception the wrapped exception.
   */
  public ServiceLocatorException(Exception exception) {
    this(null, exception);
    return;
  }

  /**
   * Gets the wrapped exception.
   *
   * @return the wrapped exception.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Retrieves (recursively) the root cause exception.
   *
   * @return the root cause exception.
   */
  public Exception getRootCause() {
    if (exception instanceof ServiceLocatorException) {
      return ((ServiceLocatorException) exception).getRootCause();
    }
    return exception == null ? this : exception;
  }

  public String toString() {
    if (exception instanceof ServiceLocatorException) {
      return ((ServiceLocatorException) exception).toString();
    }
    return exception == null ? super.toString() : exception.toString();
  }
}
