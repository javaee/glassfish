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
    PROPRIETARY/CONFIDENTIAL. Use of this product is subject
    to license terms. Copyright (c) 2002 Sun Microsystems, Inc.
        All rights reserved.
 
    $Id: MBeanNamingDescriptor.java,v 1.4 2005/12/25 04:14:34 tcfujii Exp $
 */

package com.sun.enterprise.admin.server.core.mbean.config.naming;

//import com.sun.enterprise.admin.util.Debug;
import com.sun.enterprise.admin.common.Name;
import com.sun.enterprise.admin.common.MalformedNameException;

import com.sun.enterprise.admin.common.exception.MBeanConfigException;

//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.text.MessageFormat;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Provides naming support for ConfigMbeans
 */
public class MBeanNamingDescriptor
{
    String   m_type;
    String   m_className;
    String   m_objectPattern;
    String[] m_dottedPatterns;
    String   m_xpathPattern;
    int      m_mode;
    
    //work objects
    int      m_parmListSize = 0;
    Object[][] m_dottedTokens = null;
    Object[] m_objectTokens = null;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( MBeanNamingDescriptor.class );
    
    public MBeanNamingDescriptor(Object[] description) throws MBeanConfigException
    {
        this((String)description[0], (Integer)description[1], (String)description[2], (String)description[3], (String)description[4], (String)description[5]);
    }
    
    public MBeanNamingDescriptor(String type, Integer mode, String dottedPatterns, String objectPattern, String xpathPattern, String className) throws MBeanConfigException
    {
        m_type     = type;
        m_className     = className;
        m_dottedPatterns = splitDottedPatternsString(dottedPatterns);
        m_xpathPattern  = xpathPattern;
        m_objectPattern = objectPattern;
        m_mode          = mode.intValue();
        
        try
        {
            m_dottedTokens = new Object[m_dottedPatterns.length][];
            for(int i=0; i<m_dottedPatterns.length; i++)
                m_dottedTokens[i] = getDottedNamePatternTokens(m_dottedPatterns[i]);
            m_objectTokens = getObjectNamePatternTokens(m_objectPattern);
            m_parmListSize = getMaxTokenIndex(m_objectTokens) + 1;
            
            checkConsistency();
        }
        catch(Exception e)
        {
			String msg = localStrings.getString( "admin.server.core.mbean.config.naming.mbeandescriptor_creation_failure_for_object_pattern", objectPattern, e.getMessage() );
            throw new MBeanConfigException( msg );
        }
    }
    
    private void checkConsistency() throws MBeanConfigException
    {
    }
    
    //DOTTED NAME SERVICES
    boolean isDottedPatternMatch(Name name)
    {
        if(findDottedPatternTokens(name)!=null)
            return true;
        return false;
    }

    private Object[] findDottedPatternTokens(Name name)
    {
        for(int i=0; i<m_dottedTokens.length; i++)
        {
            if(isDottedPatternMatch(name, m_dottedTokens[i], true))
                return m_dottedTokens[i];
        }
        return null;
    }
    
    boolean isObjectNamePatternMatch(Hashtable sample)
    {
        if(m_objectTokens.length!=(sample.size()*2))
            return false;
        for(int i=0; i<m_objectTokens.length; i = i+2)
        {
            String sampleVal = (String)sample.get(m_objectTokens[i]);
            if(sampleVal==null || 
               ((m_objectTokens[i+1] instanceof String) && !sampleVal.equals((String)m_objectTokens[i+1])) )
                return false;
        }
        return (true);
    }
    
    
    String[] extractParmList(String dottedName)  throws MalformedNameException
    {
        if(m_dottedTokens==null)
            return null;
        Name name = new Name(dottedName);
        Object[] tokens = findDottedPatternTokens(name);
        if(tokens == null)
            return null;
        int  nTokens = name.getNumParts();
        if(name.getNumParts()!=tokens.length)
            return null;
        
        String[] parmList = new String[m_parmListSize];
        
        for(int i=0; i<nTokens; i++)
        {
            if( tokens[i] instanceof Integer )
            {
                parmList[((Integer)tokens[i]).intValue()] = name.getNamePart(i).toString();
            }
        }
        return parmList;
    }
    
    
    
