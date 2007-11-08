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

package com.sun.enterprise.admin.server.core.mbean.config;

import java.util.Vector;
import java.util.Properties;

import com.sun.enterprise.config.serverbeans.ElementProperty;

/**
 * Class which represents the Resource.
 */
public class Resource
{
    public final static int BEGIN_INDEX = 0;
    public final static int JMS_RESOURCE = BEGIN_INDEX + 1;
    public final static int MAIL_RESOURCE = BEGIN_INDEX + 2;
    public final static int PERSISTENCE_RESOURCE = BEGIN_INDEX + 3;
    public final static int JDBC_RESOURCE = BEGIN_INDEX + 4;
    public final static int JDBC_CONN_POOL = BEGIN_INDEX + 5;
    public final static int CUSTOM_RESOURCE = BEGIN_INDEX + 6;
    public final static int EXT_JNDI_RESOURCE = BEGIN_INDEX + 7;

    private int resType = 0;
    private Properties attributes = new Properties();
    private Vector vProperty = new Vector();
    private String sDescription = null;

    public Resource()
    {
    }
    
    public Resource(int type)
    {
       resType = type;
    }
    
    public int getType()
    {
        return resType;
    }
    
    public void setType(int type)
    {
        resType = type;
    }
    
    public Properties getAttributes()
    {
        return attributes;
    }
    
    public void setAttribute(String name, String value)
    {
        attributes.setProperty(name, value);
    }

    public void setDescription(String sDescription)
    {
        this.sDescription = sDescription;
    }
    
    public String getDescription()
    {
       return sDescription;
    }
    
    public void setElementProperty(String name, String value)
    {
        ElementProperty ep = new ElementProperty();
        ep.setName(name);
        ep.setValue(value);
        vProperty.add(ep);
    }

    public void setElementProperty(String name, String value, String sDesc)
    {
        ElementProperty ep = new ElementProperty();
        ep.setName(name);
        ep.setValue(value);
        ep.setDescription(sDesc);
        vProperty.add(ep);
    }

    public ElementProperty[] getElementProperty()    
    {
        Object[] arrayObj = vProperty.toArray();
        //convert vector to array
        ElementProperty [] epArray = new ElementProperty[arrayObj.length];
        for (int ii=0; ii<arrayObj.length; ii++) {
           epArray[ii] = (ElementProperty)arrayObj[ii];
        }
        return epArray;
    }
}
