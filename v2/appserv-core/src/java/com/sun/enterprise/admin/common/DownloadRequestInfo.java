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
package com.sun.enterprise.admin.common;

import java.io.File;
import java.io.Serializable;

/**
 * Contains information pertaining to a download request. 
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 *
 * @author  Nazrul Islam
 * @since   J2SE1.4
 */
public class DownloadRequestInfo implements Serializable {

    /**
     * Constructor.
     */
    private DownloadRequestInfo() {
        _downloadFilePath    = null;
        _numChunks           = 0;
        _numBytesSent        = 0;
        _isPrepared          = false;
        _chunkIndex          = -1;
        _chunk               = null;
    }

    public DownloadRequestInfo(File f) {
        super();

        if (f.exists()) {
            _downloadFilePath = f.getAbsolutePath();

            // total size of the download file
            _totalFileSize   = f.length();

            // total number of chunks
            _numChunks  = (int)(_totalFileSize/ (long)ByteChunk.kChunkMaxSize);

            // verifies the total
            if (_numChunks * (long)ByteChunk.kChunkMaxSize < _totalFileSize) {
                _numChunks += 1;
            }

            // sets the prepared flag
            _isPrepared = true;
            _chunkIndex = 0;
        }
    }

    /**
     * Returns the download file path.
     *
     * @return  download file path
     */
    public String getDownloadFilePath() {
        return _downloadFilePath;
    }

    /**
     * Sets the download file path.
     *
     * @param  f  download file path
     */
    void setDownloadFilePath(String f) {
        _downloadFilePath = f;
    }

    /**
     * Returns total number of download chunks for this request.
     * 
     * @return  total number of download chunks
     */
    public int getNumberOfChunks() {
        return _numChunks;
    }

    /**
     * Sets the total number of download chunks.
     *
     * @param  n  total number of download chunks
     */
    void setNumberOfChunks(int n) {
        _numChunks = n;
    }

    /**
     * Returns true if the request info is computed
     *
     * @return  if the request is prepared
     */
    public boolean isPrepared() {
        return _isPrepared;
    }

    /**
     * Sets the prepared flag for this request.
     *
     * @param  p  prepared flag for this request.
     */
    void setPrepared(boolean p) {
        _isPrepared = p;
    }

    /**
     * Returns the current chunk index.
     *
     * @return  the current chunk index
     */
    public int getChunkIndex() {
        return _chunkIndex;
    }

    /**
     * Sets the current chunk index.
     *
     * @param  i  current chunk index
     */
    void setChunkIndex(int i) {
        _chunkIndex = i;

        // resets the byte chunk
        _chunk      = null;
    }

    /**
     * Returns the total number of bytes downloaded.
     *
     * @return  total number of bytes downloaded
     */
    public long getNumberOfBytesSent() {
        return _numBytesSent;
    }

    /**
     * Increments the total number of bytes sent out.
     *
     * @param  bytesRead  number of bytes to be added to the total
     */
    public void incrementNumberOfBytesSent(int bytesRead) {
        _numBytesSent += bytesRead;
    }

    /**
     * Returns true if this is the first chunk to download.
     *
     * @return  true if this is the first chunk
     */
    public boolean isFirstChunk() {
        return (_chunkIndex == 0);
    }

    /**
     * Returns true if this the last chunk to download.
     *
     * @return  true if this the last chunk.
     */
    public boolean isLastChunk() {
        return (_chunkIndex == (_numChunks-1));
    }

    /**
     * Returns the byte chunk for this request.
     *
     * @return  byte chunk
     */
    public ByteChunk getChunk() {
        return _chunk;
    }

    /**
     * Sets the byte chunk for this request.
     *
     * @param  chunk  byte chunk
     */
    public void setChunk(ByteChunk chunk) {
        _chunk = chunk;
    }

    /**
     * Returns the total file size for this request.
     *
     * @return  total file size
     */
    public long getTotalFileSize() {
        return _totalFileSize;
    }

    // ---- VARIABLES - PRIVATE -------------
    private String    _downloadFilePath;
    private int       _numChunks;
    private long      _numBytesSent;
    private boolean   _isPrepared;
    private int       _chunkIndex;
    private ByteChunk _chunk;
    private long      _totalFileSize;
}
