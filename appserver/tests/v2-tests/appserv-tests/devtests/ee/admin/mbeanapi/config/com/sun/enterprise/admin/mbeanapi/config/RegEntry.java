/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.enterprise.admin.mbeanapi.config;

import java.util.HashMap;

/**
 * This is the class for element representing object.
 * It contains element name and attributes for testing element
 * Thhis object is using in cofig related generic tests (create/delete/update/list...)
 * @author alexkrav
 * @version $Revision: 1.2 $
 */
//************************************************************************************************
public class RegEntry
{

    String name;
    String dtdName;
    String[] requiredAttrs;
    Class[]  requiredAttrClasses;
    String   masterNode;

    RegEntry(String name, String dtdName, String[] required, String masterNode)
    {
        this.name = name;
        this.dtdName = dtdName;
        this.masterNode = masterNode;
        requiredAttrs = required;
        requiredAttrClasses = new Class[requiredAttrs.length];
        Class strClass = name.getClass();
        Class intClass = Integer.TYPE;
        for(int i=0; i<requiredAttrs.length; i++)
        {
            if(requiredAttrs[i].endsWith("*int"))
            {
                requiredAttrs[i] = requiredAttrs[i].substring(0, requiredAttrs[i].length()-4);
                requiredAttrClasses[i] = intClass;
            }
            else
            {
                requiredAttrClasses[i] = strClass;
            }
        }
    }
    public String[] getReqAttrs()
    {
        return requiredAttrs;
    }
    public Class[] getReqAttrClasses()
    {
        return requiredAttrClasses;
    }
    public String  getMasterNodeName()
    {
        return masterNode;
    }
}

