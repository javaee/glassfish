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
 *   $Id: MBeanNamingDescriptor.java,v 1.4 2006/05/08 17:18:54 kravtch Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanNamingDescriptor.java,v $
 *   Revision 1.4  2006/05/08 17:18:54  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.3  2005/12/25 03:47:38  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:44  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.10  2005/02/02 19:15:56  kedar
 *   BugId: 6219838. Additional Files for JDK 1.5 upgrade -- Note that from now onwards, we would need JDK 1.5 to run the installer
 *
 *   Revision 1.9  2004/11/14 07:04:22  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.8  2004/02/20 03:56:16  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.7.4.1  2004/02/02 07:25:20  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.7  2003/08/15 23:08:29  kravtch
 *   DottedName Support (generation and call to manager)
 *   notifyRegisterMBean/UnregisterMBean are implemented;
 *   dotted name related opeartions are added to NaminDescriptor and NamingInfo
 *   removeChild support is added;
 *
 *   Revision 1.6  2003/06/25 20:03:41  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta.naming;

//import com.iplanet.ias.admin.util.Debug;



//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.text.MessageFormat;

//i18n import
//import com.iplanet.ias.util.i18n.StringManager;


/**
 * Provides naming support for Mbeans
 */
public class MBeanNamingDescriptor
{
    //int      m_category;
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
    Object[] m_xpathTokens = null;

//	// i18n StringManager
//	private static StringManager localStrings =
//		StringManager.getManager( MBeanNamingDescriptor.class );
    
    public MBeanNamingDescriptor(Object[] description) throws MBeanNamingException
    {
        this((String)description[0], (Integer)description[1], (String)description[2], (String)description[3], (String)description[4], (String)description[5]);
    }
    
    public MBeanNamingDescriptor(String type, Integer mode, String dottedPatterns, 
               String objectPattern, String xpathPattern, String className) throws MBeanNamingException
    {
        m_type     = type;
        m_className     = className;
        m_dottedPatterns = splitDottedPatternsString(dottedPatterns);
        m_xpathPattern  = xpathPattern;
        m_objectPattern = objectPattern;
        m_mode          = mode.intValue();
        
        try
        {
            if(m_dottedPatterns!=null)
            {
                m_dottedTokens = new Object[m_dottedPatterns.length][];
                for(int i=0; i<m_dottedPatterns.length; i++)
                    m_dottedTokens[i] = getDottedNamePatternTokens(m_dottedPatterns[i]);
            }
            m_objectTokens = getObjectNamePatternTokens(m_objectPattern);
            m_xpathTokens  = getXPathTokens(m_xpathPattern);
            m_parmListSize = getMaxTokenIndex(m_objectTokens) + 1;
            
            checkConsistency();
        }
        catch(Exception e)
        {
			String msg = /*localStrings.getString*/( "admin.server.core.mbean.config.naming.mbeandescriptor_creation_failure_for_object_pattern"+ objectPattern+ e.getMessage() );
            throw new MBeanNamingException( msg );
        }
    }
    
    private void checkConsistency() throws MBeanNamingException
    {
    }
    

    //DOTTED NAME SERVICES
    public boolean isDottedPatternMatch(Name name)
    {
        if(findDottedPatternTokens(name)!=null)
            return true;
        return false;
    }

    private Object[] findDottedPatternTokens(Name name)
    {
        if(m_dottedTokens!=null)
        {
            for(int i=0; i<m_dottedTokens.length; i++)
            {
                if(isDottedPatternMatch(name, m_dottedTokens[i], true))
                    return m_dottedTokens[i];
            }
        }
        return null;
    }
    
    //**************************************************************************
    public boolean isObjectNamePatternMatch(ObjectName objectName)
    {
       Hashtable ht = objectName.getKeyPropertyList();
       ht.put(":",objectName.getDomain()); //add domain name pseudo pair
       return isObjectNamePatternMatch(ht);
    }

    //**************************************************************************
    public boolean isObjectNamePatternMatch(Hashtable sample)
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
    
    
    String[] extractParmList(String dottedName)  throws MalformedObjectNameException
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
    
    
    
    private Object[] getDottedNamePatternTokens(String dottedPattern)  throws MalformedObjectNameException
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
        
