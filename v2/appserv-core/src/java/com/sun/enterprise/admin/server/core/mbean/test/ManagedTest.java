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
 * ManagedTest.java
 *
 * Created on December 4, 2001, 8:10 PM
 */

package com.sun.enterprise.admin.server.core.mbean.test;

//Admin imports
import com.sun.enterprise.admin.server.core.mbean.config.AdminBase;
import com.sun.enterprise.admin.util.*;

/**
 *
 * @author  kedar
 * @version 
 */
public class ManagedTest extends AdminBase
{
	public String Color = null;
	public String Smell  = null;
	public String Taste = null;

	/** 
		Creates new ManagedTest
	*/
    
	public ManagedTest ()
	{
		Color = "BLUE";
		Smell = "PLEASANT";
		Taste = "SWEET";
    }
	
	public ManagedTest(String msg)
	{
	}
	
	public String getColor()
	{
		return Color;
	}
	public void setColor(String c)
	{
		Color = c;
	}
	public String getSmell()
	{
		return Smell;
	}
	public void setSmell(String s)
	{
		Smell = s;
	}
	public String getTaste()
	{
		return Taste;
	}
	public void setTaste(String t)
	{
		Taste = t;
	}
	public void voidVoid()
	{
		Logger.log("Method: takes void returns void");
	}
	public String stringVoid()
	{
		Logger.log("Method: takes void returns string");
		return ("stringvoid");
	}
	public void voidInt(int i)
	{
		Logger.log("Method: takes int = " + i + " and returns void");
	}
    /** Every resource MBean should override this method to execute specific
     * operations on the MBean. This method is enhanced in 8.0. It was a no-op
     * in 7.0. In 8.0, it is modified to invoke the actual method through
     * reflection.
     * @since 8.0
     * @see javax.management.MBeanServer#invoke
     * @see #getImplementingClass
     */
    protected Class getImplementingClass() {
        return ( this.getClass() );
    }
    
    /** Reflection requires the implementing object. 
     * @since 8.0
    */
    protected Object getImplementingMBean() {
        return ( this );
    }
	
}
