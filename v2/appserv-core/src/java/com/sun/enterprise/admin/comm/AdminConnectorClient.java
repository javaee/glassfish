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

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.RuntimeErrorException;

import com.sun.enterprise.admin.util.*;
import com.sun.enterprise.admin.common.AdminRequest;
import com.sun.enterprise.admin.common.AdminRequestType;
import com.sun.enterprise.admin.common.AdminResponse;
import com.sun.enterprise.admin.common.AdminRequestConfigurator;
import com.sun.enterprise.admin.common.AdminResponseConfigurator;
import com.sun.enterprise.admin.common.exception.AFRuntimeException;

/**
 */
public class AdminConnectorClient implements ConnectorClient
{
    private final HttpConnectorAddress connectorAddress;
    private final static String  ADMIN_CLIENT_VERSION = "2.0";

    public AdminConnectorClient(HttpConnectorAddress address)
    {
        this.connectorAddress = address;
        Debug.println("AdminConnectorClient.<init> : " + 
            "host = " + address.getHost() + 
            " port = " + address.getPort() + 
            " user = " + address.getAuthenticationInfo().getUser() );
    }

    /**
     */
    public String getClientVersion()
    {
        return ADMIN_CLIENT_VERSION;
    }
    
    /**
     */
    public Object invoke(ObjectName    mbeanName,
                         String        operationName,
                         Object[]      params,
                         String[]      signature)
        throws  InstanceNotFoundException, MBeanException, ReflectionException
    {
        Object returnValue = null;
        AdminRequest request = new AdminRequest(AdminRequestType.INVOKE);
        AdminRequestConfigurator reqConfigurator = 
            new AdminRequestConfigurator(request);
        reqConfigurator.setObjectName(mbeanName);
        reqConfigurator.setOperationName(operationName);
        reqConfigurator.setOperationParams(params);
        reqConfigurator.setOperationSignature(signature);
        reqConfigurator.setClientVersion(ADMIN_CLIENT_VERSION);

        AdminResponse response = sendRequest(request);
        AdminResponseConfigurator resConfigurator = 
            new AdminResponseConfigurator(response);
        if (resConfigurator.hasException())
        {
            Throwable t = resConfigurator.getException();
            if (t instanceof InstanceNotFoundException)
            {
                throw (InstanceNotFoundException)t;
            }
            else if (t instanceof MBeanException)
            {
                throw (MBeanException)t;
            }
            else if (t instanceof ReflectionException)
            {
                throw (ReflectionException)t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
                throw new RuntimeErrorException((Error)t);
            }
        }
        else
        {
            returnValue = resConfigurator.getReturnValue();
        }
        return returnValue;
    }

    /**
     */
    public Object getAttribute(ObjectName mbeanName, String attributeName)
        throws  MBeanException, AttributeNotFoundException, 
                InstanceNotFoundException, ReflectionException
    {
        Object returnValue = null;
        AdminRequest request = new AdminRequest(AdminRequestType.GET_ATTRIBUTE);
        AdminRequestConfigurator reqConfigurator = 
            new AdminRequestConfigurator(request);
        reqConfigurator.setObjectName(mbeanName);
        reqConfigurator.setAttributeName(attributeName);
        reqConfigurator.setClientVersion(ADMIN_CLIENT_VERSION);
        AdminResponse response = sendRequest(request);
        //Assert.assert(response);
        AdminResponseConfigurator resConfigurator = 
            new AdminResponseConfigurator(response);
        if (resConfigurator.hasException())
        {
            Throwable t = resConfigurator.getException();
            if (t instanceof AttributeNotFoundException)
            {
                throw (AttributeNotFoundException)t;
            }
            else if (t instanceof InstanceNotFoundException)
            {
                throw (InstanceNotFoundException)t;
            }
            else if (t instanceof MBeanException)
            {
                throw (MBeanException)t;
            }
            else if (t instanceof ReflectionException)
            {
                throw (ReflectionException)t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
                throw new RuntimeErrorException((Error)t);
            }
        }
        else
        {
            returnValue = resConfigurator.getReturnValue();
        }
        return returnValue;
    }

