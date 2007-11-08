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

import javax.management.*;
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.common.Param;
import com.sun.enterprise.admin.common.AdminRequest;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 */
public class AdminRequestConfigurator
{
    private final AdminRequest request;

    /**
     * Creates new AdminRequestConfigurator
     */
    public AdminRequestConfigurator(AdminRequest request)
    {
        ArgChecker.checkValid(request, "request");  //NOI18N
        this.request = request;
    }

    /**
     * Sets the objectName param in the request.
     */
    public void setObjectName(ObjectName objectName)
    {
        request.addParam(new Param(AdminConstants.OBJECT_NAME, objectName));
    }

    /**
     * Gets the objectName param value from the request.
     */
    public ObjectName getObjectName()
    {
        ObjectName objectName = null;
        Param param = request.getParam(AdminConstants.OBJECT_NAME);
        if (param != null)
        {
            objectName = (ObjectName)(param.mValue);
        }
        return objectName;
    }

    /**
     * Sets the clientVersion param in the request.
     */
    public void setClientVersion(String clientVersion)
    {
        request.addParam(new Param(AdminConstants.CLIENT_VERSION, 
                                        clientVersion));
    }

    /**
     * Gets the clientVersion param value from the request.
     */
    public String getClientVersion()
    {
        String clientVersion = null;
        Param param = request.getParam(AdminConstants.CLIENT_VERSION);
        if (param != null)
        {
            clientVersion = (String)(param.mValue);
        }
        return clientVersion;
    }

    /**
     * Sets the operationName param in the request.
     */
    public void setOperationName(String operationName)
    {
        request.addParam(new Param(AdminConstants.OPERATION_NAME, 
                                        operationName));
    }

    /**
     * Gets the operationName param value from the request.
     */
    public String getOperationName()
    {
        String operationName = null;
        Param param = request.getParam(AdminConstants.OPERATION_NAME);
        if (param != null)
        {
            operationName = (String)(param.mValue);
        }
        return operationName;
    }

    /**
     * Sets the 'params' param in the request.
     */
    public void setOperationParams(Object[] params)
    {
        request.addParam(new Param(AdminConstants.OPERATION_PARAMS, params));
    }

    /**
     * Gets the 'params' param from the request.
     */
    public Object[] getOperationParams()
    {
        Object[] params = null;
        Param param = request.getParam(AdminConstants.OPERATION_PARAMS);
        if (param != null)
        {
            params = (Object[])(param.mValue);
        }
        return params;
    }

    /**
     * Sets the signature param in the request.
     */
    public void setOperationSignature(String[] signature)
    {
        request.addParam(new Param(AdminConstants.OPERATION_SIGNATURE, 
                                   signature));
    }

    /**
     * Gets the signature param from the request.
     */
    public String[] getOperationSignature()
    {
        String[] signature = null;
        Param param = request.getParam(AdminConstants.OPERATION_SIGNATURE);
        if (param != null)
        {
            signature = (String[])(param.mValue);
        }
        return signature;
    }

    /**
     */
    public void setAttributeName(String attributeName)
    {
        request.addParam(new Param(AdminConstants.ATTRIBUTE_NAME,  
                                    attributeName));
    }

    /**
     */
    public String getAttributeName()
    {
        String attributeName = null;
        Param param = request.getParam(AdminConstants.ATTRIBUTE_NAME);
        if (param != null)
        {
            attributeName = (String)(param.mValue);
        }
        return attributeName;
    }

    /**
     */
    public void setAttribute(Attribute attribute)
    {
        request.addParam(new Param(AdminConstants.ATTRIBUTE, attribute));
    }

    /**
     */
    public Attribute getAttribute()
    {
        Attribute attribute = null;
        Param param = request.getParam(AdminConstants.ATTRIBUTE);
        if (param != null)
        {
            attribute = (Attribute)(param.mValue);
        }
        return attribute;
    }

    /**
     */
    public void setAttributeList(AttributeList al)
    {
        request.addParam(new Param(AdminConstants.ATTRIBUTE_LIST, al));
    }

    /**
     */
    public AttributeList getAttributeList()
    {
        AttributeList attributeList = null;
        Param param = request.getParam(AdminConstants.ATTRIBUTE_LIST);
        if (param != null)
        {
            attributeList = (AttributeList)(param.mValue);
        }
        return attributeList;
    }

    /**
     */
    public String[] getAttributeNames()
    {
        String[] attributeNames = null;
        Param param = request.getParam(AdminConstants.ATTRIBUTE_NAMES);
        if (param != null)
        {
            attributeNames = (String[])(param.mValue);
        }
        return attributeNames;
    }

    /**
     */
    public void setAttributeNames(String[] attributeNames)
    {
        request.addParam(new Param(AdminConstants.ATTRIBUTE_NAMES, 
                                   attributeNames));
    }
}