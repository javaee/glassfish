/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * SampleGetter.java
 *
 * Created on September 21, 2004, 1:58 PM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.util.*;

/**
 *
 * @author  bnevins
 */
class SampleGetter
{
	SampleGetter(Properties p)
	{
		props = p;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	List getSampleList() throws DeploymentTestsException
	{
		String sampleDir = props.getProperty("sampledir");

		if(sampleDir != null && sampleDir.length() > 0)
			return getSampleListFromDir(sampleDir);
		else
			return getSampleListFromProps();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private List getSampleListFromDir(String dirName) throws DeploymentTestsException
	{
		// sampledir has a value in the properties file.  We are committed.  If
		// there are any problems, it is an error and we don't try to get explicit
		// samples from the props file.
		
		String usage = "Put some sample files in a directory" +
				" and specify it in the properties file like so:  \"sampledir=./samples\"";
		
		File samplesDir = Utils.safeGetCanonicalFile(new File(dirName));

		// does the directory exist?
		if(! (samplesDir.exists() && samplesDir.isDirectory()))
			throw new DeploymentTestsException("samples dir doesn't exist or is not a directory (" +
				samplesDir + ").\n" + usage);

		List samples = new ArrayList();

		//	does the directory have sample archive files in it?
		File[] sampleFiles = samplesDir.listFiles(new Utils.ArchiveFilter());

		for(int i = 0; sampleFiles != null && i < sampleFiles.length; i++)
		{
			samples.add(new Utils.Sample(sampleFiles[i]));
		}
		
		// now look for dir-deploys...
		sampleFiles = samplesDir.listFiles(new Utils.DirDeployFilter());
		
		for(int i = 0; sampleFiles != null && i < sampleFiles.length; i++)
		{
			samples.add(new Utils.Sample(sampleFiles[i]));
			//System.err.println("ZZZZZZ dir-deploy: " + sampleFiles[i]);
			//System.exit(1);
		}

		if(samples.size() <= 0)
			throw new DeploymentTestsException("No samples in " + samplesDir + ".\n" + usage);

		return samples;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private List getSampleListFromProps() throws DeploymentTestsException
	{
		throw new DeploymentTestsException("Not Implemented Yet!");
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private	Properties	props;
}
