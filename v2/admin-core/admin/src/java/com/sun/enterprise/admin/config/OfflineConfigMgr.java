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
 *   $Id: OfflineConfigMgr.java,v 1.2 2006/05/16 18:26:25 kravtch Exp $
 *   @author: alexkrav
 *
*/

package com.sun.enterprise.admin.config;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.logging.Level;

import javax.management.*;
import javax.management.modelmbean.*;

//config
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;

//meta
import com.sun.enterprise.admin.meta.*;
import com.sun.enterprise.admin.meta.naming.*;

/**
 * <p>Base class for Config MBeans which implements basic config
 * activity according to ModelMBeanInfo provided by MBeanRegistry
 *
 *
 */

public class OfflineConfigMgr implements MBeanMetaConstants
{
    MBeanRegistry _registry; 
    ConfigContext _ctx;
    ArrayList     _allDottedNames;
    ////////////////////////////////////////////////////////////////////
    public OfflineConfigMgr(String domainFileName) throws Exception
    {
        _ctx = ConfigFactory.createConfigContext(domainFileName);
        _registry = MBeanRegistryFactory.getOfflineAdminMBeanRegistry();
        try{
             Class cl = Class.forName("com.sun.enterprise.config.serverbeans.validation.DomainMgr");
             Constructor ctr  = cl.getConstructor(
                  new Class[]{ConfigContext.class, Boolean.TYPE, MBeanRegistry.class});
             _ctx.addConfigContextEventListener((ConfigContextEventListener)
                      ctr.newInstance(new Object[]{_ctx, false, _registry}));
        } catch(Exception e)
        {
            e.printStackTrace();
            throw new MBeanConfigException("error registering validator");
        }
    }
    
    ////////////////////////////////////////////////////////////////////
    public AttributeList getAttributes(String dottedName) throws MBeanConfigException
    {
        String[] splitted = DottedNameHelper.splitAttributeNameFromDottedName(dottedName);
        ManagedConfigBean mb = getManagedConfigBean(splitted[0]);
        AttributeList attrs = mb.getAttributes(new String[]{splitted[1]});
        return DottedNameHelper.addDottedPrefix(attrs, splitted[0]);
    }
    
    ////////////////////////////////////////////////////////////////////
    public AttributeList setAttributes(AttributeList attrsIn)
    {
        AttributeList attrsOut = new AttributeList();
        for(int i=0; i< attrsIn.size(); i++)
        {
            Attribute attr = (Attribute)attrsIn.get(i);
            try {
               AttributeList attrs = setAttribute(attr.getName(), attr.getValue());
               if(attrs.size()>0)
                   attrsOut.addAll(attrs);
            } catch (Exception e) {}
        }
        return attrsOut;    
    }
    
    ////////////////////////////////////////////////////////////////////
    public AttributeList setAttribute(String dottedName, Object value) throws MBeanConfigException,ConfigException
    {
        String[] splitted = DottedNameHelper.splitAttributeNameFromDottedName(dottedName);
        ManagedConfigBean mb = getManagedConfigBean(splitted[0]);
        AttributeList attrsIn = new AttributeList();
        attrsIn.add(new Attribute(splitted[1], value));
        AttributeList attrsOut = mb.setAttributes(attrsIn);
        _ctx.flush();
        return DottedNameHelper.addDottedPrefix(attrsOut, splitted[0]);
    }

    ////////////////////////////////////////////////////////////////////

    public AttributeList addSubvaluesToArrayAttribute(String dottedName, String[] addValues) throws MBeanConfigException,ConfigException
    {
        String[] splitted = DottedNameHelper.splitAttributeNameFromDottedName(dottedName);
        ManagedConfigBean mb = getManagedConfigBean(splitted[0]);
        MBeanAttributeInfo attrInfo = mb.getAttributeInfo(splitted[1]);
        if(attrInfo==null ||
           !attrInfo.getType().startsWith("[")) //array?
        {
            throw new MBeanConfigException("add subelement: attribute type is not array");
        }
        AttributeList list = getAttributes(dottedName);
        ArrayList values = new ArrayList();
        if(list.size()!=0)
        {
            DottedNameHelper.addArrayToList(values, (Object[])((Attribute)list.get(0)).getValue());
        }
        DottedNameHelper.addArrayToList(values, addValues);
        return setAttribute(dottedName, values.toArray(new String[]{}));
    }

    ////////////////////////////////////////////////////////////////////

