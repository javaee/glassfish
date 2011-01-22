/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;

import java.io.*;

import java.util.*;
import java.util.jar.*;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.common_impl.Jar;

import com.sun.hk2.component.InhabitantsFile;

/**
 * Generates a consolidated OSGI bundle with a consolidated 
 * HK2 header
 *
 * @goal hk2-generate
 * @phase prepare-package
 * 
 * @requiresProject true
 * @requiresDependencyResolution compile
 * @author Sivakumar Thyagarajan
 */
/* We use prepare-package as the phase as we need to perform this consolidation before the maven-bundle-plugin's bundle goal gets executed in the package phase.*/

public class HK2GenerateMojo extends AbstractMojo {

	/**
	 * Directory where the manifest will be written
	 * 
	 * @parameter expression="${manifestLocation}"
	 *            default-value="${project.build.outputDirectory}"
	 */
	protected File manifestLocation;

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	public void execute() throws MojoExecutionException {
		if (project.getDependencyArtifacts() != null) {
			List<String> consolidatedInhabitants = new ArrayList<String>();

			// Create the consolidated inhabitant file contents by
			// going through all dependency artifacts
			for (Artifact a : (Set<Artifact>) project.getDependencyArtifacts()) {
				if (a.getScope() != null && a.getScope().equals("test"))
					continue;
				getLog().info("Dependency Artifact: " + a.getFile().toString());

				try {
					JarFile jf = new JarFile(a.getFile());
					JarEntry je = jf.getJarEntry(InhabitantsFile.PATH);
					if (je == null)
						continue;
					getLog().debug("Dependency Artifact " + a + " has Inhabitants File: " + je);

					Enumeration<JarEntry> entries = jf.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (!entry.isDirectory()
								&& entry.getName().startsWith(
										InhabitantsFile.PATH)) {
							getLog().info("Entry: " + entry.getName());
							consolidatedInhabitants.addAll(loadInhabitants(jf,
									entry));
						}
					}
				} catch (IOException iex) {
					iex.printStackTrace();
				}
			}

		    writeToFile(consolidatedInhabitants);
		}
	}

	private List<String> loadInhabitants(JarFile jf, JarEntry e)
			throws IOException {
		List<String> l = new ArrayList<String>();

		BufferedReader reader = null;
		try {
			DataInputStream in = new DataInputStream(jf.getInputStream(e));
			reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				l.add(line);
			}

		} catch (IOException iex) {
			iex.printStackTrace();
		} finally {
			reader.close();
		}
		return l;
	}

	private void writeToFile(List<String> consolidatedInhabitants) {
		// Write the consolidated inhabitant file contents
		// to manifestLocation
		
		//Create Manifest directory. 
		String inhabitantsDir = "" + manifestLocation + File.separatorChar
		+ "META-INF" + File.separatorChar + "inhabitants";
		boolean success = (new File(inhabitantsDir)).mkdirs();
		
		String fileLocation = inhabitantsDir + File.separatorChar + "default";
		getLog().info("Writing consolidated inhabitants to: " + fileLocation);

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fileLocation));
			for (String s : consolidatedInhabitants) {
				out.write(s);
				out.newLine();
			}
		} catch (IOException iex) {
			iex.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException iex) {
				//ignore
			}
		}
	}

}