    /**
     */
    public void setAttribute(ObjectName mbeanName, Attribute attribute)
        throws  InstanceNotFoundException, AttributeNotFoundException, 
                InvalidAttributeValueException, MBeanException, 
                ReflectionException
    {
        AdminRequest request = new AdminRequest(AdminRequestType.SET_ATTRIBUTE);
        AdminRequestConfigurator reqConfigurator = 
            new AdminRequestConfigurator(request);
        reqConfigurator.setObjectName(mbeanName);
        reqConfigurator.setAttribute(attribute);
        reqConfigurator.setClientVersion(ADMIN_CLIENT_VERSION);
        AdminResponse response = sendRequest(request);
        //Assert.assert(response);
        AdminResponseConfigurator resConfigurator = 
            new AdminResponseConfigurator(response);
        if (resConfigurator.hasException())
        {
            Throwable t = resConfigurator.getException();
            if (t instanceof AttributeNotFoundException)
            {
                throw (AttributeNotFoundException)t;
            }
            else if (t instanceof InvalidAttributeValueException)
            {
                throw (InvalidAttributeValueException)t;
            }
            else if (t instanceof InstanceNotFoundException)
            {
                throw (InstanceNotFoundException)t;
            }
            else if (t instanceof MBeanException)
            {
                throw (MBeanException)t;
            }
            else if (t instanceof ReflectionException)
            {
                throw (ReflectionException)t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
                throw new RuntimeErrorException((Error)t);
            }
        }
    }

    /**
     */
    public AttributeList getAttributes(ObjectName   mbeanName, 
                                       String[]     attributes)
        throws  InstanceNotFoundException, ReflectionException
    {
        AttributeList values = null;
        AdminRequest request = new AdminRequest(
                                    AdminRequestType.GET_ATTRIBUTES);
        AdminRequestConfigurator reqConfigurator = 
            new AdminRequestConfigurator(request);
        reqConfigurator.setObjectName(mbeanName);
        reqConfigurator.setAttributeNames(attributes);
        reqConfigurator.setClientVersion(ADMIN_CLIENT_VERSION);
        AdminResponse response = sendRequest(request);
        //Assert.assert(response);
        AdminResponseConfigurator resConfigurator = 
            new AdminResponseConfigurator(response);
        if (resConfigurator.hasException())
        {
            Throwable t = resConfigurator.getException();
            if (t instanceof InstanceNotFoundException)
            {
                throw (InstanceNotFoundException)t;
            }
            else if (t instanceof ReflectionException)
            {
                throw (ReflectionException)t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
                throw new RuntimeErrorException((Error)t);
            }
        }
        else
        {
            values = (AttributeList) resConfigurator.getReturnValue();
        }
        return values;
    }

    /**
     */
    public AttributeList setAttributes(ObjectName       mbeanName, 
                                       AttributeList    attributes)
        throws  InstanceNotFoundException, ReflectionException
    {
        AttributeList values = null;
        AdminRequest request = new AdminRequest(
                                    AdminRequestType.SET_ATTRIBUTES);
        AdminRequestConfigurator reqConfigurator = 
            new AdminRequestConfigurator(request);
        reqConfigurator.setObjectName(mbeanName);
        reqConfigurator.setAttributeList(attributes);
        reqConfigurator.setClientVersion(ADMIN_CLIENT_VERSION);
        AdminResponse response = sendRequest(request);
        //Assert.assert(response);
        AdminResponseConfigurator resConfigurator = 
            new AdminResponseConfigurator(response);
        if (resConfigurator.hasException())
        {
            Throwable t = resConfigurator.getException();
            if (t instanceof InstanceNotFoundException)
            {
                throw (InstanceNotFoundException)t;
            }
            else if (t instanceof ReflectionException)
            {
                throw (ReflectionException)t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException)t;
            }
            else if (t instanceof Error)
            {
                throw new RuntimeErrorException((Error)t);
            }
        }
        else
        {
            values = (AttributeList) resConfigurator.getReturnValue();
        }
        return values;
    }

    private AdminResponse sendRequest(AdminRequest request)
        throws AFConnectionException, AFRuntimeException
    {
        AdminResponse   response    = null;
        IConnection     connection  = null;
        try
        {
		  connection = ConnectionFactory.createConnection(connectorAddress);
            connection.send(request);
            response = (AdminResponse)connection.receive();
        }
        catch (IOException e)
        {
            Debug.printStackTrace(e);
            throw new AFConnectionException(e.getMessage());
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new AFRuntimeException(cnfe.toString());
        }
        catch (Exception e)
        {
            throw new AFRuntimeException(e.getLocalizedMessage());
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (Exception e)
                {
                    ExceptionUtil.ignoreException(e);
                }
            }
        }
        return response;
    }
}