    private Object[] getDottedNamePatternTokens(String dottedPattern)  throws MalformedNameException
    {
        ArrayList list = new ArrayList();
        int       idx = 0, idx2 = 0;
        
        if(dottedPattern!=null)
        {
            while(idx<dottedPattern.length() && (idx2=dottedPattern.indexOf('.', idx))>=0)
            {
                if(idx == idx2)
                    list.add("");
                else
                    list.add(dottedPattern.substring(idx,idx2).trim());
                idx = idx2+1;
            }
            if(idx<dottedPattern.length())
                list.add(dottedPattern.substring(idx).trim());
            Object[] tokens = list.toArray();
            replacePlaceholdersToIntegers(tokens);
            return tokens;
        }
        return null;
    }
    
    
    //ObjectName services
    private Object[] getObjectNamePatternTokens(String objectPattern)  throws MalformedObjectNameException
    {
        if(objectPattern!=null)
        {
            ObjectName objName = new ObjectName(objectPattern);
            Hashtable  ht = objName.getKeyPropertyList();
            Enumeration e = ht.keys();
            Object[] tokens = new Object[ht.size()*2];
            int i = 0;
            while(e.hasMoreElements())
            {
                String key = (String)e.nextElement();
                tokens[i++] = key;
                tokens[i++] = ht.get(key);
            }
            replacePlaceholdersToIntegers(tokens);
            return tokens;
        }
        return null;
    }
    
    public int getParmListSize()
    {
        return m_parmListSize;
    }
    public String[] extractParmList(ObjectName objectName)
    {
        if(m_objectTokens==null)
            return null;
        Hashtable  ht = objectName.getKeyPropertyList();
        String[] parmList = new String[m_parmListSize];
        for(int i=0; i<m_objectTokens.length; i=i+2)
        {
            if( m_objectTokens[i+1] instanceof Integer )
            {
                parmList[((Integer)m_objectTokens[i+1]).intValue()] = (String)ht.get(m_objectTokens[i]);
            }
        }
        return parmList;
    }
    
    private void replacePlaceholdersToIntegers(Object[] tokens)
    {
        for(int i=0; i<tokens.length; i++)
        {
            Object idx = getIndexForPlaceholder((String)tokens[i]);
            if(idx!=null)
                tokens[i] = idx;
        }
    }
    
    private Integer getIndexForPlaceholder(String str)
    {
        int len = str.length();
        if(len<3 || str.charAt(0)!='{' || str.charAt(len-1)!='}')
            return null;
        try
        {
            return Integer.valueOf(str.substring(1,len-1));
        }
        catch(Throwable e)
        {
        }
        return null;
    }
    
    private int getMaxTokenIndex(Object[] tokens)
    {
        int res = -1;
        int current;
        for(int i=0; i<tokens.length; i++)
        {
            if(tokens[i] instanceof Integer &&
            res < (current=((Integer)tokens[i]).intValue()))
                res  = current;
        }
        return res;
    }
    
    public String getMBeanClassName()
    {
        return m_className;
    }
    
    public String getType()
    {
        return m_type;
    }
    
    public int getMode()
    {
        return m_mode;
    }
    
    public String[] getDottedPatterns()
    {
        return m_dottedPatterns;
    }
    
    public Object[][] getDottedTokens()
    {
        return m_dottedTokens;
    }

    public String getXPathPattern()
    {
        return m_xpathPattern;
    }
    
    public ObjectName createObjectName(Object[] params) throws MalformedObjectNameException
    {
        return new ObjectName(formatPattern(m_objectPattern, params));
    }
    public String createDottedNameName(Object[] params)
    {
        if(m_dottedPatterns==null || m_dottedPatterns.length<1)
            return null;
        return formatPattern(m_dottedPatterns[0], params);
    }
    public String createXPath(Object[] params)
    {
        return formatPattern(m_xpathPattern, params);
    }
    private String formatPattern(String pattern, Object[] params)
    {
        return MessageFormat.format(pattern, params);
    }

    private String[] splitDottedPatternsString(String names)
    {
        ArrayList list = new ArrayList();
        int idx = 0, idx2 = 0;
        while(idx<names.length() && (idx2=names.indexOf(MBeansDescriptions.PATTERNS_SEPARATOR, idx))>=0)
        {
            if(idx2!=idx)
                list.add(names.substring(idx, idx2));
            idx = idx2+1;
        }
        if(idx2<0)
            list.add(names.substring(idx));
        return (String[])list.toArray(new String[list.size()]);
    }

    public static boolean isDottedPatternMatch(Name name, Object[] tokens, boolean bExactMatch)
    {
        if(tokens==null)
            return false;
        int  nTokens = name.getNumParts();
        if(bExactMatch)
        {
            if(nTokens!=tokens.length)
                return false;
        }
        else
        {
            if(nTokens>tokens.length)
                return false;
        }
        for(int i=0; i<nTokens; i++)
        {
            if( (tokens[i] instanceof String) &&
            !name.getNamePart(i).toString().equals((String)tokens[i]))
                return false;
        }
        return true;
    }
    
}
