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
package com.sun.enterprise.ee.synchronization.processor;

import java.io.File;
import java.util.List;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.ee.synchronization.TextProcess;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.util.io.Utils;

/**
 * Context used while processing a synchronization request.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class RequestContext {

    /**
     * Constructor.
     *
     * @param  ctx  config context
     */
    public RequestContext(ConfigContext ctx, SynchronizationRequest[] reqs) {

        _startTime = System.currentTimeMillis();
        _configCtx = ctx;
        if (reqs != null) {
            // tokenizes the meta data in request
            TextProcess.transformDASConfig(reqs);
        }
        _requests    = reqs;
    }

    /**
     * Returns the config context.
     *
     * @return  config context
     */
    public ConfigContext getConfigContext() {
        return _configCtx;
    }

    /**
     * Returns the synchronization requests.
     *
     * @return  synchronization requests
     */
    public SynchronizationRequest[] getRequests() {
        return _requests;
    }

    /**
     * Returns the time delta between file system and DAS's JVM.
     *
     * @return    delta between file system and DAS
     */
    public long getTimeDelta() {
        return _delta;
    }

    /**
     * Sets the time delta between file system and DAS's JVM. 
     *
     * @param  delta  time delta between file system and DAS
     */
    public void setTimeDelta(long delta) {
        _delta = delta;
    }

    /**
     * Returns the start time for this request processing.
     *
     * @return  start time for this request processing
     */
    public long getStartTime() {
        return _startTime;
    }

    /**
     * Sets the start time for this request processing.
     *
     * @param  startTime   start time for this request processing
     */
    public void setStartTime(long startTime) {
        _startTime = startTime;
    }

    /**
     * Returns the zip file location for this request or null when 
     * request is not dealing with zip.
     * 
     * @return  zip file location
     */
    public File getZipFile() {
        if (_zipFile == null) {
            _zipFile = Utils.getTempZipFile();
        }

        return _zipFile;
    }

    /**
     * Sets the zip file for this request.
     *
     * @param  f   zip file
     */
    public void setZipFile(File f) {
        _zipFile = f;
    }

    /**
     * Returns true if process inventory is enabled.
     *
     * @return  true if process inventory is enabled
     */
    public boolean isProcessInventory() {
        return _processInventory;
    }

    /**
     * Sets the process inventory flag. When true, process inventory is 
     * enabled. 
     *
     * @param    pi   new process inventory value
     */
    public void setProcessInventory(boolean pi) {
        _processInventory = pi;
    }

    // ---- VARIABLES - PRIVATE --------------------------------------
    private ConfigContext _configCtx           = null;
    private SynchronizationRequest[] _requests = null;
    private File _zipFile                      = null;
    private long _delta                        = 0;
    private long _startTime                    = 0;
    private boolean _processInventory          = true;
}
