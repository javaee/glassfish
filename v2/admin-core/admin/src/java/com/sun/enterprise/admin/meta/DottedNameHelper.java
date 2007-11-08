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

package com.sun.enterprise.admin.meta;

import com.sun.enterprise.admin.config.MBeanConfigException;
//naming
import com.sun.enterprise.admin.meta.naming.*;

//jdk imports
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.sun.enterprise.util.LocalStringManagerImpl;

// config imports
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
//serverbean helpers
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;

//JMX
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *  This class provides helper methods for xpath handling in ConfigBeans space
 *
 *  $Id: DottedNameHelper.java,v 1.3 2007/04/03 01:13:39 llc Exp $
 *  @author alexkrav
 *
 *
*/

public abstract class DottedNameHelper  implements MBeanMetaConstants
{
    static final String PROPERTY_TOKEN = "property";
    static final String PROPERTY_PREFIX = ".property.";
    static final String ALL_PROPERTIES = "property.*";
    static final String _PREFIX_SYMB = "%";
    static final String VALUE_PREFIX_SYMB = "%";
    //spec values for managedConfigBean helper
    static final String SPECNAME_FOR_ALL_PROPERTIES = "property.";
    static final String SPECNAME_FOR_ALL_ATTRIBUTES = "";
    /**
     *
     **/
    public static String[] splitAttributeNameFromDottedName(String dottedName)
                    throws MBeanConfigException
    {
        dottedName = dottedName.replaceAll("\\\\\\.","\\\\\\~");
        String[] tokens = dottedName.trim().split("\\.");

        if(tokens.length>=2)
        {
            int idx = 0;
            if(tokens.length>2 && tokens[tokens.length-2].equals(PROPERTY_TOKEN))
                idx = dottedName.lastIndexOf(PROPERTY_PREFIX);
            else
                idx = dottedName.lastIndexOf(".");
            if(idx>0 && idx<dottedName.length()-1)
            {
                String attrName = dottedName.substring(idx+1).replaceAll("\\\\\\~",".");
                //wildcard
                if(attrName.equals("*"))
                   attrName = SPECNAME_FOR_ALL_ATTRIBUTES;
                else if(attrName.equals(ALL_PROPERTIES))
                   attrName = SPECNAME_FOR_ALL_PROPERTIES;
                return new String[]{
                    dottedName.substring(0, idx).replaceAll("\\\\\\~","\\\\\\."),
                    attrName};
            }
        }
        throw new MBeanConfigException("malformed dotted name");
    }
    
    ////////////////////////////////////////////////////////////////////////
    public static String getFirstToken(String dottedName)
    {
       int idx = dottedName.indexOf('.');
       return (idx>0)?dottedName.substring(0, idx):dottedName;
    }

    ////////////////////////////////////////////////////////////////////////
    public static int getDottedNameTargetType(ConfigContext ctx,
                                              String dottedName)
    {
       String token = getFirstToken(dottedName);
       if(token.startsWith(VALUE_PREFIX_SYMB))
           token = token.substring(VALUE_PREFIX_SYMB.length());
       if(token.equals("domain"))
           return TARGET_TYPE_DOMAIN;
       try {
           if(ConfigAPIHelper.isAConfig(ctx, token))
               return TARGET_TYPE_CONFIG;
       } catch (Exception e) {}
       try {
           if(ServerHelper.isAServer(ctx, token))
               return TARGET_TYPE_SERVER;
       } catch (Exception e) {}
       try {
           if(ClusterHelper.isACluster(ctx, token))
               return TARGET_TYPE_CLUSTER;
       } catch (Exception e) {}
       try {
           if(NodeAgentHelper.isANodeAgent(ctx, token))
               return TARGET_TYPE_NODEAGENT;
       } catch (Exception e) {}
       return TARGET_TYPE_DOMAIN;
    }

    ////////////////////////////////////////////////////////////////////////
    public static AttributeList addDottedPrefix(AttributeList attrs, String dottedName)
    {
        if(attrs==null || attrs.size()==0 ||
           dottedName==null || dottedName.length()==0)
            return attrs;
        AttributeList attrsMod = new AttributeList();
        final String prefix = dottedName + ".";
        for(int i=0; i<attrs.size(); i++)
        {
            Attribute attr = (Attribute)attrs.get(i);
            attrsMod.add(new Attribute(prefix+attr.getName(), attr.getValue()));
        }
        return attrsMod;
    }
    
