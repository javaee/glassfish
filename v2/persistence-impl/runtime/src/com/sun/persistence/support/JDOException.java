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
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * JDOException.java
 *
 * Created on March 8, 2000, 8:29 AM
 */

package com.sun.persistence.support;

import com.sun.persistence.support.spi.I18NHelper;

/** This is the root of all JDO Exceptions.  It contains an optional detail
 * message, an optional nested <code>Throwable</code> array and an optional failed object.
 * @author Craig Russell
 * @version 1.0.2
 */
public class JDOException extends java.lang.RuntimeException {
  
  /** This exception was generated because of an exception in the runtime library.
   * @serial the nested <code>Throwable</code> array
   */
  Throwable[] nested;
  
  /** This exception may be the result of incorrect parameters supplied
   * to an API.  This is the object from which the user can determine
   * the cause of the problem.
   * @serial the failed <code>Object</code>
   */
  Object failed;

    /** The Internationalization message helper.
     */
    private static I18NHelper msg = I18NHelper.getInstance ("com.sun.persistence.support.Bundle"); //NOI18N

    /** Flag indicating whether printStackTrace is being executed.
     */
    private boolean inPrintStackTrace = false;
    
  /**
   * Constructs a new <code>JDOException</code> without a detail message.
   */
  public JDOException() {
  }
  

  /**
   * Constructs a new <code>JDOException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public JDOException(String msg) {
    super(msg);
  }

  /** Constructs a new <code>JDOException</code> with the specified detail message
   * and nested <code>Throwable</code>s.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable[]</code>.
   */
  public JDOException(String msg, Throwable[] nested) {
    super(msg);
    this.nested = nested;
  }
  
  /** Constructs a new <code>JDOException</code> with the specified detail message
   * and nested <code>Throwable</code>.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable</code>.
   */
  public JDOException(String msg, Throwable nested) {
    super(msg);
    this.nested = new Throwable[] {nested};
  }
  
  /** Constructs a new <code>JDOException</code> with the specified detail message
   * and failed object.
   * @param msg the detail message.
   * @param failed the failed object.
   */
  public JDOException(String msg, Object failed) {
    super(msg);
    this.failed = failed;
  }
  
  /** Constructs a new <code>JDOException</code> with the specified detail message,
   * nested <code>Throwable</code>s, and failed object.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable[]</code>.
   * @param failed the failed object.
   */
  public JDOException(String msg, Throwable[] nested, Object failed) {
    super(msg);
    this.nested = nested;
    this.failed = failed;
  }
  
  /** Constructs a new <code>JDOException</code> with the specified detail message,
   * nested <code>Throwable</code>, and failed object.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable</code>.
   * @param failed the failed object.
   */
  public JDOException(String msg, Throwable nested, Object failed) {
    super(msg);
    this.nested = new Throwable[] {nested};
    this.failed = failed;
  }
  
  /** The exception may include a failed object.
   * @return the failed object.
   */
  public Object getFailedObject() {
    return failed;
  }
  
  /** The exception may have been caused by multiple exceptions in the runtime.
   * If multiple objects caused the problem, each failed object will have
   * its own <code>Exception</code>.
   * @return the nested Throwable array.
   */
  public Throwable[] getNestedExceptions() {
    return nested;
  }
  
  /** Often there is only one nested exception, and this method returns it.
   * If there are more than one, then this method returns the first nested
   * exception. If there is no nested exception, then null is returned.
   * @return the first or only nested Throwable.
   * @since 1.0.1
   */
  public synchronized Throwable getCause() {
      // super.printStackTrace calls getCause to handle the cause. 
      // Returning null prevents the superclass from handling the cause;
      // instead the local implementation of printStackTrace should
      // handle the cause. Otherwise, the cause is printed twice.
      if (nested == null || nested.length == 0 || inPrintStackTrace) {
          return null;
      } else {
          return nested[0];
      }
  }
  
  /** JDK 1.4 includes a new chaining mechanism for Throwable, but since
   * JDO has its own "legacy" chaining mechanism, the "standard" mechanism
   * cannot be used. This method always throws a JDOFatalInternalException.
   * @param cause ignored.
   * @return never.
   */
  public Throwable initCause(Throwable cause) {
      throw new JDOFatalInternalException(msg.msg("ERR_CannotInitCause"));
  }
  
