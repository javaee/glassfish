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
 * HADBStartDB.java
 *
 * Created on March 15, 2005, 9:53 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

/**
 *
 * @author bnevins
 */
public class HADBRestartDB
{
	public HADBRestartDB(HADBInfo info)
	{
		this.info = info;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public Object[] restartDB() throws HADBSetupException
	{
		// note that the DB may already be running...
		HADBUtils.setPhonyReturnValue(0);
		String[] commands = info.getRestartCommands();
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);
		int exitValue = exec.exec();

		if(exitValue != 0)
		{
			// not running: " hadbm:Error 22105: Database foo is not running.
			if(exec.isHadbmError(22105))
				throw new HADBSetupException("hadbmgmt-res.AlreadyStopped", info.getClusterName());

			// unknown error...
			throw new HADBSetupException("hadbmgmt-res.RestartFailed",
				new Object[] {info.getClusterName(), "" + exitValue, exec.getStdout(), exec.getStderr()} );
		}
		else
		{
			String s = StringHelper.get("hadbmgmt-res.RestartSucceeded", info.getClusterName());
			LoggerHelper.fine(s);
			info.addMsg(s);
		}

		return info.prepMsgs();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private HADBInfo	info;
	
}