    ////////////////////////////////////////////////////////////////////////
    public static String resolveDottedNameToTarget(ConfigContext ctx,
                           String dottedName, int fromType, int toType)
    {
        if(dottedName==null || 
          dottedName.length()==0 ||
          fromType==toType)
           return dottedName;
        String newTargetName = null;
        if(toType == TARGET_TYPE_DOMAIN ||
          toType == TARGET_TYPE_APPLICATION ||
          toType == TARGET_TYPE_RESOURCE)
        {
           newTargetName =  "domain";
        }
        else if((fromType == TARGET_TYPE_SERVER || fromType == TARGET_TYPE_CLUSTER) &&
                toType == TARGET_TYPE_CONFIG ) 
        {
            newTargetName = getReferencedConfigName(
                    ctx, getFirstToken(dottedName));
        }
        if(newTargetName==null)
            return dottedName;
        return replaceFirstToken(dottedName, newTargetName);
    }
    
    ////////////////////////////////////////////////////////////////////
    public static String  getReferencedConfigName(ConfigContext ctx, 
                              String targetName)
    {
        String configName = null;
        try {
           return ClusterHelper.getClusterByName(ctx, targetName).getConfigRef();
        } catch (Exception e) {}
        try {
           return ServerHelper.getServerByName(ctx, targetName).getConfigRef();
        } catch (Exception e) {}
        return null;
    }

    
    //****************************************************************************************************
    /**
     * A recursive method to gather all possible dotted names
     * If bean is null, return silently. This method is designed to instantiate
     * all possible mbeans without throwing any exceptions--a best effort
     * solution.
     */
    static public void collectConfigMBeansDottedNames(MBeanRegistry registry, ConfigBean bean, ArrayList list)
    {
        if(bean == null) 
            return;
        String xpath = bean.getAbsoluteXPath("");
        ConfigContext ctx = bean.getConfigContext();
        MBeanNamingInfo namingInfo = registry.getNamingInfoForConfigBean(bean, "testdomain");
        ArrayList dottedNames = registry.getValidDottedNames(namingInfo, VALUE_PREFIX_SYMB);
        
        if(dottedNames!=null && dottedNames.size()>0)
        {
            String[] location = namingInfo.getLocationParams();
            String   tokenToReplace = "";
            if(location.length>1)
            {
                String name = location[1];
                ArrayList additionalTargetNames = new ArrayList();
                int iTargetType = getTargetTypeForXPath(xpath);
                if(iTargetType==TARGET_TYPE_CONFIG)
                {
                    //CONFIG
                    tokenToReplace = VALUE_PREFIX_SYMB+name;
                    try {
                        String refsList = ConfigAPIHelper.
                            getConfigurationReferenceesAsString(ctx, name);
                        addStringListToArrayList(additionalTargetNames, refsList);
                    } catch(Exception e){}
                }
                else if(iTargetType==TARGET_TYPE_RESOURCE)
                {
                    //RESOURCES RESOLUTON
                    tokenToReplace = "domain";
                    try {
                        String refsList = ResourceHelper.
                                  getResourceReferenceesAsString(ctx, name);
                        addStringListToArrayList(additionalTargetNames, refsList);
                    } catch(Exception e){}
                }
                else if(iTargetType==TARGET_TYPE_APPLICATION)
                {
                    //APPS RESOLUTON
                    tokenToReplace = "domain";
                    try {
                        String refsList = ApplicationHelper.
                                  getApplicationReferenceesAsString(ctx, name);
                        addStringListToArrayList(additionalTargetNames, refsList);
                    } catch(Exception e){}
                }
                if(additionalTargetNames.size()>0)
                {
                    ArrayList additionalDottedNames = new ArrayList();
                    for(int i=0; i<dottedNames.size(); i++)
                    {
                        String dottedName =  (String)dottedNames.get(i);
                        if(tokenToReplace==null || isDottedNameStartsWithToken(
                                                dottedName, tokenToReplace))
                            for(int j=0; j<additionalTargetNames.size(); j++)
                            {
                               additionalDottedNames.add(
                                  replaceFirstToken(dottedName, 
                                    VALUE_PREFIX_SYMB+(String)additionalTargetNames.get(j)));
                            }
                    }
                    dottedNames.addAll(additionalDottedNames);
                }
            }
            list.addAll(dottedNames);
        }
        
        try 
        {
            ConfigBean[] beans = bean.getAllChildBeans();
            if(beans==null)
                return;
            for(int i=0; i<beans.length; i++)
            {
                if(beans[i]!=null && beans[i].getConfigContext()==null)
                { //temporary patch for bug #6171788
                    beans[i].setConfigContext(ctx);
                }
                
                try {
                    collectConfigMBeansDottedNames(registry, beans[i], list);
                } catch(Exception ex) {
                    ex.printStackTrace(); 
                }
            }
        }
        catch(Exception e)
        {
             e.printStackTrace(); 
        } 
    }
   
