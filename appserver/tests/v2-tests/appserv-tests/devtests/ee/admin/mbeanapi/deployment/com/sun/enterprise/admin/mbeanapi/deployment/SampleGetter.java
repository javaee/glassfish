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
