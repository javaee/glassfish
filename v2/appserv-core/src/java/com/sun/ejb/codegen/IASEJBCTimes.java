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
 * IASEJBCTimes.java
 *
 * Created on June 23, 2002, 10:05 PM
 * 
 * @author  bnevins
 * @version $Revision: 1.3 $
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/codegen/IASEJBCTimes.java,v $
 *
 */

package com.sun.ejb.codegen;
 
/** 
 * This class gathers timing information for the sub-tasks of IASEJBC
 */

public class IASEJBCTimes 
{
	public IASEJBCTimes()
	{
	}

	///////////////////////////////////////////////////////////////////////////

	public long getTotalTime()
	{
		return totalTime;
	}

	///////////////////////////////////////////////////////////////////////////

	public String toString() 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Total Time for EJBC: " + totalTime + " msec, ");
		sb.append(makeEntry("CMP Generation",	cmpGeneratorTime)); 
		sb.append(makeEntry("Java Compilation", javaCompileTime)); 
		sb.append(makeEntry("RMI Compilation",	RMICompileTime)); 
                sb.append(makeEntry("JAX-RPC Generation", jaxrpcGenerationTime));
		sb.append("\n");
		return sb.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private String makeEntry(String title, long time)
	{
		return title + ": " + time + " msec (" + percent(time) + "), ";
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private String percent(long time)
	{
		if(totalTime <= 0)
			return "0%";
		
		return "" + ((time * 100) / totalTime) + "%";
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	long cmpGeneratorTime	= 0;
	long javaCompileTime	= 0;
	long RMICompileTime		= 0;
        long jaxrpcGenerationTime  = 0;
	long totalTime			= 0;
        
}
