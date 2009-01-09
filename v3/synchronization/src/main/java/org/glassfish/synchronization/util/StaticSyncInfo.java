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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.synchronization.central.MainServerConfig;
import org.glassfish.synchronization.client.ClientConfig;
import org.glassfish.synchronization.client.SyncContext;
import org.glassfish.synchronization.manifest.ManifestManager;

/**
 * A simple class which holds all the unchanging (static but not in the java
 * quaifier sense) information needed by client and server.
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class StaticSyncInfo {
	private static final String MAN_FILE_NAME = "manifest.txt";
	private static final String COOKIE_FILE_NAME = "cookie.txt";
	public static final String CONFIG_FILE_NAME = "sync_config.txt";
	/** the folder where all the received files are unpacked */
	private String BASE_PATH = "Instances" + File.separator + "Idefault"
			+ File.separator;
	/** Das URL */
	private String DAS_URL = "http//:localhost:5555";
	/** Temporary folder where received files are dropped */
	private String TMP_FOLDER = System.getProperty("java.io.tmpdir")
			+ File.separator + "appserver-tmp" + File.separator;
	private boolean HASH_VERIFY = false;
	/** Use NIO to zip files */
	private boolean NIO = false;
	/** Print log to stdout for debugging only */
	private boolean SYS_OUT = false;
	/** Sets log level for logging only */
	private Level LOG = Level.INFO;
	/** Designates how many SyncAgents are used to sync (Sync Agent Threads) */
	private int SAT = 1;
	/** Address at which local grizzly adapter is on */
	private String machineAddress = null;
	/** The port on which the grizzly adapter is running */
	private int grizzlyPort;
	private String MONITOR_URL = null;

	public StaticSyncInfo(String base, File config, SyncContext c)
			throws IOException {
		BASE_PATH = FileUtils.formatPath(base);
		TMP_FOLDER = BASE_PATH + SYNC_FOLDER + "LocalTmpDir" + File.separator;
		File localConfig = new File(base + SYNC_FOLDER + CONFIG_FILE_NAME);
		if (localConfig.exists()) {
			parseConfigFile(localConfig);
		} else
			parseConfigFile(config);
		createFolders();
		c.setStaticSyncInfo(this);
	}
	public StaticSyncInfo(MainServerConfig config, SyncContext c)
															throws IOException {
		BASE_PATH = config.base_dir;
		TMP_FOLDER = BASE_PATH + SYNC_FOLDER + "LocalTmpDir" + File.separator;
		createFolders();
		c.setStaticSyncInfo(this);
	}
	public StaticSyncInfo(ClientConfig config, SyncContext c) 
															throws IOException {
		BASE_PATH = FileUtils.formatPath(config.base_dir);
		TMP_FOLDER = BASE_PATH + SYNC_FOLDER + "LocalTmpDir" + File.separator;
		SAT = config.sync_threads;
		DAS_URL = config.das_url;
		HASH_VERIFY = config.verify;
		setLogLevel(config.logLevel);
		createFolders();
		c.setStaticSyncInfo(this);
	}
//	public StaticSyncInfo(String base, String das, File config, SyncContext c)
//			throws IOException {
//		this(base, config, c);
//		DAS_URL = das;
//	}

	public String getCookieFilePath() {
		return BASE_PATH + SYNC_FOLDER + COOKIE_FILE_NAME;
	}

	public String getBasePath() {
		return BASE_PATH;
	}

	public String getManifestFilePath() {
		return BASE_PATH + SYNC_FOLDER + MAN_FILE_NAME;
	}

	public static String getManFileName() {
		return MAN_FILE_NAME;
	}

	public static String getCookieFileName() {
		return COOKIE_FILE_NAME;
	}

	public String getDasUrl() {
		return DAS_URL;
	}

	public String getTempFolder() {
		return TMP_FOLDER;
	}

	public void setServerIP(String addr) {
		machineAddress = addr;
	}

	public String getServerAddress() {
		return "http://" + machineAddress + ":" + grizzlyPort;
	}

	public void setPort(int p) {
		grizzlyPort = p;
	}

	public boolean getNIO() {
		return NIO;
	}

	public boolean getSysOut() {
		return SYS_OUT;
	}

	public Level getLogLevel() {
		return LOG;
	}

	public int getSAT() {
		return SAT;
	}

	public boolean getHashVerify() {
		return HASH_VERIFY;
	}
	/**
	 * Returns the monitoring url but monitoring is done now by central server
	 * as well.
	 * @return
	 * @deprecated
	 */
	public String getMonitorURL() {
		return DAS_URL;
	}
	/**
	 * Sets the log level from a string
	 * @param logLevel string representation of the log level
	 */
	private void setLogLevel(String logLevel) {
		if (logLevel.equals("FINE")) {
			LOG = Level.FINE;
		} else if (logLevel.equals("INFO")) {
			LOG = Level.INFO;
		} else {
			LOG = Level.INFO;
		}
	}
	/**
	 * Parses the config file setting all relevant variables
	 * 
	 * @param config
	 *            the config file
	 * @throws IOException
	 */
	private void parseConfigFile(File config) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(config));
		try {
			String line = null;
			String[] split = null;
			while ((line = input.readLine()) != null) {
				split = line.split("=");
				setVariable(split[0], split[1]);
			}
		} finally {
			input.close();
		}
	}

	private void setVariable(String var, String value) throws IOException {
		if (var.equals("NIO"))
			NIO = Boolean.parseBoolean(value);
		else if (var.equals("SAT")) {
			SAT = Integer.parseInt(value);
			if (SAT < 1)
				throw new IOException("Must have atleast one SyncAgent");
		} else if (var.equals("SYS_OUT"))
			SYS_OUT = Boolean.parseBoolean(value);
		else if (var.equals("DAS_URL"))
			DAS_URL = value;
		else if (var.equals("HASH_VERIFY"))
			HASH_VERIFY = Boolean.parseBoolean(value);
		// else if(var.equals("Machine-Address"))
		// machineAddress = value;
//		else if (var.equals("MONITOR_URL"))
//			MONITOR_URL = value;
		else if (var.equals("LOG")) {
			if (value.equals("FINE")) {
				LOG = Level.FINE;
			} else if (value.equals("INFO")) {
				LOG = Level.INFO;
			} else {
				LOG = Level.INFO;
			}
		}
	}

	/**
	 * Creates the folders needed for synchronization
	 * 
	 * @throws IOException
	 */
	private void createFolders() throws IOException {
		File baseDir = new File(BASE_PATH);
		boolean success = baseDir.exists();
		for (int i = 0; i < 10 && !success; i++) {
			success = (baseDir.mkdirs());
		}
		if (!success)
			throw new IOException("Could not create local folder"
					+ baseDir.getPath());
		success = false;
		File tempDir = new File(TMP_FOLDER);
		success = tempDir.exists();
		for (int i = 0; i < 10 && !success; i++) {
			success = (tempDir.mkdirs());
		}
		if (!success)
			throw new IOException("Could not create local tmp folder "
					+ tempDir.getPath());
		File config_dir = new File(BASE_PATH + SYNC_FOLDER);
		success = false;
		success = config_dir.exists();
		for (int i = 0; i < 10 && !success; i++) {
			success = (config_dir.mkdirs());
		}
		if (!success)
			throw new IOException("Could not create local config folder "
					+ config_dir.getPath());
	}

	public static final String SYNC_FOLDER = "sync-config" + File.separator;
	public final int MAX_FILES_IN_MEM = 1;
}