    //XPath services
    public static Object[] getXPathTokens(String xpathPattern)
    {
        ArrayList list = new ArrayList();
        int       idx = 0, idx2 = 0;
        
        if(xpathPattern!=null)
        {
            while(idx<xpathPattern.length() && (idx2=xpathPattern.indexOf("'", idx))>=0)
            {
                if(idx != idx2)
                    list.add(xpathPattern.substring(idx,idx2));
                idx = idx2+1;
            }
            if(idx<xpathPattern.length())
                list.add(xpathPattern.substring(idx).trim());
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
            ht.put(":",objName.getDomain()); //add domain name pseudo pair
            Enumeration ee = ht.keys();
            Object[] tokens = new Object[ht.size()*2];
            int i = 0;
            while(ee.hasMoreElements())
            {
                String key = ((String)ee.nextElement());
                tokens[i++] = key; //key.trim(); jmx allows to have spaces in keys and values
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
        ht.put(":",objectName.getDomain()); //add domain name pseudo pair
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
    
    static private void replacePlaceholdersToIntegers(Object[] tokens)
    {
        for(int i=0; i<tokens.length; i++)
        {
            Object idx = getIndexForPlaceholder((String)tokens[i]);
            if(idx!=null)
                tokens[i] = idx;
        }
    }
    
    static private Integer getIndexForPlaceholder(String str)
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
    public String[] createDottedNames(Object[] params)
    {
        if(m_dottedPatterns==null || m_dottedPatterns.length<1)
            return null;
        String[] names = new String[m_dottedPatterns.length];
        for(int i=0; i<m_dottedPatterns.length;i++)
           names[i] = formatPattern(m_dottedPatterns[i], params);
        return names;
    }
    public String createXPath(Object[] params)
    {
        return formatPattern(m_xpathPattern, params);
    }
    private String formatPattern(String pattern, Object[] params)
    {
        if(pattern==null)
            return null;
        return MessageFormat.format(pattern, params);
    }

    private String[] splitDottedPatternsString(String names)
    {
        if(names==null)
            return null;
        ArrayList list = new ArrayList();
        int idx = 0, idx2 = 0;
        while(idx<names.length() && (idx2=names.indexOf(MBeansNaming.PATTERNS_SEPARATOR, idx))>=0)
        {
            if(idx2!=idx)
                list.add(names.substring(idx, idx2));
            idx = idx2+1;
        }
        if(idx2<0)
            list.add(names.substring(idx));
        return (String[])list.toArray(new String[list.size()]);
    }

    public boolean isXpathTokensMatch(Object[] tokens)
    {
        if(m_xpathTokens==null || tokens==null)
            return false;
        if(m_xpathTokens.length!= tokens.length)
            return false;
        for(int i=0; i<tokens.length; i++)
        {
            if( (m_xpathTokens[i] instanceof String) &&
            !m_xpathTokens[i].equals(tokens[i]))
                return false;
        }
        return true;
    }
    public String[] extractParmListFromXPath(String xpath)
    {
        Object[] tokens = MBeanNamingDescriptor.getXPathTokens(xpath);
        if(m_xpathTokens==null || tokens==null || m_parmListSize<=0)
            return null;
        if(m_xpathTokens.length!= tokens.length)
            return null;
        String[] parmList = new String[m_parmListSize];
        for(int i=0; i<tokens.length; i++)
        {
            if(m_xpathTokens[i] instanceof Integer)
            {
                parmList[((Integer)m_xpathTokens[i]).intValue()] = (String)tokens[i];
            }        
        }
        return parmList;
    }
    
    private static boolean isDottedPatternMatch(Name name, Object[] tokens, boolean bExactMatch)
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

    public static String XPATH_TO_MASK(String xpath)
    {
        if(xpath==null || xpath.length()==0)
            return xpath;
        
        char[] chrs =  xpath.toCharArray();
        char[] newchrs = new char[chrs.length*2];
        int j = 0;
        for(int i=0; i<chrs.length; i++)
        {
            newchrs[j++] = chrs[i];
            if(chrs[i]=='\'')
                newchrs[j++] = '\'';
        }
        return String.valueOf(newchrs, 0, j);
    }
    
}
