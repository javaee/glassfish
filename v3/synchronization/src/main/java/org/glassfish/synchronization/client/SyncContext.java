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

package org.glassfish.synchronization.client;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.synchronization.filemanagement.FileServiceManager;
import org.glassfish.synchronization.loadbalancer.LoadBalancerInterface;
import org.glassfish.synchronization.manifest.ManifestManager;
import org.glassfish.synchronization.util.CookieManager;
import org.glassfish.synchronization.util.FileUtils;
import org.glassfish.synchronization.util.StaticSyncInfo;
import org.glassfish.synchronization.util.ZipUtility;

/**
 * Object holds all needed synchronization objects and utilities
 * 
 * @author Behrooz Khorashadi
 * 
 */
public class SyncContext {
	private ManifestManager _man_Manager;
	private CookieManager _cookieM;
	private StaticSyncInfo sync;
	private FileUtils f_utils;
	private ZipUtility zip_util;
	private FileServiceManager fsm;
	private LoadBalancerInterface balancer;
	private Logger logger;

	public void setManifestManager(ManifestManager m) {
		_man_Manager = m;
	}

	public ManifestManager getManifestManager() {
		return _man_Manager;
	}

	public void setCookieManager(CookieManager c) {
		_cookieM = c;
	}

	public CookieManager getCookieManager() {
		return _cookieM;
	}

	/**
	 * This function will also set the logger which can be created once a
	 * StaticSyncInfo object is created.
	 * 
	 * @param s
	 * @throws IOException
	 * @throws SecurityException
	 */
	public void setStaticSyncInfo(StaticSyncInfo s) throws SecurityException,
			IOException {
		sync = s;
		createLogger(sync.getLogLevel(), sync.getBasePath()
				+ StaticSyncInfo.SYNC_FOLDER);
	}

	public StaticSyncInfo getStaticSyncInfo() {
		return sync;
	}

	public void setFileUtils(FileUtils f) {
		f_utils = f;
	}

	public FileUtils getFileUtils() {
		return f_utils;
	}

	public void setZipUtil(ZipUtility z) {
		zip_util = z;
	}

	public ZipUtility getZipUtility() {
		return zip_util;
	}

	public void setFileManager(FileServiceManager f) {
		fsm = f;
	}

	public FileServiceManager getFileManager() {
		return fsm;
	}

	public void setLoadBalancer(LoadBalancerInterface b) {
		balancer = b;
	}

	public int getBalancerLoad() {
		return balancer.getLoad();
	}

	public Logger getLogger() {
		return logger;
	}

	private void createLogger(Level loglevel, String path)
			throws SecurityException, IOException {
		FileHandler handler = new FileHandler(path + "log.txt");
		logger = Logger.getLogger(path);
		logger.setLevel(sync.getLogLevel());
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
	}

	public void resetContext() {
		_man_Manager = null;
		_cookieM = null;
		fsm = null;
		balancer = null;
	}
}
