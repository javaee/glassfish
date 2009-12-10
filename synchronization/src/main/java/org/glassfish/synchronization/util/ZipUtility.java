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
package org.glassfish.synchronization.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.glassfish.synchronization.client.SyncContext;

/**
 * Used to create zip files
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class ZipUtility {

	private SyncContext context;

	public ZipUtility(SyncContext c) {
		context = c;
		c.setZipUtil(this);
	}

	public void createManifestZip(String sourceFile, File zipDestination)
			throws IOException {
		ZipOutputStream out;
		byte[] buf = new byte[1024];
		out = new ZipOutputStream(new FileOutputStream(zipDestination));
		FileInputStream infile = new FileInputStream(sourceFile);
		BufferedInputStream bin = new BufferedInputStream(infile, BUFFER_SIZE);
		out.putNextEntry(new ZipEntry(sourceFile));
		// BufferedOutputStream bout = new BufferedOutputStream(out);
		int len;
		while ((len = bin.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.closeEntry();
		bin.close();
		out.close();
	}

	public File createZipNIO(BitSet bs, BitSet content) {// add your own
															// manifest bitset
		// Create a buffer for reading the files
		File outFile = null;
		int numFiles;
		int index = bs.nextSetBit(0);

		try {
			BufferedReader in = new BufferedReader(new FileReader(context
					.getStaticSyncInfo().getManifestFilePath()));
			LineNumberReader lineReader = new LineNumberReader(in);
			String[] split = lineReader.readLine().split(",");
			numFiles = new Integer(split[0]);
			if (index == numFiles) {
				// System.out.println("no files needed");
				return null;
			}
			// lineReader.setLineNumber(index+2);
			// Create the ZIP file
			outFile = context.getFileUtils().getTempZipFile();
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					outFile));
			// Zip files needed
			String str;
			String relativeFilePath;
			// Compress the files//
			String base_path = context.getStaticSyncInfo().getBasePath();
			while ((str = lineReader.readLine()) != null && index != -1) {
				if (lineReader.getLineNumber() < index + 2)
					continue;
				content.set(index);
				relativeFilePath = FileUtils.formatPath((str.split(","))[0]);
				File toZip = new File(base_path + relativeFilePath);
				// a check to make sure that the file wasn't at some point
				// deleted
				if (!toZip.exists()) {
					context.getManifestManager().missingFileUpdate(index);
					index = bs.nextSetBit(index + 1);
					continue;
				}
				out.putNextEntry(new ZipEntry(relativeFilePath));
				addFileWithNIO(toZip, out);
				out.closeEntry();
				index = bs.nextSetBit(index + 1);
			}
			lineReader.close();
			// Complete the ZIP file
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outFile;
	}

	/**
	 * Adds the given file content to the zip output stream using NIO. Before
	 * calling this method, a zip entry should be created. This method is
	 * optimal for large files.
	 * 
	 * @param file
	 *            file to be added to the zip
	 * @param out
	 *            output stream from the zip
	 * 
	 * @throws IOException
	 *             if an i/o error
	 * @throws FileNotFoundException
	 *             if file does not exist
	 */
	private void addFileWithNIO(File file, ZipOutputStream out)
			throws IOException, FileNotFoundException {

		FileInputStream fis = null;
		FileChannel fc = null;
		WritableByteChannel wbc = null;

		try {
			fis = new FileInputStream(file);
			fc = fis.getChannel();
			long sz = (long) fc.size();

			wbc = Channels.newChannel(out);

			long count = 0;
			int attempts = 0;
			while (count < sz) {
				long written = fc.transferTo(count, sz, wbc);
				count += written;

				if (written == 0) {
					attempts++;
					if (attempts > 100) {
						throw new IOException("Nio failed");
					}
				} else {
					attempts = 0;
				}
			}
			// _size += sz;

		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
			if (fc != null) {
				try {
					fc.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * This function desides whether to use NIO or not based on the context
	 * information.
	 */
	public File createZip(BitSet bs, BitSet content) throws IOException {
		if (context.getStaticSyncInfo().getNIO())
			return createZipNIO(bs, content);
		return createZipNoNIO(bs, content);
	}

	public File createZipNoNIO(BitSet bs, BitSet content) throws IOException {

		// Create a buffer for reading the files
		byte[] buf = new byte[1024];
		File outFile = null;
		int numFiles;
		int index = bs.nextSetBit(0);

		BufferedReader in = new BufferedReader(new FileReader(context
				.getStaticSyncInfo().getManifestFilePath()));
		LineNumberReader lineReader = new LineNumberReader(in);
		String[] split = lineReader.readLine().split(",");
		numFiles = new Integer(split[0]);
		if (index == numFiles) {
			return null;
		}
		// lineReader.setLineNumber(index+2);
		// Create the ZIP file
		outFile = context.getFileUtils().getTempZipFile();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
		// Zip files needed
		String str;
		String relativeFilePath;
		String base_path = context.getStaticSyncInfo().getBasePath();
		while ((str = lineReader.readLine()) != null && index != -1) {
			if (lineReader.getLineNumber() < index + 2)
				continue;
			content.set(index);
			relativeFilePath = (str.split(","))[0];
			File checkFile = new File(base_path + relativeFilePath);
			// a check to make sure that the file wasn't at some point deleted
			if (!checkFile.exists()) {
				context.getManifestManager().missingFileUpdate(index);
				index = bs.nextSetBit(index + 1);
				continue;
			}
			FileInputStream infile = new FileInputStream(base_path
					+ relativeFilePath);
			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(relativeFilePath));

			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = infile.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Complete the entry
			out.closeEntry();
			// infile.close();
			index = bs.nextSetBit(index + 1);
		}
		lineReader.close();
		// Complete the ZIP file
		out.close();
		return outFile;
	}

	public void unzipit(File zip, String basPath) throws FileNotFoundException,
			IOException {
		if (zip == null) // zip file is empty
			return;

		if (context.getLogger().isLoggable(Level.FINER)) {
			String log = "the zip name is" + zip.getPath();
			if (context.getStaticSyncInfo().getSysOut())
				System.out.println(log);
			context.getLogger().finer(log);
		}
		// Logger logger = Logger.getLogger("behrooz.com");
		// logger.info("the zip name is" + zip.getPath());
		FileInputStream fin = new FileInputStream(zip.getAbsolutePath());
		ZipInputStream zin = new ZipInputStream(fin);
		BufferedInputStream zbin = new BufferedInputStream(zin, BUFFER_SIZE);
		ZipEntry ze = null;
		BufferedOutputStream bout = null;
		try {
			while ((ze = zin.getNextEntry()) != null) {
				if (context.getLogger().isLoggable(Level.FINER)) {
					String log = "Unzipping " + ze.getName();
					if (context.getStaticSyncInfo().getSysOut())
						System.out.println(log);
					context.getLogger().finer(log);
				}
				// logger.info("Unzipping " + ze.getName());
				String path = basPath + FileUtils.formatPath(ze.getName());
				File file = new File(path);
				if (!file.getParentFile().exists()) {
					tryCreateParentDirectory(file.getParentFile());
				}
				FileOutputStream fout = new FileOutputStream(path);
				bout = new BufferedOutputStream(fout, BUFFER_SIZE);
				for (int c = zbin.read(); c != -1; c = zbin.read()) {
					bout.write(c);
				}
				zin.closeEntry();
				if (context.getLogger().isLoggable(Level.FINER)) {
					String log = "Completed unzip to: " + path;
					if (context.getStaticSyncInfo().getSysOut())
						System.out.println(log);
					context.getLogger().finer(log);
				}
				// logger.info("Completed unzip to: " + path);
				bout.close();
			}
		} finally {
			if (zbin != null)
				zbin.close();
			if (bout != null)
				bout.close();
		}
	}

	/**
	 * This function was created because in some cases we saw that the parent
	 * directory was not created and a file not found exception was being
	 * thrown.
	 * 
	 * @param parentFile
	 */
	private void tryCreateParentDirectory(File parentFile) {
		boolean success = false;
		for (int i = 0; i < 5 && !success; i++) {
			success = parentFile.mkdirs();
		}
	}

	public File unZipFile(File zip, String file_pathName) throws IOException {
		File outfile = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bin = null;
		try {
			// Open the ZIP file
			ZipInputStream in = new ZipInputStream(new FileInputStream(zip
					.getAbsoluteFile()));

			// Get the first entry
			in.getNextEntry();

			// Open the output file
			outfile = new File(file_pathName);
			OutputStream out = new FileOutputStream(outfile);
			bos = new BufferedOutputStream(out, BUFFER_SIZE);
			bin = new BufferedInputStream(in, BUFFER_SIZE);
			// Transfer bytes from the ZIP file to the output file
			byte[] buf = new byte[1024];
			int len;
			while ((len = bin.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}

		} finally {
			// Close the streams
			if (bos != null)
				bos.close();
			if (bin != null)
				bin.close();
		}
		return outfile;
	}

	private static final int BUFFER_SIZE = 65536;
}
