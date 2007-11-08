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

package com.sun.ejb.base.sfsb;

import java.util.ArrayList;

import java.io.File;

import java.util.logging.*;
import com.sun.logging.*;

import com.sun.ejb.spi.sfsb.SFSBUUIDUtil;
import com.sun.ejb.spi.container.ContainerService;

import com.sun.ejb.containers.ContainerFactoryImpl;


/**
 * @author     Mahesh Kannan
 */
 
public class PersistentFileStoreManager
    extends AbstractFileStoreManager
{
    private SFSBUUIDUtil uuidGenerator;

    /**
     * No arg constructor
     */
    public PersistentFileStoreManager() {
    }

    protected void onInitialization() {

        try {
	    uuidGenerator = new SimpleKeyGenerator();
    
	    if (baseDir.isDirectory()) {
		String[] fileNames = baseDir.list();
		_logger.log(Level.FINE, storeManagerName
		    + "; removing: " + fileNames.length + " sessions");

		removeExpiredPassivatedSessions(fileNames);     
	    }
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, "ejb.sfsb_persistmgr_oninit_failed",
		    new Object[] {storeManagerName});
	    _logger.log(Level.WARNING, "ejb.sfsb_persistmgr_oninit_failed_exception", ex);
	}
    }

    public SFSBUUIDUtil getUUIDUtil() {
        return uuidGenerator;
    }

    public void shutdown() {
        super.shutdown();
    }

    private void removeExpiredPassivatedSessions(String[] fileNames) {
	if (fileNames.length > 0) {
	    AsyncFileRemovalTask  task = new AsyncFileRemovalTask(
		    this, fileNames);
	    try {
		ContainerService service = ContainerFactoryImpl.getContainerService();

		//scheduleWork performs the task on the same thread
		//  if it cannot schedule the task for async execution
		service.scheduleWork(super.getClassLoader(), task);
	    } catch (Throwable th) {
		//We would be here only if containerService is null

		_logger.log(Level.FINE, storeManagerName
			+ ": Cannot execute file removal aynchronously", th);

		//Execute in the current thread
		task.run(); //Doesn't throw any exception
	    }
	}
    }

    private static class AsyncFileRemovalTask
	implements Runnable
    {
	AbstractFileStoreManager	storeManager;
	String[]			fileNames;

	AsyncFileRemovalTask(AbstractFileStoreManager manager,
		String[] fileNames)
	{
	    this.storeManager = manager;
	    this.fileNames = fileNames;
	}

	public void run() {
	    try {
		int sz = fileNames.length;
		for (int i=0; i<sz; i++) {
		    storeManager.remove(fileNames[i]);
		}
	    } catch (Exception ex) {
		_logger.log(Level.FINE, "Error while removing expired "
			+ "file during startup", ex);
	    }
	}
    }
}
