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
 *   $Id: MBeansNaming.java,v 1.4 2006/03/12 01:26:57 jluehe Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeansNaming.java,v $
 *   Revision 1.4  2006/03/12 01:26:57  jluehe
 *   Renamed AS's org.apache.commons.* to com.sun.org.apache.commons.*, to avoid collisions with org.apache.commons.* packages bundled by webapps.
 *
 *   Tests run: QL, Servlet TCK
 *
 *   Revision 1.3  2005/12/25 03:47:39  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:45  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.6  2004/11/14 07:04:23  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.5  2004/02/20 03:56:16  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.4.4.1  2004/02/02 07:25:21  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.4  2003/06/25 20:03:41  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/


package com.sun.enterprise.admin.meta.naming;


//import com.sun.enterprise.admin.meta.MBeanRegistry;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;

import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.FieldInfo;

//JMX imports
import javax.management.ObjectName;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *   Provides naming support for Mbeans
 *
 */

public class MBeansNaming
{
    //PUBLIC CONSTANTS
    public static final int  MBEAN_CATEGORY_SYSTEM      = 0x01;
    public static final int  MBEAN_CATEGORY_CONFIG      = 0x02;
    public static final int  MBEAN_CATEGORY_RUNTIME     = 0x04;

    //PRIVATE CONTSTANTS
    final static String CONFIG_MBEANS_BASE_CLASS_PREFIX    = "com.iplanet.ias.admin.server.core.mbean.config.";
    final static char   PATTERNS_SEPARATOR                 = '|';
    final public static int  MODE_CONFIG       = 0x0001;
    final public static int  MODE_MONITOR      = 0x0002;
    private final static String MSG_FINDNAMINDESCRIPTOR_FAILED = "mbean.config.findnamingdescriptor_failed";
    private final static String MSG_MALFORMED_DOTTED_NAME = "mbean.config.malformed_dotted_name";
    private final static String MSG_EXCEPTION_DURING_LIST_NAMES = "mbean.config.list_names_failed";
    public static final Logger sLogger = Logger.getLogger("test"); //AdminConstants.kLoggerName);
/* alexkrav 
    protected MBeanNamingDescriptor[] m_mbeanDescr = null; //initMBeanNaming();
    protected String m_defaultDomainName = "domain_name";     //default value for {0} placeholder - set in initNaming()
    protected String m_defaultInstanceName = "instance_name"; //default value for {1} placeholder - set in initNaming()

    //**************************************************************************
    protected MBeanNamingDescriptor findNamingDescriptorByType(/*int category,/ String type)
    {
        try
        {
            for(int i=0; i<m_mbeanDescr.length; i++)
            {
                if(/*category==m_mbeanDescr[i].m_category &&/ type.equals(m_mbeanDescr[i].m_type))
                    return m_mbeanDescr[i];
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }
    //**************************************************************************
    public MBeanNamingDescriptor findNamingDescriptor(String dottedName)
    {
        try
        {
            Name name = new Name(dottedName);
            for(int i=0; i<m_mbeanDescr.length; i++)
            {
                if(m_mbeanDescr[i].isDottedPatternMatch(name))
                    return m_mbeanDescr[i];
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }
    //**************************************************************************
    public MBeanNamingDescriptor findNamingDescriptor(ObjectName objectName)
    {
        try
        {
           Hashtable ht = objectName.getKeyPropertyList();
           ht.put(":",objectName.getDomain()); //add domain name pseudo pair
           for(int i=0; i<m_mbeanDescr.length; i++)
           {
               if(m_mbeanDescr[i].isObjectNamePatternMatch(ht))
                   return m_mbeanDescr[i];
           }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }
*/
/*
//**************************************************************************
    public String[] findNameContinuation(String instanceName, String dottedName)
    {
        HashSet hs = new HashSet();
        int wildDescrIndex = -1;
        Name name = null;
        int  nNameTokens = 0;

        //if there is no such instance - next statement will throw runtime exception
        InstanceEnvironment instanceEnvironment= new InstanceEnvironment(instanceName);
        
        //First: add "static" continuations
        try
        {
            name = new Name(dottedName);
            nNameTokens = name.getNumParts();
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_MALFORMED_DOTTED_NAME, e);
            return new String[0];
        }

        for(int i=0; i<m_mbeanDescr.length; i++) //enumerate all descriptors
        {
            Object[][] tokens = m_mbeanDescr[i].getDottedTokens();
            if(tokens!=null)
            {
                for(int j=0; j<tokens.length; j++) //enum different dotted patterns presentations
                {
                    if(MBeanNamingDescriptor.isDottedPatternMatch(name, tokens[j], false) && tokens[j].length>nNameTokens)
                    {
                        //dotted pattern beginning matches to sample
                        if(!(tokens[j][nNameTokens] instanceof String)) //wildcard?
                        {
                            if(tokens[j].length==nNameTokens+1) //only if wildcard at the end; otherwise - ignore
                                wildDescrIndex = i;
                        }
                        else
                        {
                            hs.add(dottedName+"."+tokens[j][nNameTokens]);
                        }
                    }
                }
            }
        }
        //Now try to add childrens names
        String xpath = null;
        if(wildDescrIndex>=0)
        {
            try
            {
                MBeanNamingInfo info = new MBeanNamingInfo(dottedName + ".fake");
                xpath = info.getXPath();
            }
            catch (Exception e)
            {
               sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, e);
            }
        }
        if(xpath!=null)
        {
            String attributeName = null;
            String elementName= null;
            // seek for elemname[@attrname=]
            xpath = xpath.trim();
            if(xpath.length()>0 && xpath.endsWith("]"))
            {
                int i = xpath.lastIndexOf('@') + 1;
                int j = xpath.indexOf('=',i) ;
                if(i>0 && j>i)
                {
                    attributeName = xpath.substring(i,j).trim();
                    j = xpath.lastIndexOf('[');
                    if(j>0 && j<i)
                    {
                        xpath = xpath.substring(0,j);
                        j = xpath.lastIndexOf('/');
                        if(j>0 && j<xpath.length()-2)
                        {
                            elementName = xpath.substring(j+1).trim();
                            xpath = xpath.substring(0,j);
                        }
                    }
                }
                
            }
            
            if(attributeName!=null && elementName!=null) //is parsed successfully
            {
                //here we are to call ConfiBeans methods
                ConfigContext configContext;
                try
                {
                    String fileUrl  = instanceEnvironment.getBackupConfigFilePath();
                    configContext   = ConfigFactory.createConfigContext(fileUrl);
                    ConfigBean bean = ConfigBeansFactory.getConfigBeanByXPath(configContext, xpath);
                    ConfigBean[] childs =  bean.getChildBeansByName(elementName);
                    for(int i=0; i<childs.length; i++)
                    {
                        String next = childs[i].getAttributeValue(attributeName);
                        if(next!=null)
                            hs.add(dottedName+"."+next);
                    }
                    
                }
                catch (ConfigException ce)
                {
                   sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, ce);
                }
                catch (NullPointerException npe) //ConfigBean returns this exception by many reasons
                {
                   sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, npe);
                }
            }
        }
        return (String[])hs.toArray(new String[hs.size()]);
    }
*/
    
/*    private HashMap convertListFieldsToHashMap(List fields)
    {
        Iterator iter = fields.iterator();
        HashMap fldsMap = new HashMap();
        while(iter.hasNext())
        {
            FieldInfo field = (FieldInfo)iter.next();
            fldsMap.put(field.getName(), field.getValue());
        }
        return fldsMap;
    }
*/    
/*    synchronized public MBeanNamingDescriptor[]  initMBeanNaming() // throws MBeanNamingException
    {
        print("####################################### initMBeanNaming() ##############################################");
        ArrayList arr = new ArrayList();    
        try
        {
            String[] names = MBeanRegistry.getMBeanResgistryEntryNames();
            for(int i=0; i<names.length; i++)
            {
                //FIXME just for prototype
                ManagedBean mb = MBeanRegistry.findMBeanRegistryEntry(names[i]).managedBean;
                HashMap fields = convertListFieldsToHashMap(mb.getFields());
                    arr.add( new MBeanNamingDescriptor( //MBeansNaming.MBEAN_CATEGORY_RUNTIME,
                            names[i], new Integer(MODE_CONFIG), 
                            (String)fields.get("CLIName"), 
                            (String)fields.get("ObjectName"), 
                            (String)fields.get("persistLocation"), 
                            (String)mb.getClassName())); // fields.get("Class")));
            }
        }
       catch (MBeanNamingException e)
       {
       }
      
       m_mbeanDescr = (MBeanNamingDescriptor[])arr.toArray(new MBeanNamingDescriptor[arr.size()]); 
       return m_mbeanDescr; //descrs;
    }
*/    
    
/*    
    static private MBeanNamingInfo getMBeanNamingDescriptor(String type)
    {
        int idx = getTypeIndex(type);
        if(idx<0)
           return null;
        return m_mbeanDescr[idx];
    }
    
    static private int getTypeIndex(String type)
    {
       Integer i = (Integer)m_typeIndex.get(type);
       if(i==null)
           return -1;
       return i.intValue();
    }
*/
        
    
}
