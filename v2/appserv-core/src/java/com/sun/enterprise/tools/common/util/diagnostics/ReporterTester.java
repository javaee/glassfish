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

package com.sun.enterprise.tools.common.util.diagnostics;

import java.io.*;

/** General Purpose Debugging Output
 ** -- create a ri object and then write with
 ** pr()
 ** If you construct with an Object -- that Object's class name will automatically be prepended to each message
 ** If you use pr(String metName, String mesg) -- the metName will be added to the ObjectName
 ** The output of ri is controlled by an environmental variable
 ** if you call it with java -DaibDebug=true  -- it gets turned on...
 **/

class foo
{
	public String toString() { return "Hi There!!"; }//NOI18N
}

public class ReporterTester
{
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		ReporterTester rt = new ReporterTester();
		/*
		ReporterImpl ri = new ReporterImpl(2);
		ReporterImpl rj = new ReporterImpl("Second One", 0);

		ri.ignoreRegistry();
		ri.dump(rt);
		ri.dump(rt, "This is an Object Dump");
		ri.verbose("This is Verbose Comment");
		ri.info("This is an INFO comment");
		ri.warn("This is a Warning");
		ri.error("This is an Error");
		ri.critical("This is a Critical Error");
		ri.warn(new foo());
		ri.verbose(new foo());
		ri.verbose(new StackTrace());

		rj.ignoreRegistry();
		rj.dump(rt);
		rj.dump(rt, "This is an Object Dump");
		rj.verbose("This is Verbose Comment");
		rj.info("This is an INFO comment");
		rj.warn("This is a Warning");
		rj.error("This is an Error");
		rj.critical("This is a Critical Error");
		rj.warn(new foo());
		rj.verbose(new foo());
		rj.verbose(new StackTrace());
*/
		CallerInfo.addToGlobalIgnore(rt);
		new Goo();
	}
	private int junk = 5;	// so dump() has something to work with!
	protected String sss = "Foo";//NOI18N
}

class Goo
{
	Goo()
	{
		/*
		int iid = Reporter.createReporter("Third One");
		ReporterImpl rk = Reporter.get(iid);
		rk.ignoreRegistry();
		Reporter.get(0).ignoreRegistry();
		*/
		for(int i = 0; i < 25; i++)
			Reporter.crit("Reporter CRITICAL Message #" + i);//NOI18N // +"asdkl slkd aklsdalksdlaksdalksd aslkdalkd alskdalskd aslkdalksd alskda lskla skdlaskd asdalskdalskd alsdia lskd");
		for(int i = 0; i < 25; i++)
			Reporter.verbose("Reporter VERBOSE Message #" + i);//NOI18N // +"asdkl slkd aklsdalksdlaksdalksd aslkdalkd alskdalskd aslkdalksd alskda lskla skdlaskd asdalskdalskd alsdia lskd");
		//rk.crit("Through ReporterImpl");
	}
}