    public AttributeList removeSubvaluesFromArrayAttribute(String dottedName, String[] removeValues) throws MBeanConfigException,ConfigException
    {
        String[] splitted = DottedNameHelper.splitAttributeNameFromDottedName(dottedName);
        ManagedConfigBean mb = getManagedConfigBean(splitted[0]);
        MBeanAttributeInfo attrInfo = mb.getAttributeInfo(splitted[1]);
        if(attrInfo==null ||
           !attrInfo.getType().startsWith("[")) //array?
        {
            throw new MBeanConfigException("add subelement: attribute type is not array");
        }
        AttributeList list = getAttributes(dottedName);
        ArrayList values = new ArrayList();
        if(list.size()!=0)
        {
            DottedNameHelper.addArrayToList(values, (Object[])((Attribute)list.get(0)).getValue());
            for(int i=0; i<removeValues.length; i++)
            {
                for(int j=0; j<values.size(); j++)
                {
                    if(removeValues[i].equals(values.get(j)))
                    {
                        values.remove(j);
                        break; // only first occasion will be removed
                    }
                }
            }
        }
        return setAttribute(dottedName, values.toArray(new String[]{}));
    }

    ////////////////////////////////////////////////////////////////////
    private  ManagedConfigBean getManagedConfigBean(String dottedName)
             throws MBeanConfigException
    {
        int cmdTargetType =  DottedNameHelper.getDottedNameTargetType(
                                                    _ctx, dottedName);
        MBeanRegistryEntry entry = findRegistryEntry(dottedName, cmdTargetType);
        ManagedConfigBean mbc = null;
        if(entry!=null)
        {
            int elementTargetType = getRegistryEntryTargetType(entry);
            String resolvedName  = DottedNameHelper.resolveDottedNameToTarget(
                               _ctx, dottedName, cmdTargetType, elementTargetType);
            if(resolvedName!=null)
            {
                try {
                    MBeanNamingInfo namingInfo = 
                       new MBeanNamingInfo(entry.getNamingDescriptor(), resolvedName);
                    String xpath = namingInfo.getXPath();
                    ConfigBean configBean = 
                       (ConfigBean)ConfigBeansFactory.getConfigBeanByXPath(_ctx, xpath);
                    if(configBean!=null)
                    {
                        ModelMBeanInfo mbi = entry.createMBeanInfo(namingInfo, "offline");
                        mbc =  new ManagedConfigBean((MBeanInfo)mbi, configBean, _registry);
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        if(mbc==null)
            throw new MBeanConfigException("config element is not found");
        return mbc;
    }
    
    ////////////////////////////////////////////////////////////////////
    private MBeanRegistryEntry findRegistryEntry(String dottedName, int nameTarget)
    {
        MBeanRegistryEntry[] entries =  _registry.findMBeanRegistryEntriesByDottedName(dottedName);
        if(entries==null || entries.length==0)
            return null;
        if(entries.length==1)
            return entries[0];
        for(int i=0; i<entries.length; i++)
        {
            int entryTarget = getRegistryEntryTargetType(entries[i]);
            if(nameTarget==entryTarget)
                return entries[i];
            if ((nameTarget==TARGET_TYPE_SERVER || nameTarget==TARGET_TYPE_CLUSTER) &&
                (   entryTarget==TARGET_TYPE_CONFIG || 
                    entryTarget==TARGET_TYPE_APPLICATION ||
                    entryTarget==TARGET_TYPE_RESOURCE ) )
                return entries[i];
            if ( nameTarget==TARGET_TYPE_DOMAIN  &&
                 entryTarget!=TARGET_TYPE_CONFIG && 
                 entryTarget!=TARGET_TYPE_SERVER &&
                 entryTarget!=TARGET_TYPE_CLUSTER )
                return entries[i];
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////
    private int getRegistryEntryTargetType(MBeanRegistryEntry entry)
    {
       MBeanNamingDescriptor descr = entry.getNamingDescriptor();
       return DottedNameHelper.getTargetTypeForXPath(descr.getXPathPattern());
    }

    public void resetDottedNames()
                    throws Exception {
        synchronized(this) {
            _allDottedNames = null;
        }
    }
    
    public ArrayList getListDottedNames(String mask) 
                    throws Exception
    {
        synchronized(this) {
            if(_allDottedNames==null) {
                _allDottedNames  = new ArrayList();
                DottedNameHelper.collectConfigMBeansDottedNames(_registry, _ctx.getRootConfigBean(), _allDottedNames);
            }
        }
        ArrayList list = DottedNameHelper.filterStringValues(_allDottedNames, mask);
        return DottedNameHelper.sortDottedNames(_ctx, list);
    }
    
}
