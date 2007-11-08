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

package com.sun.enterprise.admin.server.core.servlet;

//j2ee imports
import javax.servlet.http.*;
import javax.servlet.ServletException;

//jdk imports
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
//JMX imports
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;

//Admin imports
import com.sun.enterprise.admin.common.AdminRequest;
import com.sun.enterprise.admin.common.AdminRequestConfigurator;
import com.sun.enterprise.admin.common.AdminRequestType;
import com.sun.enterprise.admin.common.AdminResponse;
import com.sun.enterprise.admin.common.AdminResponseConfigurator;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.exception.AFRuntimeException;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
    This is the entry point to the Admin Framework's API layer.
    For each HTTP Request it receives, its principal functionality is to
    <li>
        deserialize the serialized java objects on its input stream sent by the client.
    <li>
        send the deserialized java objects to dispatcher available to it.
    <li>
        <strong> wait </strong> on dispatcher to return with response.
    <li>
        send the response on output stream after serializing it.

    Hence it is to be noted that this class deals with serialized java objects.
*/

public class AdminAPIEntryServlet extends HttpServlet
{
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( AdminAPIEntryServlet.class );

    private MBeanServer mMBeanServer = null;
    private Logger mLogger = Logger.
                getLogger(AdminConstants.kLoggerName);
    private final int SUPPORTED_CLIENT_MAJOR_VERSION = 2;
    private final int SUPPORTED_CLIENT_MINOR_VERSION = 0;
    /**
        Populates the recipients in the dispatcher. These are the recipients
        to whom the <strong> admin request </strong> will be forwarded. This needs
        to be done only when the Servlet is loaded by the servlet container.

        @throws ServletException when some configuration error occurs.
    */
    public void init() throws ServletException
    {
        super.init();
	    mMBeanServer = MBeanServerFactory.getMBeanServer();
        mLogger.log(Level.FINE, "comm.init_ok");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    	throws ServletException, IOException
    {
        Serializable resultObject = "TEST";
        try
        {
            mLogger.log(Level.FINE, "comm.recd_request");
            ObjectInputStream inpStream = new ObjectInputStream (
			new BufferedInputStream(
				request.getInputStream()));
            Object obj = inpStream.readObject();
            AdminRequest adminRequest = (AdminRequest)obj;
            
            resultObject = clientVersionCheck(adminRequest);
			if(resultObject == null)
				resultObject = callMBean(adminRequest);

            ObjectOutputStream oos = new ObjectOutputStream (
                   new BufferedOutputStream (response.getOutputStream ()));

            response.setHeader("Content-type", "application/octet-stream");
            final int contentLength = getContentLength(resultObject);
            response.setContentLength(contentLength);
            response.setStatus(HttpServletResponse.SC_OK);

            oos.writeObject(resultObject);
            oos.flush();

        }
        catch(Exception e)
        {
            throw new ServletException(e.getMessage());
        }
    }

    private AdminResponse clientVersionCheck(AdminRequest req)
    {
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        String	clientVersion = null;
        try
        {
            clientVersion  = requestConfig.getClientVersion();
            int dotIdx = clientVersion.indexOf('.');
            int majorVersion = (Integer.valueOf(clientVersion.substring(0, dotIdx))).intValue();
            int minorVersion = (Integer.valueOf(clientVersion.substring(dotIdx+1))).intValue();
            if(majorVersion == SUPPORTED_CLIENT_MAJOR_VERSION &&
               minorVersion <= SUPPORTED_CLIENT_MINOR_VERSION /* backward compartibility */)
                return null;
        }
        catch (Exception e)
        {
        }
        
        // here we are only in case of non-matching version
        // prepare and wrap exception
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator config = new AdminResponseConfigurator(response);
        String msg;
        if(clientVersion == null)
            msg = localStrings.getString( "admin.server.core.servlet.no_client_version" );
        else
            msg = localStrings.getString( "admin.server.core.servlet.nonsupported_client_version", clientVersion);
        config.setException(new AFRuntimeException(msg));
        return response;
	}
	
    private synchronized AdminResponse callMBean(AdminRequest req)
    {
        String type = req.getRequestType();
        AdminResponse response =  null;

        if (type.equals(AdminRequestType.INVOKE))
	    {
		    response = callInvoke(req);
	    }
        else if(type.equals(AdminRequestType.GET_ATTRIBUTE))
        {
            response = callGetAttribute(req);
        }
        else if(type.equals(AdminRequestType.SET_ATTRIBUTE))
        {
            response = callSetAttribute(req);
        }
        else if (type.equals(AdminRequestType.GET_ATTRIBUTES))
        {
            response = callGetAttributes(req);
        }
        else if (type.equals(AdminRequestType.SET_ATTRIBUTES))
        {
            response = callSetAttributes(req);
        }
        else
        {
            response = new AdminResponse();
            AdminResponseConfigurator config = new 
                AdminResponseConfigurator(response);
            config.setException(new Exception("No Such Type"));
	    }
	    return ( response );
    }
    private synchronized AdminResponse callInvoke(AdminRequest req)
    {
        Object  invokeResult = null;
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator responseConfig = new 
            AdminResponseConfigurator(response);
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        ObjectName	objName	    = requestConfig.getObjectName();
        String		oprName	    = requestConfig.getOperationName();
        Object[]	params	    = requestConfig.getOperationParams();
        String[]	signature   = requestConfig.getOperationSignature();
        try
        {
            invokeResult = 
                mMBeanServer.invoke(objName, oprName, params, signature);
            responseConfig.setReturnValue((Serializable)invokeResult);
            mLogger.log(Level.FINE, "comm.remote_invoke_ok", objName);
        }
        catch(Exception e)
        {
            mLogger.log(Level.WARNING, "comm.remote_invoke_failed", unwrapMBeanException(e));
	        responseConfig.setException(e);
	    }
	    return ( response );
    }

    private synchronized AdminResponse callGetAttribute(AdminRequest req)
    {
        Object  invokeResult = null;
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator responseConfig = new 
            AdminResponseConfigurator(response);
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        ObjectName	objName	    = requestConfig.getObjectName();
        String      attrName    = requestConfig.getAttributeName();
        try
        {
            invokeResult = mMBeanServer.getAttribute(objName, attrName);
            responseConfig.setReturnValue((Serializable)invokeResult);
            mLogger.log(Level.FINE, "comm.get_attr_ok", objName);
        }
        catch(Exception e)
        {
            mLogger.log(Level.WARNING, "comm.get_attr_failed", unwrapMBeanException(e));
	        responseConfig.setException(e);
        }
        return ( response );
    }
    private synchronized AdminResponse callSetAttribute(AdminRequest req)
    {
        Object  invokeResult = null;
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator responseConfig = new 
            AdminResponseConfigurator(response);
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        ObjectName	objName	    = requestConfig.getObjectName();
        Attribute   attribute   = requestConfig.getAttribute();
        try
        {
            mMBeanServer.setAttribute(objName, attribute);
            String setValue = "value set: " + attribute.getValue();
            responseConfig.setReturnValue(setValue);
            mLogger.log(Level.FINE, "comm.set_attr_ok", objName);
        }
        catch(Exception e)
        {
            mLogger.log(Level.WARNING, "comm.set_attr_failed", unwrapMBeanException(e));
	        responseConfig.setException(e);
        }
        return ( response );
    }
    private synchronized AdminResponse callGetAttributes(AdminRequest req)
    {
        Object  invokeResult = null;
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator responseConfig = new 
            AdminResponseConfigurator(response);
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        ObjectName  mbeanName   = requestConfig.getObjectName();
        String[]    attributes  = requestConfig.getAttributeNames();
        try
        {
            AttributeList values = mMBeanServer.getAttributes(mbeanName, 
                                                              attributes);
            responseConfig.setReturnValue(values);
            mLogger.log(Level.FINE, "comm.get_attrs_ok", mbeanName);
        }
        catch(Exception t)
        {
            mLogger.log(Level.WARNING, "comm.get_attrs_failed", unwrapMBeanException(t));
            responseConfig.setException(t);
        }
        return ( response );
    }

    private synchronized AdminResponse callSetAttributes(AdminRequest req)
    {
        Object  invokeResult = null;
        AdminResponse response = new AdminResponse();
        AdminResponseConfigurator responseConfig = new 
            AdminResponseConfigurator(response);
        AdminRequestConfigurator requestConfig = new
            AdminRequestConfigurator(req);
        ObjectName      mbeanName   = requestConfig.getObjectName();
        AttributeList   attributes  = requestConfig.getAttributeList();
        try
        {
            AttributeList values = mMBeanServer.setAttributes(mbeanName, 
                                                              attributes);
            responseConfig.setReturnValue(values);
            mLogger.log(Level.FINE, "comm.set_attrs_ok", mbeanName);
        }
        catch(Exception e)
        {
            mLogger.log(Level.WARNING, "comm.set_attrs_failed", unwrapMBeanException(e));
            responseConfig.setException(e);
        }
        return ( response );
    }
    
    
    /** Returns the size of given seialized object in bytes.
        The size is calculated from the underlying ByteArrayOutputStream
        backing an ObjectStream, onto which the Object is written.
    */
    private int getContentLength(Serializable serObject)
    {
        int size = 0;
        ObjectOutputStream oos = null;

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serObject);
            size = baos.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally 
        {
            try
            {
                if (oos != null)
                {
                    oos.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return size;
    }

    
    private Exception unwrapMBeanException(Exception e)
    {
        while(e instanceof MBeanException)
        {
            e = ((MBeanException)e).getTargetException();    
        }
        return e;
    }
}
