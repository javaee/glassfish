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
 * @(#)ImageViewer.java	1.9 07/05/14
 */

package com.sun.activation.viewers;

import java.awt.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;

public class ImageViewer extends Panel implements CommandObject {
    // UI Vars...
    private ImageViewerCanvas canvas = null;
    
    // File Vars
    //    private InputStream data_ins = null;
    private Image image = null;
    private DataHandler _dh = null;
    
    private boolean DEBUG = false;
    /**
     * Constructor
     */
    public ImageViewer(){
	
	// create the ImageViewerCanvas
	canvas = new ImageViewerCanvas();
	add(canvas);
    }
    /**
     * Set the DataHandler for this CommandObject
     * @param DataHandler the DataHandler
     */
    public void setCommandContext(String verb, DataHandler dh)	throws IOException{
	_dh = dh;
	this.setInputStream( _dh.getInputStream() );
    }
    //--------------------------------------------------------------------
    
    /**
     * Set the data stream, component to assume it is ready to
     * be read.
     */
    private void setInputStream(InputStream ins) throws IOException {
	MediaTracker mt = new MediaTracker(this);
	int bytes_read = 0;
	byte data[] = new byte[1024];
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	while((bytes_read = ins.read(data)) >0)
	    baos.write(data, 0, bytes_read);
	ins.close();
	
	// convert the buffer into an image
	image = getToolkit().createImage(baos.toByteArray());
	
	mt.addImage(image, 0);
	
	try {
	    mt.waitForID(0);
	    mt.waitForAll();
	    if(mt.statusID(0, true ) != MediaTracker.COMPLETE){
		System.out.println("Error occured in image loading = " +
				   mt.getErrorsID(0));
		
	    }
	    
	}
	catch(InterruptedException e) {
	    throw new IOException("Error reading image data");
	}
	
	canvas.setImage(image);
	if(DEBUG)
	    System.out.println("calling invalidate");
	
    }
    //--------------------------------------------------------------------
    public void addNotify(){
	super.addNotify(); // call the real one first...
	this.invalidate();
	this.validate();
	this.doLayout();
    }
    //--------------------------------------------------------------------
    public Dimension getPreferredSize(){
	return canvas.getPreferredSize();
    }

}