   ////////////////////////////////////////////////////////////////////
   public static int getTargetTypeForXPath(String xpath)
   {
       if (xpath==null)
           return 0;
       if(xpath.startsWith("/domain/configs/config"))
           return TARGET_TYPE_CONFIG;
       else if(xpath.startsWith("/domain/servers/server"))
           return TARGET_TYPE_SERVER;
       else if(xpath.startsWith("/domain/clusters/cluster"))
           return TARGET_TYPE_CLUSTER;
       else if(xpath.startsWith("/domain/node-agents/node-agent"))
           return TARGET_TYPE_NODEAGENT;
       else if(xpath.startsWith("/domain/applications/"))
           return TARGET_TYPE_APPLICATION;
       else if(xpath.startsWith("/domain/resources/"))
           return TARGET_TYPE_RESOURCE;
       else
           return TARGET_TYPE_DOMAIN;
    }

    ////////////////////////////////////////////////////////////////////
    public static boolean isDottedNameStartsWithToken(String dottedName, String token)
    {
        return (dottedName!=null &&
                token!=null &&
                dottedName.startsWith(token) &&
                   (dottedName.length()==token.length() ||
                    dottedName.charAt(token.length())=='.'));
                    
    }

    ////////////////////////////////////////////////////////////////////
    public static String  replaceFirstToken(String dottedName, String replaceTo)
    {
        int idx = dottedName.indexOf('.');
        if(idx<0)
            return replaceTo;
        else
            return replaceTo + dottedName.substring(idx);
    }

    ////////////////////////////////////////////////////////////////////
    public static void  addArrayToList(ArrayList list, Object[] arr)
    {
        for(int i=0; i<arr.length; i++)
            list.add(arr[i]);
    }
    ////////////////////////////////////////////////////////////////////
    public static void  addStringListToArrayList(ArrayList list, String strList)
    {
        if(strList!=null && strList.trim().length()>0)
        {
           String [] elems = strList.trim().split(",");
           addArrayToList(list, elems);
        }
    }

    public static ArrayList removeNamePrefixes(ArrayList list)
    {
        if(list==null)
            return list;
        ArrayList out = new ArrayList();
        for(int i=0; i<list.size(); i++)
        {
            out.add(removeNamePrefixes((String)list.get(i)));
        }
        return out;
    }
    
    public static String removeNamePrefixes(String name)
    {
        if(name==null)
            return name;
        if(name.startsWith(VALUE_PREFIX_SYMB))
            return (name.substring(VALUE_PREFIX_SYMB.length()).
                    replaceAll("\\.\\"+VALUE_PREFIX_SYMB,"."));
        else
            return (name.replaceAll("\\.\\"+VALUE_PREFIX_SYMB,"."));
    }

    public static ArrayList sortDottedNames(ConfigContext ctx, ArrayList list)
    {
        if(list==null || list.size()<2)
            return list;
        // veeery slooow
        TreeMap map  = new TreeMap();
        String  prefix="1";
        for(int i=0; i<list.size(); i++)
        {
            String name = (String)list.get(i);
            int type = getDottedNameTargetType(ctx, name);
            switch(type) {
                case TARGET_TYPE_CONFIG:
                    prefix="2";
                    break;
                case TARGET_TYPE_SERVER:
                    prefix="3";
                    break;
                case TARGET_TYPE_CLUSTER:
                    prefix="4";
                    break;
                case TARGET_TYPE_NODEAGENT:
                    prefix="5";
                    break;
                default: 
                    prefix="1";
                    break;
            }
            map.put(prefix+name, name);
        }
        return new ArrayList(map.values());
    }
    
    public static ArrayList filterStringValues(
        final ArrayList list,
        final String mask)
    {
        if(list==null || mask==null || list.size()==0)
            return list;
            
        String regexp;
        boolean bRemovePrefixBeforeMatching = true;
        final String MUSTBE_PREFIX = "[\\"+VALUE_PREFIX_SYMB+"]";
        final String MAYBE_PREFIX = MUSTBE_PREFIX+"?";
        if(mask.length()==0)
        {
            regexp = mask+"[^.]*";
        }
        else if(mask.indexOf('*')<0)
        {
            regexp = MAYBE_PREFIX+
                   mask.replace(".", "\\."+MAYBE_PREFIX.replace("$", "\\$")) +
                   "\\.([^.]*)"+
                   "(\\."+MUSTBE_PREFIX+"[^.]*)?";
            bRemovePrefixBeforeMatching = false;
        }
        else
        {
            regexp = mask.replace(".", "\\.");
            regexp = regexp.replace("$", "\\$");
            regexp = regexp.replace("*",  ".*");
        }
        final ArrayList out = new ArrayList();
        for(int i=0; i<list.size(); i++)
        {
            String dottedName = (String)list.get(i);
            if(bRemovePrefixBeforeMatching)
               dottedName=removeNamePrefixes(dottedName);     
            if(dottedName.matches(regexp))
            {
                if(bRemovePrefixBeforeMatching)
                   out.add(dottedName);
                else
                   out.add(removeNamePrefixes(dottedName)); 
            }
        }
        return out;
    }
}