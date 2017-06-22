/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * JDODuplicateObjectIdException.java
 *
 * Created on May 06, 2002
 */

package com.sun.jdo.api.persistence.support;

/** JDODuplicateObjectIdException is thrown in case this PersistenceManager
 * has another instance with the same Object Id in its cache.
 *
 * @author  Marina Vatkina
 * @version 0.1
 */
public class JDODuplicateObjectIdException extends JDOUserException
{
    /**
     * Creates a new <code>JDODuplicateObjectIdException</code> without detail message.
     */
    public JDODuplicateObjectIdException() 
    {
    }

    /**
     * Constructs a new <code>JDODuplicateObjectIdException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JDODuplicateObjectIdException(String msg) 
    {
        super(msg);
    }
    
    /**
      * Constructs a new <code>JDODuplicateObjectIdException</code> with the specified detail message
      * and nested Exception.
      * @param msg the detail message.
      * @param nested the nested <code>Exception</code>.
      */
    public JDODuplicateObjectIdException(String msg, Exception nested) 
    {
        super(msg, nested);
    }

    /** Constructs a new <code>JDODuplicateObjectIdException</code> with the specified detail message
     * and failed object array.
     * @param msg the detail message.
     * @param failed the failed object array.
     */
    public JDODuplicateObjectIdException(String msg, Object[] failed) {
        super(msg, failed);
    }
 
    /** Constructs a new <code>JDODuplicateObjectIdException</code> with the specified detail message,
     * nested exception, and failed object array.
     * @param msg the detail message.
     * @param nested the nested <code>Exception</code>.
     * @param failed the failed object array.
     */
    public JDODuplicateObjectIdException(String msg, Exception nested, Object[] failed) {
        super(msg, nested, failed);
    }
}
