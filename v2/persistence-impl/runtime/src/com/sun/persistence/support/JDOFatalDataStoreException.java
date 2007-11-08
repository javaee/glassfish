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
 * JDOFatalDataStoreException.java
 *
 * Created on March 8, 2000, 8:36 AM
 */

package com.sun.persistence.support;

/** This class represents data store exceptions that cannot be retried.
 *
 * @author  Craig Russell
 * @version 1.0.1
 */
public class JDOFatalDataStoreException extends JDOFatalException {

  /**
   * Constructs a new <code>JDOFatalDataStoreException</code> without a detail message.
   */
  public JDOFatalDataStoreException() {
  }
  

  /**
   * Constructs a new <code>JDOFatalDataStoreException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public JDOFatalDataStoreException(String msg) {
    super(msg);
  }

  /** Constructs a new <code>JDOFatalDataStoreException</code> with the specified detail message
   * and failed object.
   * @param msg the detail message.
   * @param failed the failed object.
   */
  public JDOFatalDataStoreException(String msg, Object failed) {
    super(msg, failed);
  }
  
  /**
   * Constructs a new <code>JDOFatalDataStoreException</code> with the specified
   * detail message and nested <code>Throwable</code>s.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable[]</code>.
   */
  public JDOFatalDataStoreException(String msg, Throwable[] nested) {
    super(msg, nested);
  }

  /**
   * Constructs a new <code>JDOFatalDataStoreException</code> with the specified
   * detail message and nested <code>Throwable</code>s.
   * @param msg the detail message.
   * @param nested the nested <code>Throwable</code>.
   */
  public JDOFatalDataStoreException(String msg, Throwable nested) {
    super(msg, nested);
  }
}

