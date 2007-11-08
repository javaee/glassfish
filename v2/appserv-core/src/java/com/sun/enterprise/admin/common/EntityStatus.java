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

package com.sun.enterprise.admin.common;

/**
	A class representing the <code> status </code> of an entity.
	@author  Kedar Mhaswade
	@version  1.0
*/

public class EntityStatus extends Status
{
    /* javac 1.3 generated serialVersionUID */
    public static final long serialVersionUID	=  120287380119583589L;

    public static final String kEnabledStr  = "enabled";
    public static final String kDisabledStr = "disabled";

    /**
        Creates new EntityStatus that represents an <code>ENABLED </code> entity.
    */
    public EntityStatus()
    {
        super(Status.kEntityEnabledCode, kEnabledStr);
    }

    /**
     */
    public EntityStatus(int code, String str)
    {
        super(code, str);
    }

    public void setDisabled()
    {
        if (!isDisabled())
        {
            mStatusCode     = Status.kEntityDisabledCode;
            mStatusString   = kDisabledStr;
        }
    }

    /**
     * 
     */
    public void setEnabled()
    {
        if (!isEnabled())
        {
            mStatusCode     = Status.kEntityEnabledCode;
            mStatusString   = kEnabledStr;
        }
    }

    /**
            Returns whether the entity is enabled.

            @return true if the entity is enabled, false otherwise
    */
    public boolean isEnabled ()
    {
        return (mStatusCode == Status.kEntityEnabledCode);
    }

    /**
            Returns whether the entity is disabled.

            @return true if the entity is disabled, false otherwise
    */
    public boolean isDisabled ()
    {
        return (mStatusCode == Status.kEntityDisabledCode);
    }
}