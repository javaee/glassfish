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

//JDK imports
import java.io.Serializable;

/**
	A Class that represents a Chunk of Bytes. This is designed to be used
	to transfer bytes of data between remote entities. There are some properties
	of the class that will allow users of this class to take specific
	actions. Maximum size of the chunk is 64 KBytes. Needs to be Serializable,
	for ByteChunks may transmit over wire.
	<p>
	Chunks are generally parts of file identified by a name. Note that this
	class is modeled to be immutable.
	<p>
	@author Kedar Mhaswade
	@version 1.0
*/

public class ByteChunk implements Serializable
{
	/* javac 1.3 generated serialVersionUID */
	public static final long serialVersionUID			= 9100504074948693275L;
    public static final int kChunkMaxSize               = 10485760;
    public static final int kChunkMinSize               = 0;  //  0 Bytes
    
    private int     mSize;
    private boolean mIsLast;
    private boolean mIsFirst;
    private byte[]  mBytes;
    private String  mChunkedFileName;
    private String  mTargetDir;
    private String  mUniqueId;
    private long    mTotalFileSize;

	/** 
        Creates new ByteChunk with the specified size. The chunk may not be
        larger than the size specfied by kChunkMaxSize.
        
        @throws IllegalArgumentException if the size of Chunk is more than
        kChunkSize or less than kChunkMinSize..
	 
		@param byteArray specifies the array of bytes that constitutes chunk.
		@param forFileName specifies the name of the file whose part is it.
		@param isFirst boolean specifying if this is the first of the chunks.
		@param isLast boolean specifying if this is the first of the chunks.
		@param uniqueId String specifies an unique id for this series of chunks. 
                        This has to be same in all chunks of the series. 
		@param totalFileSize long specifies total file size of the chunked file.
    */
	
    public ByteChunk(byte[] byteArray, String forFileName, 
        boolean isFirst, boolean isLast, String uniqueId, long totalFileSize)
    {
        int size =0;
        if (byteArray != null) {
            size = byteArray.length;
            if ( size < kChunkMinSize || size > kChunkMaxSize)
            {
                throw new IllegalArgumentException(size + "");
            }
        }
        mSize               = size;
        mIsFirst            = isFirst;
        mIsLast             = isLast;
        mBytes              = byteArray;
        mChunkedFileName    = forFileName;
        mUniqueId           = uniqueId;
        mTotalFileSize      = totalFileSize;
    }
 
	/** 
        Creates new ByteChunk with the specified size. The chunk may not be
        larger than the size specfied by kChunkMaxSize.
        
        @throws IllegalArgumentException if the size of Chunk is more than
        kChunkSize or less than kChunkMinSize..
	 
		@param byteArray specifies the array of bytes that constitutes chunk.
		@param forFileName specifies the name of the file whose part is it.
		@param isFirst boolean specifying if this is the first of the chunks.
		@param isLast boolean specifying if this is the first of the chunks.
    */
	
    public ByteChunk(byte[] byteArray, String forFileName, 
        boolean isFirst, boolean isLast)
    {
        this(byteArray,forFileName,isFirst,isLast,forFileName, -1);
    }
 
    /**
        Determines whether this is the last Chunk in a series of Chunk transfer.
        
        @return true if this is last Chunk, false otherwize.
    */
    
	public boolean isLast()
    {
        return ( mIsLast );
    }

	/**
        Determines whether this is the last Chunk in a series of Chunk transfer.
        
        @return true if this is last Chunk, false otherwize.
    */

    public boolean isFirst()
    {
        return ( mIsFirst );
    }
	
	/**
		Gets the actual size of the chunk. The chunk will have exactly this many
		bytes in the underlying byte array.
	 
		@return integer indicating the size of the chunk.
	*/
    public int getSize()
    {
        return ( mSize );
    }
	/**
		Gets the array of actual bytes.
	 
		@return byte[] with actual bytes.
	*/
    public byte[] getBytes()
    {
        return mBytes;
    }
	
	/**
		Returns the <code> name </code> of the file that was chunked.
	 
		@return String representing the name of file (whose this is a chunk).
	*/
    public String getChunkedFileName()
    {
        return ( mChunkedFileName );
    }

    /**
     * Returns the name of the target directory to which this chunk
     * is copied.
     */
    public String getTargetDir()
    {
        return ( mTargetDir );
    }

    /**
     * Sets the target dir to which this chunk must be copied.
     */
    public void setTargetDir(String targetDir)
    {
        mTargetDir = targetDir;
    }
    
    /**
     * Returns the unique id for this series of chunks.
     */
    public String getId() {
        return mUniqueId;
    }

    /**
     * returns the total file size.
     */
    public long getTotalFileSize() {
        return mTotalFileSize;
    }
}
