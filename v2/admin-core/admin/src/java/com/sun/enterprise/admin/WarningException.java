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
 *   $Id: WarningException.java,v 1.3 2005/12/25 03:47:29 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: WarningException.java,v $
 *   Revision 1.3  2005/12/25 03:47:29  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:40  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.2  2004/11/14 07:04:16  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.1  2004/10/14 01:16:03  kravtch
 *   New com.sun.enterprise.admin.WarningException.java is created for delivering messages without changing CLI command state to "command failed".
 *   CLI should add code to print its message and ignore Exception.
 *   ClustersConfigBean an admin-ee and localStrings are modified to support "not all servers started" case with throwing WarniungException.
 *   Reviewer: Sreeni
 *   Tests: QLT-EE
 *   Bug #6175992
 *
 *
 *   WarningException - is runtime exception for providing warning messages for successfully executed commands
 *   (CLI/GUI framework should treat such exception as normal command execution)
 *
*/

package com.sun.enterprise.admin;

public class WarningException extends RuntimeException
{
    /**
        Creates new <code>WarningException</code> without detail message.
    */
    
    public WarningException()
    {
        super();
    }


    /**
        Constructs an <code>WarningException</code> with the specified detail message.
        @param msg the detail message.
    */
    public WarningException(String msg)
    {
        super(msg);
    }
}
