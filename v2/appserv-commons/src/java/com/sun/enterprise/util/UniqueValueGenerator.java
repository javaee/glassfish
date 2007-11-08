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

package com.sun.enterprise.util;

/**
 * A unique value generator is used to generate numbers
 * that are guaranteed to be unique within a particular
 * scope and lifetime of a J2EE server. See below for details.
 * Generators support concurrent access.  Each generator
 * generates unique numbers within a particular context.
 * Clients that need to draw numbers from the same "bucket"
 * must use the same context.  
 *
 * @author Kenneth Saks
 */
public interface UniqueValueGenerator {

    /**
     * Generate a number that is guaranteed to be unique
     * within the lifetime of one J2EE server instance
     * on one machine.(in both single-vm and multi-vm modes)
     * That is, between "j2ee -start" and "j2ee -stop".
     * @exception UniqueValueGeneratorException if error occurs
     */
    long nextNumber() throws UniqueValueGeneratorException;

    /**
     * Generate an id that is guaranteed to be unique across
     * multiple sessions of the same j2ee server on the same machine.
     * @exception UniqueValueGeneratorException if error occurs
     */
    String nextId() throws UniqueValueGeneratorException;

    /**
     * Get the context within which unique numbers will be generated.
     */
    String getContext() throws UniqueValueGeneratorException;

}