  /** The <code>String</code> representation includes the name of the class,
   * the descriptive comment (if any),
   * the <code>String</code> representation of the failed <code>Object</code> (if any),
   * and the <code>String</code> representation of the nested <code>Throwable</code>s (if any).
   * @return the <code>String</code>.
   */
  public synchronized String toString() {
    int len = nested==null?0:nested.length;
    // calculate approximate size of the String to return
    StringBuffer sb = new StringBuffer (10 + 100 * len);
    sb.append (super.toString());
    // include failed object information
    if (failed != null) {
        sb.append ("\n").append (msg.msg ("MSG_FailedObject"));
      String failedToString = null;
      try {
          failedToString = failed.toString();
      } catch (Exception ex) {
          // include the information from the exception thrown by failed.toString
          Object objectId = JDOHelper.getObjectId(failed);
          if (objectId == null) {
              failedToString = msg.msg("MSG_ExceptionGettingFailedToString", //NOI18N
                                       exceptionToString(ex));
          }
          else {
              // include the ObjectId information
              String objectIdToString = null;
              try {
                  objectIdToString = objectId.toString();
              }
              catch (Exception ex2) {
                  objectIdToString = exceptionToString(ex2);
              }
              failedToString = msg.msg("MSG_ExceptionGettingFailedToStringObjectId", //NOI18N
                                       exceptionToString(ex), objectIdToString);
          }
      }
      sb.append (failedToString);
    }
    // include nested Throwable information, but only if not called by
    // printStackTrace; the stacktrace will include the cause anyway.
    if (len > 0 && !inPrintStackTrace) {
      sb.append ("\n").append (msg.msg ("MSG_NestedThrowables")).append ("\n");
      Throwable exception = nested[0];
      sb.append (exception==null?"null":exception.toString()); //NOI18N
      for (int i=1; i<len; ++i) {
        sb.append ("\n"); //NOI18N
        exception = nested[i];
      sb.append (exception==null?"null":exception.toString()); //NOI18N
      }
    }
    return sb.toString();
  }    
  
    /**
     * Prints this <code>JDOException</code> and its backtrace to the 
     * standard error output.
     * Print nested Throwables' stack trace as well.
     */
    public void printStackTrace() { 
        printStackTrace (System.err);
    }

    /**
     * Prints this <code>JDOException</code> and its backtrace to the 
     * specified print stream.
     * Print nested Throwables' stack trace as well.
     * @param s <code>PrintStream</code> to use for output
     */
    public synchronized void printStackTrace(java.io.PrintStream s) { 
    int len = nested==null?0:nested.length;
        synchronized (s) {
            inPrintStackTrace = true;
            super.printStackTrace(s);
            if (len > 0) {
                s.println (msg.msg ("MSG_NestedThrowablesStackTrace"));
                for (int i=0; i<len; ++i) {
                    Throwable exception = nested[i];
                    if (exception != null) {
                        exception.printStackTrace(s);
                    }
                }
            }
            inPrintStackTrace = false;
        }
    }

    /**
     * Prints this <code>JDOException</code> and its backtrace to the specified
     * print writer.
     * Print nested Throwables' stack trace as well.
     * @param s <code>PrintWriter</code> to use for output
     */
    public synchronized void printStackTrace(java.io.PrintWriter s) { 
    int len = nested==null?0:nested.length;
        synchronized (s) {
            inPrintStackTrace = true;
            super.printStackTrace(s);
            if (len > 0) {
                s.println (msg.msg ("MSG_NestedThrowablesStackTrace"));
                for (int i=0; i<len; ++i) {
                    Throwable exception = nested[i];
                    if (exception != null) {
                        exception.printStackTrace(s);
                    }
                }
            }
            inPrintStackTrace = false;
        }
    }

    /**
     * Helper method returning a short description of the exception passed
     * as an argument. The returned string has the format defined by
     * Throwable.toString. If the exception has a non-null detail message 
     * string, then it returns the name of exception class concatenated
     * with ": " concatenated with the detailed message. Otherwise it
     * returns the name of exception class.
     * @param ex the exception to be represented.
     * @return a string representation of the exception passed as an argument.
     */
    private static String exceptionToString(Exception ex)
    {
        if (ex == null) return null;
        String s = ex.getClass().getName();
        String message = ex.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}

