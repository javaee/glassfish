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
