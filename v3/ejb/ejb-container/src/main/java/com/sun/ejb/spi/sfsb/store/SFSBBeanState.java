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
 * SFSBBeanState.java
 *
 * Created on May 12, 2003, 3:21 PM
 */

package com.sun.ejb.spi.sfsb.store;

/**
 * @author lwhite
 */
public class SFSBBeanState {

    /**
     * Creates a new instance of SFSBBeanState
     */
    public SFSBBeanState(Object sessionId, long lastAccess,
                         boolean isNew, byte[] state,
                         SFSBStoreManager storeManager) {
        _clusterId = null;
        _containerId = -1;
        _id = sessionId;
        _lastAccess = lastAccess;
        _isNew = isNew;
        _state = state;
        _storeManager = storeManager;
    }

    public SFSBBeanState(String clusterId, long containerId,
                         Object sessionId, long lastAccess,
                         boolean isNew, byte[] state,
                         SFSBStoreManager storeManager) {
        _clusterId = clusterId;
        _containerId = containerId;
        _id = sessionId;
        _lastAccess = lastAccess;
        _isNew = isNew;
        _state = state;
        _storeManager = storeManager;
    }

    public String getClusterId() {
        return _clusterId;
    }

    public long getContainerId() {
        return _containerId;
    }

    /**
     * @return id (key)
     */
    public Object getId() {
        return _id;
    }

    /**
     * @return last access time
     */
    public long getLastAccess() {
        return _lastAccess;
    }

    /**
     * @return tx checkpoint duration (milliseconds)
     */
    public long getTxCheckpointDuration() {
        return _txCheckpointDuration;
    }

    /**
     * set tx checkpoint duration (milliseconds)
     * by semantic convention the setter will usually
     * be incrementing the present value by an additional duration
     */
    public void setTxCheckpointDuration(long value) {
        _txCheckpointDuration = value;
    }

    /**
     * @return isNew - true means not persistent; false means already persistent
     */
    public boolean isNew() {
        return _isNew;
    }

    /**
     * @return state
     */
    public byte[] getState() {
        return _state;
    }

    public SFSBStoreManager getSFSBStoreManager() {
        return _storeManager;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    private String _clusterId;
    private long _containerId;
    private Object _id = null;
    private long _lastAccess = 0L;
    private long _txCheckpointDuration = 0L;
    private boolean _isNew = false;
    private byte[] _state = null;
    private SFSBStoreManager _storeManager;
    private long version;

}
