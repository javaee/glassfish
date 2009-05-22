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
package org.glassfish.admin.amx.base;

import java.io.File;
import java.io.IOException;

import javax.management.MBeanOperationInfo;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.api.amx.AMXMBeanMetadata;

/**
	Manages uploading or downloading of files to/from the server. Generally
	for internal use only.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(type="updown",leaf=true, singleton=true)
public interface UploadDownloadMgr extends AMXProxy, Utility, Singleton
{
	/**
	 	Initiate an upload operation.  The supplied name is intended as
	 	a prefix; if it contains file system separators such as ":", "/" or "\",
	 	they are converted into the "_" character.
	 
	 @param name		name to use for the temp file, may be null
	 @param totalSize	total size of the file to upload
	 @return an opaque identifier describing this file upload
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public Object initiateUpload(String name, long totalSize )
			throws IOException;

	/**
		Upload bytes for the specified upload
		
	 @param uploadID	the id obtained from initiateUpload()
	 @param bytes		more bytes to be uploaded
	 @return			true if the total upload has been completed, false otherwise
	 @throws			an Exception if a problem occurred
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public boolean uploadBytes(Object uploadID, byte[] bytes)
		throws IOException;
		

	/**
		Ownership of transferred bytes (now in a File) are transferred to
		the caller.
		
		@param uploadID	the id obtained from initiateUpload()
		@return a File object for a file containing the uploaded bytes
		@throws			an Exception if the uploadID doesn't exist, or has not finished.
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public File takeUpload( Object uploadID );



	/**
	 Initiates a file download with the given filename. This operation
	 may be used locally or remotely, but the File specified must exist
	 and be readable on the server.
	 
	 @param theFile	 		an accessible File
	 @param deleteWhenDone	whether to delete the file when done
	 @return the downloadID to be used for subequent calls to downloadBytes()
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public Object initiateDownload( File theFile, boolean deleteWhenDone )
		throws IOException;
		
	/**
		Get the total length the download will be, in bytes.
		
	 	@param downloadID	the dowloadID, as obtained from initiateDownload()
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public long getDownloadLength( final Object downloadID );

	/**
		@return the maximum allowable request size for downloading bytes
	 */
    @ManagedAttribute
	public int	getMaxDownloadChunkSize();

	/**
	 Download bytes from the server using the downloadID obtained from
	 initiateDownload().
	 <p>
	 The bufferSize is the requested number of bytes to 
	 be received. If the size of the returned byte[] is less than
	 the requestSize, then the transfer has completed, and the
	 downloadID is no longer valid.  An attempt to read more than
	 the allowed maximum size will throw an exception.  The caller
	 can check the total download size in advance via
	 getDownloadLength().
	 
	 @param downloadID	the id from initiateDownload()
	 @return bytes remaining bytes, up to the request size
	 */
    @ManagedOperation(impact=MBeanOperationInfo.ACTION)
	public byte[] downloadBytes( Object downloadID, int requestSize )
		throws IOException;



}
