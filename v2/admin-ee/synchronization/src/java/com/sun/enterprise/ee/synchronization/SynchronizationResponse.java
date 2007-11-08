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
package com.sun.enterprise.ee.synchronization;

import java.util.List;
import java.io.Serializable;


/**
 * Class SynchronizationResponse
 *
 */
public class SynchronizationResponse implements Serializable
{

    private byte[] _zipBytes;
    private SynchronizationRequest[] _reply;
    private long _checksum;
    private long _synchronizationStartTime;
    private long _synchronizationEndTime;
    private String _zipLocation;
    private long _zipLastModified;
    private String _dasHostName;
    private List _fileList;

    /**
     * Constructor SynchronizationResponse
     *
     *
     * @param bytes
     * @param reply
     *
     */
    public SynchronizationResponse(byte[] bytes,
                                   SynchronizationRequest[] reply,
                                   long checksum,
                                   long synchronizationStartTime,
                                   long synchronizationEndTime) {
        _zipBytes = bytes;
        _reply = reply;
        _checksum = checksum;
        _synchronizationStartTime = synchronizationStartTime;
        _synchronizationEndTime = synchronizationEndTime;
    }

    /**
     * Method getZipBytes
     *
     *
     * @return
     *
     */
    public byte[] getZipBytes() {
        return _zipBytes;
    }

    /**
     * Method getReply
     *
     *
     * @return
     *
     */
    public SynchronizationRequest[] getReply() {
        return _reply;
    }
    
    /**
     * Method getChecksum
     *
     *
     * @return
     *
     */
    public long getChecksum() {
        return _checksum;
    }
    
    public long getSynchronizationStartTime() {
        return _synchronizationStartTime;
    }
    
    public long getSynchronizationEndTime() {
        return _synchronizationEndTime;
    }

    /**
     * Sets the zip location for this response.
     * 
     * @param  location  zip location
     */
    public void setZipLocation(String location) {
        _zipLocation = location;
    }

    /**
     * Returns the zip location for this response.
     *
     * @return  zip location
     */
    public String getZipLocation() {
        return _zipLocation;
    }

    /**
     * Returns the last modified timestamp of the zip file.
     *
     * @return  last modified timestamp of the zip
     */
    public long getLastModifiedOfZip() {
        return _zipLastModified;
    }

    /**
     * Sets the last modified timestamp of the zip file.
     * 
     * @param  ts  last modified timestamp of the zip
     */
    public void setLastModifiedOfZip(long ts) {
        _zipLastModified = ts;
    }

    /**
     * Sets the name of the DAS host.
     *
     * @param  host  name of DAS host
     */
    public void setDasHostName(String host) {
        _dasHostName = host;
    }

    /**
     * Returns the DAS host name
     *
     * @return  DAS host name
     */
    public String getDasHostName() {
        return _dasHostName;
    }

    /**
     * Sets the file list. This list contains the names of files found 
     * in the central repository for the request.
     *
     * @param  list  file list
     */
    public void setFileList(List list) {
        _fileList = list;
    }

    /**
     * Return the list of file names found in the central repository 
     * for the synchronization request.
     *
     * @return  list of file names found in the central repository
     */
    public List getFileList() {
        return _fileList;
    }
}
