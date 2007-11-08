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

package com.sun.enterprise.admin.comm;

//jdk imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URL;

//admin imports
import com.sun.enterprise.admin.common.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.util.*;

/**
    Class communicating with Servlet over HTTP. Internally it uses java.net.URLConnection,
    for we may need to use it for both HTTP and HTTPS. In case of
    java.net.HttpURLConnection, only HTTP can be used.

    @author Kedar Mhaswade
    @version 1.0
*/
 class ServletConnection implements IConnection
{
    static final String         UNKNOWN_HOST            = "Unknown host : ";
    static final String         INVALID_HOST_PORT       = "Unable to connect to admin-server.  Please check if the server is up and running and that the host and port provided are correct.";
    static final String         UNAUTHORIZED_ACCESS     = 
        "Invalid user or password";

    private URLConnection        mConnection           = null;
    private ObjectOutputStream   mObjectOutStream      = null;
    private ObjectInputStream    mObjectInStream       = null;


   ServletConnection(HttpConnectorAddress a) throws IOException{
	 try{
	   mConnection = a.openConnection("/"+AdminConstants.kAdminServletURI);
	 }
	 catch (IOException ioe){
	   handleException(ioe);
	 }
   }


    /**
        Read an incoming Object.
     */
    public  Object receive(  ) throws IOException, ClassNotFoundException
    {
        Object value = null;
        try
        {
            mObjectInStream = new ObjectInputStream(
                new BufferedInputStream(mConnection.getInputStream()));
            value = mObjectInStream.readObject();
        }
        catch (IOException ioe)
        {
            handleException(ioe);
        }
        return value;
    }

    /**
        Write an object to the connection
     */
    public  void	send( Serializable object ) throws IOException
    {
        try
        {
            mObjectOutStream = new ObjectOutputStream(
                                    new BufferedOutputStream(
                                        mConnection.getOutputStream()));
            mObjectOutStream.writeObject(object);
            mObjectOutStream.flush();
            mObjectOutStream.close();
        }
        catch (IOException ioe)
        {
            handleException(ioe);
        }
    }

    public void	close()
    {
        try
        {
            mObjectInStream.close();
            mObjectOutStream.close();
        }
        catch(Exception e)
        {
            Debug.printStackTrace(e);
        }
    }


    private void handleException(IOException e) throws IOException
    {
        IOException exception = null;
        if (e instanceof java.net.UnknownHostException)
        {
            exception = new java.net.UnknownHostException(UNKNOWN_HOST + 
                                                          e.getMessage());
        }
        else if (e instanceof java.net.ConnectException)
        {
            exception = new java.net.ConnectException(INVALID_HOST_PORT);
        }
        else
        {
            int responseCode = 
                ((HttpURLConnection)mConnection).getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                exception = new IOException(UNAUTHORIZED_ACCESS);
            }
            else
            {
                exception = e;
            }
        }
        throw exception;
    }
}
